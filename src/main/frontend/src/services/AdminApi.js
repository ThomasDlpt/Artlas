import { get, post, put } from './ApiClient.js'
import { LIEUX_FICTIFS } from '../data/lieuxFictifs.js'

// Toutes ces routes sont protégées côté serveur (/api/admin/** refuse les requêtes sans jeton valide).
const MOCK = import.meta.env.VITE_USE_MOCK !== 'false'
const attendre = (ms) => new Promise((r) => setTimeout(r, ms))

// ---------- Simulation (à supprimer quand l'API existera) ----------
// Mêmes champs que backend.synchronisation.JournalSynchro (+ nbMisesAJour, attendu par la maquette)
let journaux = [
  { id: 'dt-11', source: 'DATATOURISME', dateSynchro: '2026-11-11T09:14:00', nbAjouts: 37, nbMisesAJour: 402, nbRejets: 6, nbLignesLues: 4120, statut: 'PARTIEL',
    erreurs: ['Ligne 1 284 : Code postal manquant pour « Chapelle de Bonne-Nouvelle »', 'Ligne 2 017 : Coordonnées hors de France (id DT-88412)',
      'Ligne 2 940 : Type « PATRIMOINE_IMMATERIEL » inconnu, lieu ignoré', '3 autres : Identifiants externes en double'] },
  { id: 'b-11', source: 'BASILIC', dateSynchro: '2026-11-11T09:12:00', nbAjouts: 12, nbMisesAJour: 148, nbRejets: 0, nbLignesLues: 2310, statut: 'SUCCES', erreurs: [] },
  { id: 'dt-04', source: 'DATATOURISME', dateSynchro: '2026-11-04T09:10:00', nbAjouts: 0, nbMisesAJour: 0, nbRejets: 0, nbLignesLues: 0, statut: 'ECHEC',
    erreurs: ['9 h 10 : Le flux n’a pas répondu en 60 s. Rien n’a été modifié.'] },
  { id: 'b-04', source: 'BASILIC', dateSynchro: '2026-11-04T09:08:00', nbAjouts: 3, nbMisesAJour: 96, nbRejets: 1, nbLignesLues: 2298, statut: 'PARTIEL',
    erreurs: ['Ligne 412 : Nom vide (id B-0098812)'] },
]
const copie = (o) => JSON.parse(JSON.stringify(o))
// ----------------------------------------------------------------------

/** Journaux de synchronisation, du plus récent au plus ancien. */
export async function listerJournaux() {
  if (MOCK) {
    await attendre(300)
    return copie(journaux)
  }
  return get('/admin/synchronisations')
}

/** Lance une synchronisation de toutes les sources ; renvoie les nouveaux journaux. */
export async function lancerSynchronisation() {
  if (MOCK) {
    await attendre(1800)
    const maintenant = new Date().toISOString().slice(0, 19)
    const nouveaux = [
      { id: 'dt-' + Date.now(), source: 'DATATOURISME', dateSynchro: maintenant, nbAjouts: 4, nbMisesAJour: 57, nbRejets: 0, nbLignesLues: 4124, statut: 'SUCCES', erreurs: [] },
      { id: 'b-' + Date.now(), source: 'BASILIC', dateSynchro: maintenant, nbAjouts: 1, nbMisesAJour: 22, nbRejets: 0, nbLignesLues: 2311, statut: 'SUCCES', erreurs: [] },
    ]
    journaux = [...nouveaux, ...journaux]
    return copie(nouveaux)
  }
  return post('/admin/synchronisations', {})
}

/** Recherche d'un lieu à corriger (lieux masqués compris). */
export async function rechercherLieux(texte) {
  if (MOCK) {
    await attendre(150)
    const t = texte.trim().toLowerCase()
    if (!t) return []
    return copie(LIEUX_FICTIFS.filter((l) => l.nom.toLowerCase().includes(t)).slice(0, 8))
  }
  return get('/admin/lieux', { texte })
}

/** Enregistre les corrections d'un lieu ; elles passent avant la source jusqu'à sa prochaine modification. */
export async function corrigerLieu(lieu) {
  if (MOCK) {
    await attendre(500)
    const i = LIEUX_FICTIFS.findIndex((l) => l.id === lieu.id)
    if (i < 0) throw new Error('Lieu inconnu')
    LIEUX_FICTIFS[i] = { ...LIEUX_FICTIFS[i], ...copie(lieu), dateMiseAJour: new Date().toISOString().slice(0, 10) }
    return copie(LIEUX_FICTIFS[i])
  }
  return put(`/admin/lieux/${encodeURIComponent(lieu.id)}`, lieu)
}

/** Masque (ou réaffiche) un lieu : il disparaît de la carte, des résultats et des circuits partagés. */
export async function changerVisibilite(idLieu, masque) {
  if (MOCK) {
    await attendre(400)
    const lieu = LIEUX_FICTIFS.find((l) => l.id === idLieu)
    if (lieu) lieu.masque = masque
    return copie(lieu)
  }
  return put(`/admin/lieux/${encodeURIComponent(idLieu)}/visibilite`, { masque })
}
