import { useState } from 'react'
import Icone from '../Icone.jsx'
import EtapeItem from './EtapeItem.jsx'
import useCircuit from '../../context/CircuitContext.jsx'
import useNotification from '../../context/NotificationContext.jsx'
import { formatDuree, formatKm } from '../../data/format.js'
import './PanneauCircuit.css'

/** Colonne de la bulle Circuit : nom, étapes, récapitulatif, sauvegarde et partage. */
export default function PanneauCircuit({ surFermer }) {
  const {
    nom, setNom, description, setDescription, etapes, calcul, recalcul, sauvegarde,
    retirerEtape, modifierDuree, reordonner, sauvegarder,
  } = useCircuit()
  const { notifier } = useNotification()
  const [annonce, setAnnonce] = useState('') // lue par les lecteurs d'écran
  const [focusId, setFocusId] = useState(null)
  const [glisse, setGlisse] = useState(null) // index de l'étape soulevée
  const [cible, setCible] = useState(null) // index survolé pendant le glisser

  const n = etapes.length
  const peutSauvegarder = n >= 2 && !recalcul && sauvegarde.statut !== 'enCours'

  const deplacer = (de, vers) => {
    if (vers < 0 || vers >= n) return
    const lieu = etapes[de].lieu
    reordonner(de, vers)
    setFocusId(lieu.id)
    setAnnonce(`${lieu.nom} déplacé en position ${vers + 1} sur ${n}.`)
  }

  const copierLien = async () => {
    const url = `${window.location.origin}/circuit/${sauvegarde.codePartage}`
    try {
      await navigator.clipboard.writeText(url)
      notifier('Lien copié')
    } catch {
      notifier(`Lien : ${url}`)
    }
  }

  return (
    <aside className="atl-circuit" aria-label="Votre circuit">
      <div className="atl-circuit__entete">
        <div className="atl-circuit__titre">
          <p>Votre circuit · <span className="tabular">{n}</span> {n > 1 ? 'étapes' : 'étape'}</p>
          <button type="button" className="atl-circuit__fermer" aria-label="Fermer le circuit" onClick={surFermer}>
            <Icone nom="close" taille={18} />
          </button>
        </div>
        <label htmlFor="c-nom" className="atl-sr-only">Nom du circuit</label>
        <input id="c-nom" className="atl-circuit__nom" value={nom} onChange={(e) => setNom(e.target.value)} />
        <label htmlFor="c-desc" className="atl-sr-only">Description</label>
        <textarea
          id="c-desc"
          className="atl-circuit__desc"
          rows="2"
          placeholder="Une description, facultative"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
        />
      </div>

      <div className="atl-sr-only" role="status" aria-live="polite">{annonce}</div>

      {n === 0 ? (
        <div className="atl-circuit__vide">
          <p className="atl-circuit__vide-titre">Votre circuit commence ici</p>
          <p>Ouvrez la fiche d’un lieu et touchez « Ajouter au circuit ». Dès deux lieux, vous pourrez le sauvegarder et le partager.</p>
        </div>
      ) : (
        <ol className="atl-etapes" aria-label="Étapes">
          {etapes.map((etape, i) => (
            <EtapeItem
              key={etape.lieu.id}
              etape={etape}
              index={i}
              troncon={!recalcul ? calcul?.troncons?.[i] : null}
              estDerniere={i === n - 1}
              aFocus={focusId === etape.lieu.id}
              enDeplacement={glisse === i}
              survolDepot={cible === i && glisse !== null && glisse !== i}
              surDeplacerClavier={deplacer}
              surDebutGlisser={setGlisse}
              surSurvolGlisser={setCible}
              surDeposer={(i2) => { if (glisse !== null) deplacer(glisse, i2); setGlisse(null); setCible(null) }}
              surFinGlisser={() => { setGlisse(null); setCible(null) }}
              surModifierDuree={modifierDuree}
              surRetirer={retirerEtape}
            />
          ))}
          {n === 1 && (
            <li className="atl-etape-place">
              <span className="atl-etape__numero atl-etape__numero--vide" aria-hidden="true">2</span>
              Choisissez un deuxième lieu sur la carte
            </li>
          )}
        </ol>
      )}

      <div className="atl-circuit__espace" />

      {n >= 2 && (
        <>
          <div className="atl-recap" role="group" aria-label="Récapitulatif" aria-busy={recalcul}>
            <div>
              <span className="atl-recap__libelle">Distance</span>
              {recalcul ? <span className="atl-fantome atl-recap__fantome" /> : <span className="atl-recap__valeur tabular">{formatKm(calcul?.distanceKm)}</span>}
            </div>
            <div>
              <span className="atl-recap__libelle">Durée</span>
              {recalcul ? <span className="atl-fantome atl-recap__fantome" /> : <span className="atl-recap__valeur tabular">{formatDuree(calcul?.dureeMinutes)}</span>}
            </div>
            <div>
              <span className="atl-recap__libelle">Départ</span>
              {recalcul ? <span className="atl-fantome atl-recap__fantome" /> : <span className="atl-recap__valeur atl-recap__valeur--texte">{calcul?.communeDepart ?? '—'}</span>}
            </div>
          </div>
          <p className="atl-circuit__note">
            {recalcul
              ? 'On recalcule le trajet…'
              : calcul && `${formatDuree(calcul.dureeVisitesMin)} de visite et ${formatDuree(calcul.dureeMarcheMin)} de marche`}
          </p>
        </>
      )}

      {sauvegarde.statut === 'ok' ? (
        <div className="atl-circuit__sauve" role="status">
          <div>
            <p className="atl-circuit__sauve-titre">Sauvegardé · code de partage</p>
            <p className="atl-circuit__code tabular">{sauvegarde.codePartage}</p>
          </div>
          <button type="button" className="atl-btn atl-btn--primary atl-btn--sm" onClick={copierLien}>
            <Icone nom="copy" taille={16} />
            Copier le lien
          </button>
        </div>
      ) : (
        <div className="atl-circuit__pied">
          {sauvegarde.statut === 'erreur' && (
            <p className="atl-circuit__erreur" role="alert">
              <Icone nom="warning" taille={18} />
              <span>Service indisponible, réessayez plus tard. Vos étapes restent ici, rien n’est perdu.</span>
            </p>
          )}
          <button
            type="button"
            className="atl-btn atl-btn--primary"
            style={{ width: '100%' }}
            disabled={!peutSauvegarder}
            aria-describedby={n < 2 ? 'c-pourquoi' : undefined}
            onClick={sauvegarder}
          >
            {n >= 2 && <Icone nom="share" taille={18} />}
            {sauvegarde.statut === 'enCours'
              ? 'Sauvegarde…'
              : sauvegarde.statut === 'erreur'
                ? 'Réessayer'
                : n >= 2 ? 'Sauvegarder et partager' : 'Sauvegarder'}
          </button>
          {n < 2 && (
            <p id="c-pourquoi" className="atl-circuit__pourquoi">
              {n === 1 ? 'Encore un lieu et vous pourrez le sauvegarder' : 'Disponible à partir de 2 lieux'}
            </p>
          )}
        </div>
      )}
    </aside>
  )
}
