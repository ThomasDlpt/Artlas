import { useEffect, useState } from 'react'

// En dessous de cette largeur, la carte passe en disposition mobile (volet du bas, onglets).
export const LARGEUR_MOBILE = 640
const REQUETE = `(max-width: ${LARGEUR_MOBILE}px)`

const lire = () => (typeof window !== 'undefined' && window.matchMedia ? window.matchMedia(REQUETE).matches : false)

/** Vrai quand l'écran est étroit ; suit la rotation et le redimensionnement. */
export default function useMobile() {
  const [mobile, setMobile] = useState(lire)
  useEffect(() => {
    const mq = window.matchMedia(REQUETE)
    const maj = () => setMobile(mq.matches)
    maj()
    mq.addEventListener('change', maj)
    return () => mq.removeEventListener('change', maj)
  }, [])
  return mobile
}
