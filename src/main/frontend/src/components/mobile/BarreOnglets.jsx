import Icone from '../Icone.jsx'
import './mobile.css'

const ONGLETS = [
  { cle: null, nom: 'Carte', icone: 'map' },
  { cle: 'lieux', nom: 'Liste', icone: 'list' },
  { cle: 'circuit', nom: 'Circuit', icone: 'route' },
]

/**
 * Barre d'onglets du bas (mobile) : Carte / Liste / Circuit.
 * `panneau` : 'filtres' | 'lieux' | 'circuit' | null. Les filtres s'ouvrent par-dessus la carte : l'onglet Carte reste actif.
 */
export default function BarreOnglets({ panneau, surChangementPanneau, nbEtapes = 0 }) {
  const actif = panneau === 'lieux' || panneau === 'circuit' ? panneau : null
  return (
    <nav className="atl-onglets" aria-label="Navigation">
      {ONGLETS.map((o) => {
        const courant = actif === o.cle
        return (
          <button
            key={o.nom}
            type="button"
            className={'atl-onglet' + (courant ? ' atl-onglet--actif' : '')}
            aria-current={courant ? 'page' : undefined}
            onClick={() => surChangementPanneau(o.cle)}
          >
            <span className="atl-onglet__icone">
              <Icone nom={o.icone} taille={22} />
              {o.cle === 'circuit' && nbEtapes > 0 && (
                <span className="atl-onglet__compteur tabular" aria-label={`${nbEtapes} étape${nbEtapes > 1 ? 's' : ''}`}>{nbEtapes}</span>
              )}
            </span>
            {o.nom}
          </button>
        )
      })}
    </nav>
  )
}
