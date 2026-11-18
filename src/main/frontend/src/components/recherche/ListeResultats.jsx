import PastilleType from '../PastilleType.jsx'
import { libelleType, nbLieux, resumeTypes } from '../../data/referentiels.js'
import useCircuit from '../../context/CircuitContext.jsx'
import './panneaux.css'

/**
 * Panneau déroulé sous la bulle Lieux visibles : alternative complète à la carte.
 * La liste suit la carte (recherche dans la zone visible), sans message.
 */
export default function ListeResultats({
  resultats, chargement, erreur, filtre, filtreValide, idSelectionne, idSurvole,
  surSurvol, surClicLieu, surReessayer, surReinitialiser,
}) {
  const { numeroEtape } = useCircuit()
  let contenu
  if (!filtreValide) {
    contenu = (
      <div className="atl-liste__message" role="status">
        <p className="atl-liste__message-titre">Le département saisi n’existe pas</p>
        <p>Corrigez-le dans Filtres pour voir les lieux.</p>
        <button type="button" className="atl-btn atl-btn--ghost atl-btn--sm" onClick={surReinitialiser}>Tout réinitialiser</button>
      </div>
    )
  } else if (erreur) {
    contenu = (
      <div className="atl-liste__message" role="alert">
        <p className="atl-liste__message-titre">Service indisponible, réessayez plus tard</p>
        <p>Nous n’arrivons pas à joindre nos sources pour le moment. Votre circuit, lui, est bien gardé.</p>
        <button type="button" className="atl-btn atl-btn--primary atl-btn--sm" onClick={surReessayer}>Réessayer</button>
      </div>
    )
  } else if (chargement && resultats.length === 0) {
    contenu = (
      <ul className="atl-liste" aria-busy="true" aria-label="Chargement des lieux">
        {[0, 1, 2, 3, 4].map((i) => (
          <li key={i} className="atl-ligne atl-ligne--fantome">
            <span className="atl-fantome atl-fantome--rond" />
            <span style={{ flex: 1 }}>
              <span className="atl-fantome" style={{ width: '70%' }} />
              <span className="atl-fantome" style={{ width: '45%', marginTop: 6 }} />
            </span>
          </li>
        ))}
      </ul>
    )
  } else if (resultats.length === 0) {
    const actifs = filtre.types.length || filtre.codeDepartement || filtre.label || filtre.texte
    contenu = (
      <div className="atl-liste__message">
        <p className="atl-liste__message-titre">Aucun lieu ne correspond à votre recherche</p>
        <p>{actifs ? 'Dézoomez ou retirez un filtre.' : 'Déplacez ou dézoomez la carte.'}</p>
        {actifs && (
          <button type="button" className="atl-btn atl-btn--ghost atl-btn--sm" onClick={surReinitialiser}>Tout réinitialiser</button>
        )}
      </div>
    )
  } else {
    contenu = (
      <ul className="atl-liste" aria-label="Lieux">
        {resultats.map((lieu) => {
          const selectionne = lieu.id === idSelectionne
          const meta = [libelleType(lieu.type), lieu.adresse?.commune?.nom, lieu.labels[0]].filter(Boolean).join(' · ')
          return (
            <li key={lieu.id}>
              <button
                type="button"
                className={'atl-ligne' + (selectionne ? ' atl-ligne--selectionnee' : '') + (idSurvole === lieu.id ? ' atl-ligne--survolee' : '')}
                aria-current={selectionne ? 'true' : undefined}
                onClick={() => surClicLieu(lieu.id)}
                onMouseEnter={() => surSurvol(lieu.id)}
                onMouseLeave={() => surSurvol(null)}
                onFocus={() => surSurvol(lieu.id)}
                onBlur={() => surSurvol(null)}
              >
                <PastilleType type={lieu.type} />
                <span className="atl-ligne__texte">
                  <span className="atl-ligne__nom">{lieu.nom}</span>
                  <span className="atl-ligne__meta">{meta}</span>
                </span>
                {numeroEtape(lieu.id) && (
                  <span className="atl-ligne__etape tabular" title="Étape du circuit">{numeroEtape(lieu.id)}</span>
                )}
              </button>
            </li>
          )
        })}
      </ul>
    )
  }

  return (
    <section className="atl-panneau atl-panneau--lieux" aria-label="Lieux visibles à l’écran">
      <div className="atl-panneau__entete">
        <h2 role="status">
          <span className="tabular">{nbLieux(resultats.length)}</span> à l’écran
        </h2>
        <p>{resumeTypes(filtre.types)} · la liste suit la carte</p>
      </div>
      {contenu}
    </section>
  )
}
