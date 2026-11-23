import { useEffect, useRef, useState } from 'react'
import Icone from '../Icone.jsx'
import Logo from '../Logo.jsx'
import './BarreRecherche.css'

/**
 * Haut gauche : logo + loupe. La barre de recherche est repliée par défaut ;
 * le clic sur la loupe la déplie (focus dans le champ), Échap ou la croix la referment.
 * Le texte de recherche n'est jamais invisible : refermer la barre l'efface, et elle s'affiche déjà ouverte
 * si un texte est actif (ex. saisi sur mobile avant de passer en grand écran).
 */
export default function BarreRecherche({ texte, surChangement }) {
  const [ouverte, setOuverte] = useState(() => Boolean(texte))
  const champ = useRef(null)
  const loupe = useRef(null)
  const deplacerFocus = useRef(false) // seulement après un geste de la personne, jamais au chargement

  // À l'ouverture, le focus va dans le champ ; à la fermeture, il revient sur la loupe (pas sur le haut de la page)
  useEffect(() => {
    if (!deplacerFocus.current) return
    deplacerFocus.current = false
    if (ouverte) champ.current?.focus()
    else loupe.current?.focus()
  }, [ouverte])

  const ouvrir = () => {
    deplacerFocus.current = true
    setOuverte(true)
  }

  const fermer = () => {
    if (texte) surChangement('')
    deplacerFocus.current = true
    setOuverte(false)
  }

  return (
    <div className="atl-haut-gauche">
      {/* Lien du routeur : pas de rechargement, le circuit en cours (en mémoire) est conservé */}
      <Logo />

      {!ouverte && (
        <button
          ref={loupe}
          type="button"
          className="atl-rond atl-rond--48"
          aria-label="Rechercher un lieu ou une commune"
          aria-expanded="false"
          onClick={ouvrir}
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
            onKeyDown={(e) => {
              if (e.key !== 'Escape') return
              e.stopPropagation() // Échap ferme la recherche seulement, pas la fiche ouverte à droite
              fermer()
            }}
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
