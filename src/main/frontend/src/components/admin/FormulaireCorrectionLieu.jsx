import { useEffect, useRef, useState } from 'react'
import Icone, { nomIconeType } from '../Icone.jsx'
import { TYPES } from '../../data/referentiels.js'
import { formatDate } from '../../data/format.js'
import useAdministration from '../../hooks/useAdministration.js'

const NOMS_SOURCES = { BASILIC: 'Basilic', DATATOURISME: 'DataTourisme' }

// Lieu (API) -> valeurs du formulaire
const versChamps = (l) => ({
  nom: l.nom ?? '',
  description: l.description ?? '',
  type: l.type ?? 'AUTRE',
  siteWeb: l.siteWeb ?? '',
  photo: l.photo ?? '',
  rue: l.adresse?.rue ?? '',
  codePostal: l.adresse?.codePostal ?? '',
  commune: l.adresse?.commune?.nom ?? '',
})

const estUrl = (v) => /^https?:\/\/\S+\.\S+/.test(v)

function valider(c) {
  const e = {}
  if (!c.nom.trim()) e.nom = 'Le nom ne peut pas être vide.'
  if (c.siteWeb && !estUrl(c.siteWeb)) e.siteWeb = 'Saisissez une adresse complète, commençant par https://'
  if (c.photo && !estUrl(c.photo)) e.photo = 'Saisissez l’adresse complète de l’image, commençant par https://'
  if (c.codePostal && !/^\d{5}$/.test(c.codePostal)) e.codePostal = 'Le code postal fait 5 chiffres.'
  return e
}

