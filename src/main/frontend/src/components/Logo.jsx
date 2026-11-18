import { Link } from 'react-router-dom'

// Pastille « ▲ Artlas » avec lien vers la carte
export default function Logo({ pastille = true, symbole = true }) {
  return (
    <Link to="/" className={'atl-logo-lien' + (pastille ? ' atl-logo-lien--pastille' : '')} aria-label="Artlas, accueil">
      {symbole && (
        <svg width="26" height="26" viewBox="0 0 64 64" aria-hidden="true">
          <path d="M32 7 58 53H6z" fill="none" stroke="currentColor" strokeWidth="5" strokeLinejoin="round" />
          <path d="M19 41h26" stroke="currentColor" strokeWidth="4" strokeLinecap="round" />
          <circle cx="32" cy="33" r="6" fill="var(--color-accent)" />
        </svg>
      )}
      <span className="atl-logo-texte">Art<i>las</i></span>
    </Link>
  )
}
