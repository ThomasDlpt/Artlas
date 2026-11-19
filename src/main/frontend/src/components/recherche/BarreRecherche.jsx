import { useEffect, useRef, useState } from 'react'
import Icone from '../Icone.jsx'
import Logo from '../Logo.jsx'
import './BarreRecherche.css'

/**
 * Haut gauche : logo + loupe. La barre de recherche est repliée par défaut ;
 * le clic sur la loupe la déplie (focus dans le champ), Échap ou la croix la referment.
 * Refermer la barre efface aussi le texte : sinon la carte resterait filtrée par une saisie invisible.
 */
export default function BarreRecherche({ texte, surChangement }) {
  const [ouverte, setOuverte] = useState(false)
  const champ = useRef(null)

  useEffect(() => {
    if (ouverte) champ.current?.focus()
  }, [ouverte])

  const fermer = () => {
    if (texte) surChangement('')
    setOuverte(false)
  }

  return (
    <div className="atl-haut-gauche">
      {/* Lien du routeur : pas de rechargement, le circuit en cours (en mémoire) est conservé */}
      <Logo />

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
