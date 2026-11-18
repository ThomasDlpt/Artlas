import { useCallback, useEffect, useRef, useState } from 'react'
import * as LieuApi from '../services/LieuApi.js'
import { estValide } from '../data/FiltreRecherche.js'
import useDepartements from './useDepartements.js'

export const FILTRE_VIDE = { texte: '', types: [], codeDepartement: '', label: '' }

// En dessous de ce zoom, la zone est trop large : on ne charge pas les lieux
export const ZOOM_MIN = 10

// Une seule source de vérité : les marqueurs de la carte = la liste de résultats.
export default function useLieux() {
  const [filtre, setFiltre] = useState(FILTRE_VIDE)
  const [bbox, setBbox] = useState(null)
  const [tropLarge, setTropLarge] = useState(false)
  const [resultats, setResultats] = useState([])
  const [chargement, setChargement] = useState(false)
  const [erreur, setErreur] = useState(null)
  const derniereRequete = useRef(0)
  const [essai, setEssai] = useState(0) // incrémenté par reessayer() pour relancer la requête
  const { departements } = useDepartements()
  const filtreValide = estValide(filtre, departements)

  useEffect(() => {
    if (!bbox) return
    if (tropLarge) {
      derniereRequete.current++
      setResultats([])
      setChargement(false)
      return
    }
    if (!filtreValide) {
      // Filtre invalide : il est signalé dans le panneau Filtres, sans appeler le serveur
      derniereRequete.current++
      setResultats([])
      setChargement(false)
      setErreur(null)
      return
    }
    const numero = ++derniereRequete.current
    setChargement(true)
    setErreur(null)
    LieuApi.rechercherDansZone(bbox, filtre)
      .then((lieux) => numero === derniereRequete.current && setResultats(lieux))
      .catch((e) => numero === derniereRequete.current && setErreur(e))
      .finally(() => numero === derniereRequete.current && setChargement(false))
  }, [bbox, tropLarge, filtre, filtreValide, essai])

  const modifierFiltre = useCallback((champ, valeur) => setFiltre((f) => ({ ...f, [champ]: valeur })), [])
  const reinitialiser = useCallback(() => setFiltre(FILTRE_VIDE), [])
  const surDeplacementCarte = useCallback((b, zoom) => {
    setBbox(b)
    setTropLarge(zoom < ZOOM_MIN)
  }, [])
  const reessayer = useCallback(() => setEssai((n) => n + 1), [])

  return { filtre, filtreValide, departements, resultats, chargement, erreur, tropLarge, modifierFiltre, reinitialiser, surDeplacementCarte, reessayer }
}
