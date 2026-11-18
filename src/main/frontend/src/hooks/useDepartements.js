import { useEffect, useState } from 'react'
import * as LieuApi from '../services/LieuApi.js'

// La liste change rarement : on la charge une fois par session.
let cache = null

/** Départements connus de la base (ceux des lieux importés). Tant qu'ils ne sont pas là, `departements` est vide. */
export default function useDepartements() {
  const [departements, setDepartements] = useState(cache ?? [])
  const [chargement, setChargement] = useState(cache === null)
  const [erreur, setErreur] = useState(null)

  useEffect(() => {
    if (cache !== null) return
    let actif = true
    LieuApi.listerDepartements()
      .then((liste) => {
        cache = liste
        if (actif) setDepartements(liste)
      })
      .catch((e) => actif && setErreur(e))
      .finally(() => actif && setChargement(false))
    return () => { actif = false }
  }, [])

  return { departements, chargement, erreur }
}
