import { useState } from 'react'
import './MenuBulles.css'

const trait = { fill: 'none', stroke: 'currentColor', strokeLinecap: 'round', strokeLinejoin: 'round', 'aria-hidden': true }

// Icônes propres au menu (reprises de docs/design/maquettes/CarteDesktop.dc.html)
const ICONE_CIRCUIT = (
  <svg width="19" height="19" viewBox="0 0 24 24" strokeWidth="1.8" {...trait}>
    <circle cx="6" cy="18" r="2.2" /><circle cx="18" cy="6" r="2.2" />
    <path d="M8.2 18H15a3 3 0 0 0 0-6H9a3 3 0 0 1 0-6h6.8" />
  </svg>
)
const ICONE_LIEUX = (
  <svg width="19" height="19" viewBox="0 0 24 24" strokeWidth="1.8" {...trait}>
    <path d="M9 6.5h11M9 12h11M9 17.5h11" />
    <circle cx="4.5" cy="6.5" r=".9" /><circle cx="4.5" cy="12" r=".9" /><circle cx="4.5" cy="17.5" r=".9" />
  </svg>
)
const ICONE_FILTRES = (
  <svg width="19" height="19" viewBox="0 0 24 24" strokeWidth="1.8" {...trait}>
    <path d="M4 7h10M18 7h2M4 17h4M12 17h8" /><circle cx="16" cy="7" r="2" /><circle cx="10" cy="17" r="2" />
  </svg>
)

// Positions (px, relatives à la bulle principale) : arc de ≈ 64 px à 0°, 45° et 90°
// (bulle principale 48 px, bulles secondaires 40 px).
const BULLES = [
  { cle: 'circuit', libelle: 'Votre circuit', nom: 'Circuit', icone: ICONE_CIRCUIT, x: 68, y: 4, delai: 0 },
  { cle: 'lieux', libelle: 'Lieux visibles à l’écran', nom: 'Lieux visibles', icone: ICONE_LIEUX, x: 49, y: 49, delai: 40 },
  { cle: 'filtres', libelle: 'Filtrer par type de lieu', nom: 'Filtres', icone: ICONE_FILTRES, x: 4, y: 68, delai: 80 },
]

/**
 * Bulle principale + trois bulles secondaires.
 * `panneau` : 'filtres' | 'lieux' | 'circuit' | null (un seul panneau ouvert à la fois).
 * `compteurs` : { filtres, lieux, circuit } (un compteur à 0 ou absent est masqué).
 */
export default function MenuBulles({ panneau, surChangementPanneau, compteurs = {}, ouvertInitial = false }) {
  const [ouvert, setOuvert] = useState(ouvertInitial)

  const basculerMenu = () => {
    setOuvert((o) => !o)
    surChangementPanneau(null) // fermer le menu referme tout ; l'ouvrir repart sans panneau
  }

  return (
    <nav className="atl-menu" aria-label="Menu de la carte">
      {ouvert &&
        BULLES.map((b) => {
          const actif = panneau === b.cle
          const compteur = compteurs[b.cle]
          return (
            <div key={b.cle} className="atl-menu__pop" style={{ left: b.x, top: b.y, animationDelay: `${b.delai}ms` }}>
              <button
                type="button"
                className={'atl-bulle' + (actif ? ' atl-bulle--active' : '')}
                aria-label={b.libelle}
                aria-expanded={actif}
                onClick={() => surChangementPanneau(actif ? null : b.cle)}
              >
                {b.icone}
              </button>
              {compteur ? (
                <span className="atl-menu__compteur tabular" aria-hidden="true">{compteur}</span>
              ) : null}
              {!panneau && (
                <span className={'atl-menu__nom atl-menu__nom--' + b.cle} aria-hidden="true">{b.nom}</span>
              )}
            </div>
          )
        })}

      <button
        type="button"
        className="atl-bulle-principale"
        aria-label={ouvert ? 'Fermer le menu' : 'Ouvrir le menu : filtres, lieux visibles, circuit'}
        aria-expanded={ouvert}
        onClick={basculerMenu}
      >
        {ouvert ? (
          <svg width="20" height="20" viewBox="0 0 24 24" strokeWidth="2" {...trait}><path d="m6 6 12 12M18 6 6 18" /></svg>
        ) : (
          <svg width="21" height="21" viewBox="0 0 24 24" strokeWidth="2" {...trait}><path d="M4 7h16M4 12h16M4 17h16" /></svg>
        )}
      </button>
    </nav>
  )
}
