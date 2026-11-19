import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState } from 'react'
import * as CircuitApi from '../services/CircuitApi.js'
import useNotification from './NotificationContext.jsx'

const CircuitContext = createContext(null)

/**
 * Circuit en cours, partagé par la fiche, la carte (tracé, numéros) et le panneau Circuit.
 * Toute modification relance le calcul par l'API et annule la sauvegarde précédente.
 */
export function CircuitProvider({ children }) {
  const { notifier } = useNotification()
  const [nom, setNom] = useState('Mon circuit')
  const [description, setDescription] = useState('')
  const [etapes, setEtapes] = useState([]) // [{ lieu, dureeVisiteMin }]
  const [calcul, setCalcul] = useState(null) // réponse de l'API : distanceKm, dureeMinutes, troncons, trace…
  const [recalcul, setRecalcul] = useState(false)
  const [sauvegarde, setSauvegarde] = useState({ statut: 'aucune', codePartage: null }) // aucune | enCours | ok | erreur
  const derniere = useRef(0)
  const derniereSauvegarde = useRef(0) // seule la dernière sauvegarde lancée met à jour l'état
  const version = useRef(0) // incrémentée à chaque modification du circuit

  const circuit = useMemo(
    () => ({ nom, description, etapes: etapes.map((e, i) => ({ ...e, numOrdre: i + 1 })) }),
    [nom, description, etapes],
  )

  useEffect(() => {
    version.current++
  }, [circuit])

  // Recalcul par l'API dès que les étapes changent (ordre, ajout, retrait, durée)
  useEffect(() => {
    setSauvegarde({ statut: 'aucune', codePartage: null })
    if (etapes.length < 2) {
      setCalcul(null)
      setRecalcul(false)
      return
    }
    const numero = ++derniere.current
    setRecalcul(true)
    const minuteur = setTimeout(() => {
      CircuitApi.calculer(circuit)
        .then((c) => numero === derniere.current && setCalcul(c))
        .catch(() => numero === derniere.current && setCalcul(null))
        .finally(() => numero === derniere.current && setRecalcul(false))
    }, 250) // laisse finir la saisie d'une durée
    return () => clearTimeout(minuteur)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [etapes])

  // Le nom ou la description changent : la version sauvegardée n'est plus à jour
  useEffect(() => {
    setSauvegarde((s) => (s.statut === 'ok' ? { statut: 'aucune', codePartage: null } : s))
  }, [nom, description])

  const retirerEtape = useCallback((idLieu) => {
    setEtapes((es) => es.filter((e) => e.lieu.id !== idLieu))
  }, [])

  const ajouterEtape = useCallback((lieu) => {
    setEtapes((es) => (es.some((e) => e.lieu.id === lieu.id) ? es : [...es, { lieu, dureeVisiteMin: 30 }]))
    notifier(`${lieu.nom} ajouté à votre circuit`, {
      action: { libelle: 'Annuler', action: () => retirerEtape(lieu.id) },
    })
  }, [notifier, retirerEtape])

  const modifierDuree = useCallback((idLieu, minutes) => {
    setEtapes((es) => es.map((e) => (e.lieu.id === idLieu ? { ...e, dureeVisiteMin: minutes } : e)))
  }, [])

  // Déplace l'étape de la position `de` à la position `vers` (indices à partir de 0)
  const reordonner = useCallback((de, vers) => {
    setEtapes((es) => {
      if (vers < 0 || vers >= es.length || de === vers) return es
      const copie = [...es]
      const [etape] = copie.splice(de, 1)
      copie.splice(vers, 0, etape)
      return copie
    })
  }, [])

  const sauvegarder = useCallback(async () => {
    const numero = ++derniereSauvegarde.current
    const versionSauvegardee = version.current
    setSauvegarde({ statut: 'enCours', codePartage: null })
    try {
      const enregistre = await CircuitApi.sauvegarder(circuit)
      if (numero !== derniereSauvegarde.current) return // une sauvegarde plus récente est en cours
      if (versionSauvegardee !== version.current) {
        // Modifié pendant la sauvegarde : ce code partagerait une version qui n'est plus celle affichée
        setSauvegarde({ statut: 'aucune', codePartage: null })
        notifier('Le circuit a changé pendant la sauvegarde : sauvegardez-le à nouveau pour le partager')
        return
      }
      setSauvegarde({ statut: 'ok', codePartage: enregistre.codePartage })
      notifier(`Circuit sauvegardé · code ${enregistre.codePartage}`)
    } catch {
      if (numero !== derniereSauvegarde.current) return
      setSauvegarde({ statut: versionSauvegardee === version.current ? 'erreur' : 'aucune', codePartage: null })
    }
  }, [circuit, notifier])

  // « Dupliquer et modifier » : la copie devient le circuit en cours (avec « Annuler »)
  const remplacerPar = useCallback((modele) => {
    const avant = { nom, description, etapes }
    setNom(modele.nom)
    setDescription(modele.description ?? '')
    setEtapes(modele.etapes.map((e) => ({ lieu: e.lieu, dureeVisiteMin: e.dureeVisiteMin })))
    notifier('Le circuit est copié : c’est maintenant le vôtre', {
      action: avant.etapes.length
        ? { libelle: 'Annuler', action: () => { setNom(avant.nom); setDescription(avant.description); setEtapes(avant.etapes) } }
        : null,
    })
  }, [nom, description, etapes, notifier])

  const numeroEtape = useCallback((idLieu) => {
    const i = etapes.findIndex((e) => e.lieu.id === idLieu)
    return i >= 0 ? i + 1 : null
  }, [etapes])

  const valeur = useMemo(
    () => ({
      nom, setNom, description, setDescription, etapes, calcul, recalcul, sauvegarde,
      ajouterEtape, retirerEtape, modifierDuree, reordonner, sauvegarder, numeroEtape, remplacerPar,
    }),
    [nom, description, etapes, calcul, recalcul, sauvegarde, ajouterEtape, retirerEtape, modifierDuree, reordonner, sauvegarder, numeroEtape, remplacerPar],
  )
  return <CircuitContext.Provider value={valeur}>{children}</CircuitContext.Provider>
}

export default function useCircuit() {
  return useContext(CircuitContext)
}
