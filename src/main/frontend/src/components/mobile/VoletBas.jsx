import { useRef, useState } from 'react'
import './mobile.css'

// Les trois crans du volet (docs/design/README.md § 2.10). Mêmes valeurs que dans mobile.css.
const CRANS = ['apercu', 'mi', 'plein']
const HAUTEUR_APERCU = 208 // poignée + nom, type, labels + boutons d'action
const HAUTEUR_MI_MAX = 452
const MARGE_PLEIN = 72 // barre d'onglets (64) + 8 de respiration en haut
const SEUIL_APPUI = 4 // en dessous, c'est un appui et non un glissement

function hauteursCrans(hauteurPage) {
  return {
    apercu: HAUTEUR_APERCU,
    mi: Math.min(HAUTEUR_MI_MAX, hauteurPage * 0.6),
    plein: hauteurPage - MARGE_PLEIN,
  }
}

/**
 * Volet du bas (mobile) autour de la fiche d'un lieu : trois crans, aperçu / mi-hauteur / plein écran.
 * Se règle en glissant la poignée, en appuyant dessus, ou au clavier (flèches haut et bas).
 */
export default function VoletBas({ children }) {
  const [cran, setCran] = useState('apercu')
  const [hauteur, setHauteur] = useState(null) // px pendant le glissement, sinon null (hauteur du cran)
  const volet = useRef(null)
  const geste = useRef(null)

  const hauteurPage = () => volet.current?.offsetParent?.clientHeight ?? window.innerHeight
  const monter = () => setCran((c) => CRANS[Math.min(CRANS.indexOf(c) + 1, CRANS.length - 1)])
  const descendre = () => setCran((c) => CRANS[Math.max(CRANS.indexOf(c) - 1, 0)])
  const parCran = () => setCran((c) => (c === 'plein' ? 'apercu' : CRANS[CRANS.indexOf(c) + 1]))

  const debut = (e) => {
    geste.current = { y: e.clientY, h: volet.current.getBoundingClientRect().height, bouge: false }
    try { e.currentTarget.setPointerCapture(e.pointerId) } catch { /* le glissement marche aussi sans capture */ }
  }

  const mouvement = (e) => {
    const g = geste.current
    if (!g) return
    const dy = g.y - e.clientY
    if (Math.abs(dy) > SEUIL_APPUI) g.bouge = true
    if (!g.bouge) return
    const h = hauteursCrans(hauteurPage())
    setHauteur(Math.max(h.apercu, Math.min(h.plein, g.h + dy)))
  }

  const fin = () => {
    const g = geste.current
    geste.current = null
    if (!g) return
    if (!g.bouge) {
      parCran()
      return
    }
    // On « aimante » au cran le plus proche de la hauteur atteinte
    const h = hauteursCrans(hauteurPage())
    const atteinte = volet.current.getBoundingClientRect().height
    setCran(CRANS.reduce((m, c) => (Math.abs(h[c] - atteinte) < Math.abs(h[m] - atteinte) ? c : m)))
    setHauteur(null)
  }

  const surTouche = (e) => {
    if (e.key === 'ArrowUp') { e.preventDefault(); monter() }
    if (e.key === 'ArrowDown') { e.preventDefault(); descendre() }
  }

  return (
    <div
      ref={volet}
      className={'atl-volet' + (hauteur !== null ? ' atl-volet--glisse' : '')}
      data-cran={cran}
      style={hauteur !== null ? { height: hauteur } : undefined}
    >
      <button
        type="button"
        className="atl-volet__poignee"
        aria-label={cran === 'plein' ? 'Réduire la fiche' : 'Agrandir la fiche'}
        onPointerDown={debut}
        onPointerMove={mouvement}
        onPointerUp={fin}
        onPointerCancel={() => { geste.current = null; setHauteur(null) }}
        onKeyDown={surTouche}
        // Au clavier (Entrée, Espace), le navigateur envoie un clic sans événement de pointeur
        onClick={(e) => e.detail === 0 && parCran()}
      >
        <span aria-hidden="true" />
      </button>
      <div className="atl-volet__contenu">{children}</div>
    </div>
  )
}
