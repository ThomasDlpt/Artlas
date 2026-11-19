import { useEffect, useState } from 'react'

/** La valeur, mais seulement une fois qu'elle n'a plus changé pendant `delai` ms (ex. fin de frappe). */
export default function useValeurDifferee(valeur, delai) {
  const [differee, setDifferee] = useState(valeur)
  useEffect(() => {
    const minuteur = setTimeout(() => setDifferee(valeur), delai)
    return () => clearTimeout(minuteur)
  }, [valeur, delai])
  return differee
}
