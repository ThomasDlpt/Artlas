import { useEffect } from 'react'
import Icone, { nomIconeType } from '../Icone.jsx'
import { libelleType } from '../../data/referentiels.js'
import { formatDate } from '../../data/format.js'
import useCircuit from '../../context/CircuitContext.jsx'
import useNotification from '../../context/NotificationContext.jsx'
import './FicheLieu.css'

const NOMS_SOURCES = {
  BASILIC: { nom: 'Basilic', detail: ' — ministère de la Culture' },
  DATATOURISME: { nom: 'DataTourisme', detail: '' },
}

// Le site web vient des données importées : seules les adresses http(s) deviennent des liens
// (une valeur « javascript:… » s'exécuterait au clic). Un nom de domaine sans protocole (« www.château.fr »,
// « musee.fr?lang=fr ») est complété en https ; il doit finir par une extension en lettres, donc pas un numéro de téléphone.
const DOMAINE_NU = /^[\p{L}\p{N}-]+(\.[\p{L}\p{N}-]+)*\.\p{L}{2,}(:\d+)?([/?#]\S*)?$/u
function lienSur(url) {
  const u = typeof url === 'string' ? url.trim() : ''
  if (/^https?:\/\/\S+$/i.test(u)) return u
  if (DOMAINE_NU.test(u)) return `https://${u}`
  return null
}

// « https://www.mba.rennes.fr/ » -> « mba.rennes.fr »
const siteCourt = (url) => url.replace(/^https?:\/\/(www\.)?/, '').replace(/\/$/, '')

/**
 * Fiche d'un lieu, panneau flottant à droite.
 * Peut rester ouverte en même temps qu'un panneau de gauche.
 */
export default function FicheLieu({ lieu, chargement, erreur, introuvable, surFermer, surReessayer }) {
  const { ajouterEtape, numeroEtape } = useCircuit()
  const { notifier } = useNotification()

  // Échap ferme la fiche
  useEffect(() => {
    const surTouche = (e) => e.key === 'Escape' && surFermer()
    window.addEventListener('keydown', surTouche)
    return () => window.removeEventListener('keydown', surTouche)
  }, [surFermer])

  const boutonFermer = (
    <button type="button" className="atl-fiche__fermer" aria-label="Fermer la fiche" onClick={surFermer}>
      <Icone nom="close" taille={18} />
    </button>
  )

  // Lieu inconnu ou masqué par l'administration (null en simulation, 404 côté API) : réessayer ne servirait à rien
  if (introuvable) {
    return (
      <aside className="atl-fiche" aria-label="Fiche du lieu">
        <div className="atl-fiche__photo">{boutonFermer}</div>
        <div className="atl-fiche__message" role="alert">
          <p className="atl-fiche__message-titre">Ce lieu n’est plus disponible</p>
          <p>Il a peut-être été retiré de la carte depuis votre recherche.</p>
        </div>
      </aside>
    )
  }

  if (erreur) {
    return (
      <aside className="atl-fiche" aria-label="Fiche du lieu">
        <div className="atl-fiche__photo">{boutonFermer}</div>
        <div className="atl-fiche__message" role="alert">
          <p className="atl-fiche__message-titre">Service indisponible, réessayez plus tard</p>
          <p>Nous n’arrivons pas à charger ce lieu pour le moment.</p>
          <button type="button" className="atl-btn atl-btn--primary atl-btn--sm" onClick={surReessayer}>Réessayer</button>
        </div>
      </aside>
    )
  }

  // Squelette : même silhouette que la fiche finale
  if (chargement || !lieu) {
    return (
      <aside className="atl-fiche" aria-label="Fiche du lieu" aria-busy="true">
        <div className="atl-fiche__photo atl-fiche__photo--fantome">{boutonFermer}</div>
        <div className="atl-fiche__entete">
          <span className="atl-fantome" style={{ width: '75%', height: 20 }} />
          <span className="atl-fantome" style={{ width: '50%', marginTop: 10 }} />
          <span className="atl-fantome" style={{ width: '100%', height: 44, marginTop: 18 }} />
          <span className="atl-fantome" style={{ width: '100%', marginTop: 22 }} />
          <span className="atl-fantome" style={{ width: '90%', marginTop: 8 }} />
          <span className="atl-fantome" style={{ width: '60%', marginTop: 8 }} />
        </div>
      </aside>
    )
  }

  const { adresse } = lieu
  const commune = adresse?.commune
  const etape = numeroEtape(lieu.id)
  const siteWeb = lienSur(lieu.siteWeb)
  const miseAJour = formatDate(lieu.dateMiseAJour)

  const partager = async () => {
    const url = `${window.location.origin}/?lieu=${encodeURIComponent(lieu.id)}`
    try {
      if (navigator.share) await navigator.share({ title: lieu.nom, url })
      else {
        await navigator.clipboard.writeText(url)
        notifier('Lien copié')
      }
    } catch {
      /* partage annulé par la personne : rien à signaler */
    }
  }

  return (
    <aside className="atl-fiche" data-type={lieu.type} aria-label={`Fiche du lieu : ${lieu.nom}`}>
      <div className="atl-fiche__photo">
        {lieu.photo ? (
          <img src={lieu.photo} alt="" />
        ) : (
          <div className="atl-fiche__sans-photo">
            <span className="atl-fiche__picto"><Icone nom={nomIconeType(lieu.type)} taille={24} /></span>
            <span>Pas de photo fournie par la source</span>
          </div>
        )}
        {boutonFermer}
      </div>

      <div className="atl-fiche__entete">
        <h2 className="atl-fiche__nom">{lieu.nom}</h2>
        <p className="atl-fiche__meta">
          <span className="atl-fiche__type">
            <Icone nom={nomIconeType(lieu.type)} taille={15} />
            {libelleType(lieu.type)}
          </span>
          {commune && (
            <>
              <span aria-hidden="true">·</span>
              <span>{commune.nom}{commune.departement ? `, ${commune.departement.nom}` : ''}</span>
            </>
          )}
        </p>
        {lieu.labels.length > 0 && (
          <ul className="atl-fiche__labels" aria-label="Labels">
            {lieu.labels.map((l) => (
              <li key={l} className="atl-label">{l}</li>
            ))}
          </ul>
        )}
      </div>

      <div className="atl-fiche__actions">
        {etape ? (
          <button type="button" className="atl-btn atl-btn--primary" disabled style={{ flex: 1 }}>
            <Icone nom="check" taille={18} />
            Déjà dans le circuit · étape {etape}
          </button>
        ) : (
          <button type="button" className="atl-btn atl-btn--primary" style={{ flex: 1 }} onClick={() => ajouterEtape(lieu)}>
            <Icone nom="plus" taille={18} />
            Ajouter au circuit
          </button>
        )}
        {siteWeb && (
          <a className="atl-btn atl-btn--secondaire atl-btn--icone" href={siteWeb} target="_blank" rel="noreferrer" aria-label="Site web (nouvel onglet)">
            <Icone nom="external" taille={18} />
          </a>
        )}
        <button type="button" className="atl-btn atl-btn--secondaire atl-btn--icone" aria-label="Partager ce lieu" onClick={partager}>
          <Icone nom="share" taille={18} />
        </button>
      </div>

      {lieu.description && (
        <>
          <div className="atl-fiche__filet" />
          <p className="atl-fiche__description">{lieu.description}</p>
        </>
      )}

      <div className="atl-fiche__filet" />
      <ul className="atl-fiche__infos">
        {adresse && (
          <li>
            <Icone nom="map" taille={20} />
            <span>
              <span className="atl-sr-only">Adresse : </span>
              {[adresse.rue, [adresse.codePostal, commune?.nom].filter(Boolean).join(' ')].filter(Boolean).join(', ')}
            </span>
          </li>
        )}
        {siteWeb && (
          <li>
            <Icone nom="external" taille={20} />
            <a href={siteWeb} target="_blank" rel="noreferrer">{siteCourt(siteWeb)}</a>
          </li>
        )}
        {miseAJour && (
          <li className="atl-fiche__info--discrete">
            <Icone nom="clock" taille={20} />
            <span>Mis à jour le <span className="tabular">{miseAJour}</span></span>
          </li>
        )}
      </ul>

      {lieu.source?.length > 0 && (
        <div className="atl-fiche__sources">
          {lieu.source.map((s) => {
            const n = NOMS_SOURCES[s.source] ?? { nom: s.source, detail: '' }
            const importe = formatDate(s.dateImport) // null si absente ou illisible : rien n'est affiché
            return (
              <p key={s.source + s.idExterne}>
                Source : <b>{n.nom}</b>{n.detail}{importe ? ` · importé le ${importe}` : ''}
              </p>
            )
          })}
        </div>
      )}
    </aside>
  )
}
