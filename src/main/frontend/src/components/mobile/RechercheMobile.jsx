import Icone, { nomIconeType } from '../Icone.jsx'
import { TYPES } from '../../data/referentiels.js'
import './mobile.css'

const ICONE_FILTRES = (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
    <path d="M4 7h10M18 7h2M4 17h4M12 17h8" /><circle cx="16" cy="7" r="2" /><circle cx="10" cy="17" r="2" />
  </svg>
)

/**
 * Haut de l'écran (mobile) : recherche en pilule, bouton Filtres, puis puces de types à défilement horizontal.
 * Les puces sont un raccourci : elles modifient les mêmes types que le panneau Filtres.
 */
export default function RechercheMobile({ texte, surChangement, types, modifierFiltre, filtresOuverts, surFiltres, afficherPuces }) {
  const basculer = (code) =>
    modifierFiltre('types', types.includes(code) ? types.filter((t) => t !== code) : [...types, code])

  return (
    <div className="atl-mobile-haut">
      <div className="atl-mobile-recherche" role="search">
        <Icone nom="search" taille={20} style={{ flex: 'none', color: 'var(--color-ink-muted)' }} />
        <label htmlFor="atl-q-mobile" className="atl-sr-only">Rechercher un lieu ou une commune</label>
        <input
          id="atl-q-mobile"
          type="search"
          value={texte}
          onChange={(e) => surChangement(e.target.value)}
          placeholder="Un lieu, une commune…"
          enterKeyHint="search"
        />
        <button
          type="button"
          className={'atl-mobile-filtres' + (filtresOuverts ? ' atl-mobile-filtres--actif' : '')}
          aria-label="Filtres"
          aria-expanded={filtresOuverts}
          onClick={surFiltres}
        >
          {ICONE_FILTRES}
        </button>
      </div>

      {afficherPuces && (
        <div className="atl-puces" role="group" aria-label="Types de lieu">
          {TYPES.map((t) => {
            const actif = types.includes(t.code)
            return (
              <button
                key={t.code}
                type="button"
                className={'atl-puce' + (actif ? ' atl-puce--actif' : '')}
                data-type={t.code}
                aria-pressed={actif}
                onClick={() => basculer(t.code)}
              >
                <span className="atl-puce__rond"><Icone nom={nomIconeType(t.code)} taille={14} /></span>
                {t.pluriel}
              </button>
            )
          })}
        </div>
      )}
    </div>
  )
}
