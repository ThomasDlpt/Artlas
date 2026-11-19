import { useCallback, useRef, useState } from 'react'
import * as LieuApi from '../services/LieuApi.js'

// Lieu affiché dans la fiche de droite : son id (sélection) et ses détails chargés depuis l'API.
export default function useFicheLieu() {
  const [idLieu, setIdLieu] = useState(null)
  const [lieu, setLieu] = useState(null)
  const [chargement, setChargement] = useState(false)
  const [erreur, setErreur] = useState(null)
  const derniere = useRef(0)

  const charger = useCallback((id) => {
    const numero = ++derniere.current
    setChargement(true)
    setErreur(null)
    LieuApi.selectionner(id)
      .then((l) => numero === derniere.current && setLieu(l))
      .catch((e) => numero === derniere.current && setErreur(e))
      .finally(() => numero === derniere.current && setChargement(false))
  }, [])

  const ouvrir = useCallback((id) => {
    setIdLieu(id)
    setLieu(null)
    charger(id)
  }, [charger])

  const fermer = useCallback(() => {
    derniere.current++
    setIdLieu(null)
    setLieu(null)
    setChargement(false)
    setErreur(null)
  }, [])

  const reessayer = useCallback(() => idLieu && charger(idLieu), [idLieu, charger])

  return { idLieu, lieu, chargement, erreur, ouvrir, fermer, reessayer }
}
