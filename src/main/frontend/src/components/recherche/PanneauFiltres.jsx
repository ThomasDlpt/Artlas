import { useState } from 'react'
import PastilleType from '../PastilleType.jsx'
import { TYPES, LABELS, nbLieux } from '../../data/referentiels.js'
import {
  trouverDepartement, chercherDepartements, departementsProches, departementValide, libelleDepartement,
} from '../../data/FiltreRecherche.js'
import './panneaux.css'

/**
 * Panneau déroulé sous la bulle Filtres.
 * Multi-sélection des types (aucun choix = tous les types), puis département et label repliés.
 * Département : champ avec recherche parmi les départements connus de la base (`departements`, fournis par l'API).
 * Une saisie inconnue invalide le filtre : message sous le champ, bouton désactivé, aucun appel serveur.
 */
export default function PanneauFiltres({ filtre, departements, modifierFiltre, nbResultats, surVoirLieux }) {
  const [plus, setPlus] = useState(Boolean(filtre.codeDepartement || filtre.label))
  const [saisie, setSaisie] = useState(() => {
    const d = trouverDepartement(departements, filtre.codeDepartement)
    return d ? libelleDepartement(d) : (filtre.codeDepartement ?? '')
  })
  const [quitte, setQuitte] = useState(false) // le champ a perdu le focus avec une saisie incorrecte

  const departementInvalide = !departementValide(departements, filtre.codeDepartement)
  const directes = departementInvalide ? chercherDepartements(departements, saisie) : []
  const suggestions = directes.length ? directes : departementsProches(departements, saisie)
  // Pas de reproche pendant la frappe tant que ce que la personne écrit peut encore mener à un département
  const afficherErreur = departementInvalide && (quitte || directes.length === 0)

  const basculerType = (code) => {
    const actifs = filtre.types.includes(code) ? filtre.types.filter((t) => t !== code) : [...filtre.types, code]
    modifierFiltre('types', actifs)
  }

  const saisirDepartement = (valeur) => {
    setSaisie(valeur)
    setQuitte(false)
    const d = trouverDepartement(departements, valeur)
    // Une saisie qui ne correspond à rien est gardée telle quelle : c'est ce qui rend le filtre invalide
    modifierFiltre('codeDepartement', d ? d.code : valeur.trim())
  }

  const choisirDepartement = (d) => {
    setSaisie(libelleDepartement(d))
    setQuitte(false)
    modifierFiltre('codeDepartement', d.code)
  }

  const quitterChamp = () => {
    const d = trouverDepartement(departements, saisie)
    if (d) setSaisie(libelleDepartement(d))
    else if (saisie.trim()) setQuitte(true)
  }

  const effacer = () => {
    modifierFiltre('types', [])
    modifierFiltre('codeDepartement', '')
    modifierFiltre('label', '')
    setSaisie('')
    setQuitte(false)
  }

  return (
    <section className="atl-panneau atl-panneau--filtres" aria-label="Filtres">
      <p className="atl-panneau__titre">Que voulez-vous voir ?</p>

      <ul className="atl-types">
        {TYPES.map((t) => {
          const actif = filtre.types.includes(t.code)
          return (
            <li key={t.code}>
              <button
                type="button"
                className={'atl-type' + (actif ? ' atl-type--actif' : '')}
                aria-pressed={actif}
                onClick={() => basculerType(t.code)}
              >
                <PastilleType type={t.code} taille={30} />
                <span className="atl-type__nom">{t.pluriel}</span>
                {actif && (
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="var(--color-accent)" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                    <path d="m6 12.5 4 4 8-9" />
                  </svg>
                )}
              </button>
            </li>
          )
        })}
      </ul>

      <button type="button" className="atl-lien-plus" aria-expanded={plus} onClick={() => setPlus((p) => !p)}>
        Département et label
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.9" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true" style={{ transform: plus ? 'rotate(180deg)' : 'none', transition: 'transform .15s' }}>
          <path d="m6 9 6 6 6-6" />
        </svg>
      </button>

      {plus && (
        <div className="atl-plus">
          <label htmlFor="f-dep" className="atl-sr-only">Département</label>
          <input
            id="f-dep"
            type="text"
            className={'atl-select atl-champ' + (afficherErreur ? ' atl-champ--erreur' : '')}
            placeholder={departements.length ? 'Département : 35 ou Ille-et-Vilaine' : 'Aucun département disponible'}
            disabled={departements.length === 0}
            autoComplete="off"
            spellCheck={false}
            value={saisie}
            aria-invalid={afficherErreur}
            aria-describedby={afficherErreur ? 'f-dep-erreur' : undefined}
            onChange={(e) => saisirDepartement(e.target.value)}
            onBlur={quitterChamp}
          />

          {afficherErreur && (
            <p id="f-dep-erreur" className="atl-champ__erreur" role="alert">Ce département n’existe pas.</p>
          )}

          {departementInvalide && suggestions.length > 0 && (
            <ul className="atl-suggestions" aria-label={directes.length ? 'Départements correspondants' : 'Départements proches'}>
              {suggestions.map((d) => (
                <li key={d.code}>
                  <button type="button" className="atl-suggestion" onClick={() => choisirDepartement(d)}>
                    {libelleDepartement(d)}
                  </button>
                </li>
              ))}
            </ul>
          )}

          <label htmlFor="f-label" className="atl-sr-only">Label</label>
          <select id="f-label" className="atl-select" value={filtre.label} onChange={(e) => modifierFiltre('label', e.target.value)}>
            <option value="">Tous les labels</option>
            {LABELS.map((l) => (
              <option key={l} value={l}>{l}</option>
            ))}
          </select>
        </div>
      )}

      <div className="atl-panneau__actions">
        <button
          type="button"
          className="atl-btn atl-btn--primary atl-btn--sm"
          style={{ flex: 1 }}
          disabled={departementInvalide}
          onClick={surVoirLieux}
        >
          <span className="tabular">Voir les {nbLieux(nbResultats)}</span>
        </button>
        <button type="button" className="atl-btn atl-btn--ghost atl-btn--sm" onClick={effacer}>
          Effacer
        </button>
      </div>
    </section>
  )
}
