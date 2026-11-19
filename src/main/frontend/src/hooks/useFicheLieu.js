import { useCallback, useRef, useState } from 'react'
import * as LieuApi from '../services/LieuApi.js'

// Lieu affiché dans la fiche de droite : son id (sélection) et ses détails chargés depuis l'API.
// introuvable : le lieu n'existe pas ou est masqué (null en simulation, 404 côté API), à distinguer d'une panne.
export default function useFicheLieu() {
  const [idLieu, setIdLieu] = useState(null)
  const [lieu, setLieu] = useState(null)
  const [chargement, setChargement] = useState(false)
  const [erreur, setErreur] = useState(null)
  const [introuvable, setIntrouvable] = useState(false)
  const derniere = useRef(0)

  const charger = useCallback((id) => {
    const numero = ++derniere.current
    setChargement(true)
    setErreur(null)
    setIntrouvable(false)
    LieuApi.selectionner(id)
      .then((l) => {
        if (numero !== derniere.current) return
        if (l) setLieu(l)
        else setIntrouvable(true)
      })
      .catch((e) => {
        if (numero !== derniere.current) return
        if (e?.statut === 404) setIntrouvable(true)
        else setErreur(e)
      })
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
    setIntrouvable(false)
  }, [])

  const reessayer = useCallback(() => idLieu && charger(idLieu), [idLieu, charger])

  return { idLieu, lieu, chargement, erreur, introuvable, ouvrir, fermer, reessayer }
}
