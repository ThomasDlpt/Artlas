import { useEffect, useRef, useState } from 'react'
import Icone from '../Icone.jsx'
import './BarreRecherche.css'

/**
 * Haut gauche : logo + loupe. La barre de recherche est repliée par défaut ;
 * le clic sur la loupe la déplie (focus dans le champ), Échap ou la croix la referment.
 */
export default function BarreRecherche({ texte, surChangement }) {
  const [ouverte, setOuverte] = useState(false)
  const champ = useRef(null)

  useEffect(() => {
    if (ouverte) champ.current?.focus()
  }, [ouverte])

  const fermer = () => setOuverte(false)

  return (
    <div className="atl-haut-gauche">
      <a href="/" className="atl-logo-pastille" aria-label="Artlas, accueil">
        <svg width="26" height="26" viewBox="0 0 64 64" aria-hidden="true">
          <path d="M32 7 58 53H6z" fill="none" stroke="currentColor" strokeWidth="5" strokeLinejoin="round" />
          <path d="M19 41h26" stroke="currentColor" strokeWidth="4" strokeLinecap="round" />
          <circle cx="32" cy="33" r="6" fill="var(--color-accent)" />
        </svg>
        <span className="atl-logo">
          Art<i>las</i>
        </span>
      </a>

      {!ouverte && (
        <button
          type="button"
          className="atl-rond atl-rond--48"
          aria-label="Rechercher un lieu ou une commune"
          aria-expanded="false"
          onClick={() => setOuverte(true)}
        >
          <Icone nom="search" taille={21} />
        </button>
      )}

      {ouverte && (
        <div className="atl-recherche" role="search">
          <Icone nom="search" taille={20} style={{ flex: 'none', color: 'var(--color-ink-muted)' }} />
          <label htmlFor="atl-q" className="atl-sr-only">
            Rechercher un lieu ou une commune
          </label>
          <input
            id="atl-q"
            ref={champ}
            value={texte}
            onChange={(e) => surChangement(e.target.value)}
            onKeyDown={(e) => e.key === 'Escape' && fermer()}
            placeholder="Un lieu, une commune…"
          />
          <button type="button" className="atl-recherche__fermer" aria-label="Fermer la recherche" onClick={fermer}>
            <Icone nom="close" taille={18} />
          </button>
        </div>
      )}
    </div>
  )
}