/** Rechercher un lieu, corriger ses champs, le masquer ou le réafficher. */
export default function FormulaireCorrectionLieu() {
  const {
    rechercherLieux, corrigerLieu, changerVisibilite: changerVisibiliteLieu, enregistrement, visibiliteEnCours,
  } = useAdministration()
  const [recherche, setRecherche] = useState('')
  const [suggestions, setSuggestions] = useState([])
  const [listeOuverte, setListeOuverte] = useState(false)
  const [actif, setActif] = useState(-1) // suggestion surlignée au clavier
  const [lieu, setLieu] = useState(null) // lieu tel que renvoyé par l'API
  const [champs, setChamps] = useState(null)
  const [erreurs, setErreurs] = useState({})
  const [confirmation, setConfirmation] = useState(false)
  const premierChamp = useRef(null)

  // Suggestions pendant la saisie
  useEffect(() => {
    if (!recherche.trim() || (lieu && recherche === lieu.nom)) {
      setSuggestions([])
      return
    }
    let actuel = true
    const minuteur = setTimeout(() => {
      rechercherLieux(recherche).then((r) => {
        if (!actuel) return
        setSuggestions(r)
        setListeOuverte(true)
        setActif(-1)
      })
    }, 200)
    return () => { actuel = false; clearTimeout(minuteur) }
  }, [recherche, lieu, rechercherLieux])

  const choisir = (l) => {
    setLieu(l)
    setChamps(versChamps(l))
    setErreurs({})
    setConfirmation(false)
    setRecherche(l.nom)
    setListeOuverte(false)
    setTimeout(() => premierChamp.current?.focus(), 0)
  }

  const surToucheRecherche = (e) => {
    if (!listeOuverte || !suggestions.length) return
    if (e.key === 'ArrowDown') { e.preventDefault(); setActif((i) => Math.min(i + 1, suggestions.length - 1)) }
    if (e.key === 'ArrowUp') { e.preventDefault(); setActif((i) => Math.max(i - 1, 0)) }
    if (e.key === 'Enter' && actif >= 0) { e.preventDefault(); choisir(suggestions[actif]) }
    if (e.key === 'Escape') setListeOuverte(false)
  }

  const modifier = (champ) => (e) => {
    setChamps((c) => ({ ...c, [champ]: e.target.value }))
    if (erreurs[champ]) setErreurs((er) => ({ ...er, [champ]: undefined }))
  }

  const enregistrer = async (e) => {
    e.preventDefault()
    const er = valider(champs)
    setErreurs(er)
    if (Object.keys(er).length) {
      document.getElementById(`l-${Object.keys(er)[0]}`)?.focus()
      return
    }
    const corrige = await corrigerLieu({
      id: lieu.id,
      nom: champs.nom.trim(),
      description: champs.description.trim() || null,
      type: champs.type,
      siteWeb: champs.siteWeb.trim() || null,
      photo: champs.photo.trim() || null,
      adresse: {
        ...lieu.adresse,
        rue: champs.rue.trim() || null,
        codePostal: champs.codePostal.trim() || null,
        commune: { ...lieu.adresse?.commune, nom: champs.commune.trim() },
      },
    })
    if (!corrige) return // échec : le hook a prévenu, les saisies restent dans le formulaire
    setLieu(corrige)
    setChamps(versChamps(corrige))
    setRecherche(corrige.nom)
  }

  const changerVisibilite = async (masque) => {
    const l = await changerVisibiliteLieu(lieu, masque)
    if (!l) return
    setLieu((x) => ({ ...x, masque: l.masque }))
    setConfirmation(false)
  }

  const modifie = lieu && JSON.stringify(champs) !== JSON.stringify(versChamps(lieu))
  const derniereSource = lieu?.source?.[lieu.source.length - 1]

  const champ = (id, libelle, props = {}, classe = '') => (
    <div className={classe}>
      <label htmlFor={`l-${id}`} className="admin-libelle">{libelle}</label>
      <input
        id={`l-${id}`}
        className={'admin-champ' + (erreurs[id] ? ' admin-champ--erreur' : '')}
        value={champs[id]}
        onChange={modifier(id)}
        aria-invalid={Boolean(erreurs[id])}
        aria-describedby={erreurs[id] ? `l-${id}-err` : undefined}
        {...props}
      />
      {erreurs[id] && <p id={`l-${id}-err`} className="admin-erreur admin-erreur--champ">{erreurs[id]}</p>}
    </div>
  )

  return (
    <section className="admin-bloc admin-bloc--correction" aria-labelledby="corr-titre">
      <div>
        <h2 id="corr-titre">Corriger un lieu</h2>
        <p className="admin-bloc__sous-titre">Vos corrections passent avant la source, jusqu’à sa prochaine modification.</p>
      </div>

      <div className="admin-recherche">
        <label htmlFor="adm-q" className="atl-sr-only">Rechercher un lieu</label>
        <div className="admin-recherche__champ">
          <Icone nom="search" taille={20} />
          <input
            id="adm-q"
            role="combobox"
            aria-expanded={listeOuverte && suggestions.length > 0}
            aria-controls="adm-suggestions"
            aria-activedescendant={actif >= 0 ? `adm-s-${actif}` : undefined}
            aria-autocomplete="list"
            placeholder="Nom du lieu à corriger…"
            value={recherche}
            onChange={(e) => setRecherche(e.target.value)}
            onKeyDown={surToucheRecherche}
            onFocus={() => suggestions.length && setListeOuverte(true)}
            onBlur={() => setTimeout(() => setListeOuverte(false), 150)}
          />
        </div>
        {listeOuverte && suggestions.length > 0 && (
          <ul id="adm-suggestions" role="listbox" className="admin-suggestions">
            {suggestions.map((s, i) => (
              <li
                key={s.id}
                id={`adm-s-${i}`}
                role="option"
                aria-selected={i === actif}
                className={i === actif ? 'admin-suggestion--active' : undefined}
                onMouseDown={(e) => { e.preventDefault(); choisir(s) }}
              >
                <span data-type={s.type} className="admin-suggestion__picto"><Icone nom={nomIconeType(s.type)} taille={15} /></span>
                <span className="admin-suggestion__nom">{s.nom}</span>
                <span className="admin-suggestion__meta">{s.adresse?.commune?.nom}{s.masque ? ' · masqué' : ''}</span>
              </li>
            ))}
          </ul>
        )}
        {listeOuverte && recherche.trim() && suggestions.length === 0 && !lieu && (
          <p className="admin-recherche__vide">Aucun lieu ne porte ce nom.</p>
        )}
      </div>

      {!lieu ? (
        <p className="admin-indication">Cherchez un lieu par son nom pour afficher sa fiche.</p>
      ) : (
        <div className="admin-correction">
          <form className="admin-formulaire" aria-label="Champs du lieu" onSubmit={enregistrer} noValidate>
            <div className="admin-formulaire__origine">
              <span data-type={champs.type} className="admin-type">
                <Icone nom={nomIconeType(champs.type)} taille={15} />
                {TYPES.find((t) => t.code === champs.type)?.singulier}
              </span>
              <span>
                {derniereSource && NOMS_SOURCES[derniereSource.source]}
                {lieu.dateMiseAJour && <> · mis à jour le <span className="tabular">{formatDate(lieu.dateMiseAJour, 'short')}</span></>}
              </span>
              {lieu.masque && <span className="admin-masque"><Icone nom="warning" taille={14} />Masqué au public</span>}
            </div>

            <div className="admin-grille">
              {champ('nom', 'Nom', { ref: premierChamp, required: true }, 'admin-grille__large')}
              <div className="admin-grille__large">
                <label htmlFor="l-description" className="admin-libelle">Description</label>
                <textarea id="l-description" className="admin-champ" rows="3" value={champs.description} onChange={modifier('description')} />
              </div>
              <div>
                <label htmlFor="l-type" className="admin-libelle">Type</label>
                <select id="l-type" className="admin-champ" value={champs.type} onChange={modifier('type')}>
                  {TYPES.map((t) => <option key={t.code} value={t.code}>{t.singulier}</option>)}
                </select>
              </div>
              {champ('siteWeb', 'Site web', { type: 'url', placeholder: 'https://…' })}
              <div className="admin-grille__large admin-photo">
                <div className="admin-photo__vignette" data-type={champs.type}>
                  {champs.photo && estUrl(champs.photo)
                    ? <img src={champs.photo} alt="" onError={(e) => { e.currentTarget.style.display = 'none' }} />
                    : <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><rect x="3.5" y="4.5" width="17" height="15" rx="1" /><path d="m3.5 16 5-5 4 4 2.5-2.5 5.5 5.5" /><circle cx="15.5" cy="9" r="1.5" /></svg>}
                </div>
                {champ('photo', 'Photo · adresse de l’image', { type: 'url', placeholder: 'https://…' }, 'admin-photo__champ')}
              </div>
            </div>

            <fieldset className="admin-adresse">
              <legend>Adresse</legend>
              <div className="admin-grille admin-grille--4">
                {champ('rue', 'Rue', {}, 'admin-grille__tout')}
                {champ('codePostal', 'Code postal', { inputMode: 'numeric', maxLength: 5, className: 'admin-champ tabular' + (erreurs.codePostal ? ' admin-champ--erreur' : '') })}
                {champ('commune', 'Commune', {}, 'admin-grille__trois')}
              </div>
            </fieldset>

            <div className="admin-formulaire__actions">
              <button type="submit" className="atl-btn atl-btn--primary" disabled={!modifie || enregistrement}>
                {enregistrement ? 'Enregistrement…' : 'Enregistrer les corrections'}
              </button>
              <button type="button" className="atl-btn atl-btn--ghost" disabled={!modifie || enregistrement} onClick={() => { setChamps(versChamps(lieu)); setErreurs({}) }}>
                Annuler
              </button>
            </div>
          </form>

          <aside className="admin-visibilite" aria-label="Visibilité du lieu">
            <h3>Visibilité</h3>
            {lieu.masque ? (
              <>
                <p>Ce lieu est masqué : il n’apparaît ni sur la carte, ni dans les résultats, ni dans les circuits partagés.</p>
                <button type="button" className="atl-btn atl-btn--secondaire atl-btn--sm" disabled={visibiliteEnCours} onClick={() => changerVisibilite(false)}>
                  Réafficher ce lieu
                </button>
              </>
            ) : (
              <>
                <p>Un lieu masqué disparaît de la carte, des résultats et des circuits partagés. Ses données restent en base.</p>
                {!confirmation ? (
                  <button type="button" className="atl-btn atl-btn--secondaire atl-btn--sm admin-btn-danger" onClick={() => setConfirmation(true)}>
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                      <path d="M3 3l18 18" /><path d="M10.6 5.6A9.6 9.6 0 0 1 12 5.5c5 0 8.5 4.5 9.5 6.5a13 13 0 0 1-2.6 3.4M6.6 6.6C4.4 8 3 10.5 2.5 12c1 2 4.5 6.5 9.5 6.5 1.7 0 3.2-.5 4.5-1.2" /><path d="M9.9 9.9a3 3 0 0 0 4.2 4.2" />
                    </svg>
                    Masquer ce lieu
                  </button>
                ) : (
                  <div role="alertdialog" aria-labelledby="conf-t" aria-describedby="conf-d" className="admin-confirmation">
                    <p id="conf-t" className="admin-confirmation__titre">Masquer « {lieu.nom} » ?</p>
                    <p id="conf-d">Le public ne le verra plus. Vous pourrez le réafficher d’ici à tout moment.</p>
                    <div>
                      <button type="button" className="atl-btn atl-btn--primary atl-btn--sm admin-btn-danger-plein" disabled={visibiliteEnCours} onClick={() => changerVisibilite(true)}>
                        Oui, masquer
                      </button>
                      <button type="button" className="atl-btn atl-btn--ghost atl-btn--sm" onClick={() => setConfirmation(false)}>Annuler</button>
                    </div>
                  </div>
                )}
              </>
            )}
          </aside>
        </div>
      )}
    </section>
  )
}
