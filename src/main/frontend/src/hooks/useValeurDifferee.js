import { useEffect, useState } from 'react'

/**
 * La valeur, mais seulement une fois qu'elle n'a plus changé pendant `delai` ms (ex. fin de frappe).
 * Une valeur vide s'applique tout de suite : après un effacement, on ne doit jamais
 * retomber sur l'ancienne saisie si la personne recommence à taper avant la fin du délai.
 */
export default function useValeurDifferee(valeur, delai) {
  const [differee, setDifferee] = useState(valeur)
  useEffect(() => {
    if (!valeur) {
      setDifferee(valeur)
      return
    }
    const minuteur = setTimeout(() => setDifferee(valeur), delai)
    return () => clearTimeout(minuteur)
  }, [valeur, delai])
  return differee
}
