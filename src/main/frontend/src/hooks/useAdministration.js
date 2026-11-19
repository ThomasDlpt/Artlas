import { useCallback, useState } from 'react'
import * as AdminApi from '../services/AdminApi.js'
import useNotification from '../context/NotificationContext.jsx'

const nombre = new Intl.NumberFormat('fr-FR')
const pluriel = (n, un, plusieurs) => `${nombre.format(n)} ${n > 1 ? plusieurs : un}`

/**
 * Actions de l'administration (diagramme de composants : TableauSynchronisation et
 * FormulaireCorrectionLieu passent par ce hook, qui seul parle à AdminApi et à useNotification).
 *
 * Chaque composant qui l'appelle a ses propres états : on n'utilise que la partie qui le concerne.
 * - Synchronisation : chargerJournaux, lancerSynchronisation, journaux, erreurJournaux, enCours.
 * - Correction des lieux : rechercherLieux, corrigerLieu, changerVisibilite, enregistrement, visibiliteEnCours.
 *
 * Les actions préviennent elles-mêmes la personne (notification) ; les composants n'ont à s'occuper
 * que de l'affichage. En cas d'échec, elles renvoient null (ou []) au lieu de lever une exception.
 */
export default function useAdministration() {
  const { notifier } = useNotification()
  const [journaux, setJournaux] = useState(null) // null tant qu'ils ne sont pas chargés
  const [erreurJournaux, setErreurJournaux] = useState(false)
  const [enCours, setEnCours] = useState(false) // synchronisation en cours
  const [enregistrement, setEnregistrement] = useState(false)
  const [visibiliteEnCours, setVisibiliteEnCours] = useState(false)

  // ---------- Synchronisation ----------

  /** Charge les journaux (du plus récent au plus ancien). Renvoie la liste, ou null si le service est indisponible. */
  const chargerJournaux = useCallback(async () => {
    setErreurJournaux(false)
    try {
      const liste = await AdminApi.listerJournaux()
      setJournaux(liste)
      return liste
    } catch {
      setErreurJournaux(true)
      return null
    }
  }, [])

  /** Lance une synchronisation de toutes les sources ; les nouveaux journaux s'ajoutent en tête. */
  const lancerSynchronisation = useCallback(async function lancer() {
    setEnCours(true)
    try {
      const nouveaux = await AdminApi.lancerSynchronisation()
      setJournaux((j) => [...nouveaux, ...(j ?? [])])
      const aVerifier = nouveaux.filter((n) => n.statut !== 'SUCCES').length
      notifier(aVerifier
        ? `Synchronisation terminée · ${pluriel(aVerifier, 'source à vérifier', 'sources à vérifier')}`
        : `Synchronisation terminée pour les ${nouveaux.length} sources`)
      return nouveaux
    } catch {
      notifier('La synchronisation n’a pas pu démarrer. Aucune donnée modifiée.', {
        erreur: true,
        action: { libelle: 'Réessayer', action: lancer },
      })
      return null
    } finally {
      setEnCours(false)
    }
  }, [notifier])

  // ---------- Correction des lieux ----------

  /** Lieux dont le nom contient le texte (masqués compris). Liste vide si la recherche échoue. */
  const rechercherLieux = useCallback(async (texte) => {
    try {
      return await AdminApi.rechercherLieux(texte)
    } catch {
      return []
    }
  }, [])

  /** Enregistre les corrections d'un lieu. Renvoie le lieu corrigé, ou null en cas d'échec (les saisies restent à l'écran). */
  const corrigerLieu = useCallback(async (lieu) => {
    setEnregistrement(true)
    try {
      const corrige = await AdminApi.corrigerLieu(lieu)
      notifier(`Corrections enregistrées pour « ${corrige.nom} »`)
      return corrige
    } catch {
      notifier('Les corrections n’ont pas pu être enregistrées. Vos saisies restent dans le formulaire.', { erreur: true })
      return null
    } finally {
      setEnregistrement(false)
    }
  }, [notifier])

  /** Masque (ou réaffiche) un lieu. Renvoie le lieu mis à jour, ou null en cas d'échec. */
  const changerVisibilite = useCallback(async (lieu, masque) => {
    setVisibiliteEnCours(true)
    try {
      const misAJour = await AdminApi.changerVisibilite(lieu.id, masque)
      notifier(masque ? `« ${lieu.nom} » est masqué` : `« ${lieu.nom} » est de nouveau visible`)
      return misAJour
    } catch {
      notifier('La visibilité n’a pas pu être modifiée.', { erreur: true })
      return null
    } finally {
      setVisibiliteEnCours(false)
    }
  }, [notifier])

  return {
    journaux, erreurJournaux, enCours, chargerJournaux, lancerSynchronisation,
    enregistrement, visibiliteEnCours, rechercherLieux, corrigerLieu, changerVisibilite,
  }
}
