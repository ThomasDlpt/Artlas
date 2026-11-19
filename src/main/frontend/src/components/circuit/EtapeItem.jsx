import { useEffect, useRef } from 'react'
import Icone from '../Icone.jsx'
import { libelleType } from '../../data/referentiels.js'
import { formatDuree, formatKm } from '../../data/format.js'

/**
 * Une étape du circuit : poignée (glisser-déposer ou flèches du clavier), numéro, nom,
 * durée de visite modifiable, bouton retirer ; puis le tronçon vers l'étape suivante.
 */
export default function EtapeItem({
  etape, index, troncon, estDerniere, aFocus, enDeplacement, survolDepot,
  surDeplacerClavier, surDebutGlisser, surSurvolGlisser, surDeposer, surFinGlisser,
  surModifierDuree, surRetirer,
}) {
  const { lieu, dureeVisiteMin } = etape
  const poignee = useRef(null)

  // Après un déplacement au clavier, le focus reste sur la poignée de cette étape
  useEffect(() => {
    if (aFocus) poignee.current?.focus()
  }, [aFocus, index])

  const surTouche = (e) => {
    if (e.key === 'ArrowUp' || e.key === 'ArrowDown') {
      e.preventDefault()
      surDeplacerClavier(index, e.key === 'ArrowUp' ? index - 1 : index + 1)
    }
  }

  return (
    <li
      className={'atl-etape' + (enDeplacement ? ' atl-etape--soulevee' : '') + (survolDepot ? ' atl-etape--depot' : '')}
      draggable
      onDragStart={(e) => { e.dataTransfer.effectAllowed = 'move'; surDebutGlisser(index) }}
      onDragOver={(e) => { e.preventDefault(); surSurvolGlisser(index) }}
      onDrop={(e) => { e.preventDefault(); surDeposer(index) }}
      onDragEnd={surFinGlisser}
    >
      <div className="atl-etape__ligne">
        <button
          ref={poignee}
          type="button"
          className="atl-etape__poignee"
          aria-label={`Déplacer ${lieu.nom}, étape ${index + 1}. Flèches haut et bas pour déplacer.`}
          onKeyDown={surTouche}
        >
          <Icone nom="drag" taille={18} />
        </button>
        <span className="atl-etape__numero tabular" aria-hidden="true">{index + 1}</span>
        <span className="atl-etape__texte">
          <span className="atl-etape__nom">{lieu.nom}</span>
          <span className="atl-etape__meta">
            {[libelleType(lieu.type), lieu.adresse?.commune?.nom].filter(Boolean).join(' · ')}
          </span>
        </span>
        <label className="atl-etape__duree">
          <span className="atl-sr-only">Durée de visite de {lieu.nom}, en minutes</span>
          <input
            type="number"
            className="tabular"
            min="5"
            max="600"
            step="5"
            value={dureeVisiteMin}
            onChange={(e) => surModifierDuree(lieu.id, Math.max(0, Number(e.target.value)))}
          />
          <span aria-hidden="true">min</span>
        </label>
        <button type="button" className="atl-etape__retirer" aria-label={`Retirer ${lieu.nom} du circuit`} onClick={() => surRetirer(lieu.id)}>
          <Icone nom="close" taille={16} />
        </button>
      </div>

      {!estDerniere && (
        <p className="atl-etape__troncon">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
            <circle cx="13" cy="4.5" r="1.6" /><path d="m10 21 2-6 2.5 2.5V21" /><path d="m8 11 3-3.5h2.5l2 3.5 2.5 1" />
          </svg>
          {troncon ? (
            <span className="tabular">{formatKm(troncon.distanceKm)} · {formatDuree(troncon.dureeMarcheMin)} à pied</span>
          ) : (
            <span className="atl-fantome" style={{ width: 120 }} />
          )}
        </p>
      )}
    </li>
  )
}
