import { get, post, put } from './ApiClient.js'
import { LIEUX_FICTIFS } from '../data/lieuxFictifs.js'

// Tant que le backend n'expose pas d'API circuits, on simule ses réponses.
// Le front ne calcule rien de métier : en vrai, distances, durées et code de partage viennent du serveur.
const MOCK = import.meta.env.VITE_USE_MOCK !== 'false'
const attendre = (ms) => new Promise((r) => setTimeout(r, ms))

// ---------- Simulation (à supprimer quand l'API existera) ----------
const VITESSE_MARCHE_KMH = 5
const DETOUR = 1.3 // à pied, on ne marche pas à vol d'oiseau

function distanceKm(a, b) {
  const r = 6371
  const rad = (d) => (d * Math.PI) / 180
  const dLat = rad(b.latitude - a.latitude)
  const dLon = rad(b.longitude - a.longitude)
  const h = Math.sin(dLat / 2) ** 2 + Math.cos(rad(a.latitude)) * Math.cos(rad(b.latitude)) * Math.sin(dLon / 2) ** 2
  return 2 * r * Math.atan2(Math.sqrt(h), Math.sqrt(1 - h)) * DETOUR
}

function calculerFictif(circuit) {
  const { etapes } = circuit
  const troncons = etapes.slice(1).map((e, i) => {
    const km = distanceKm(etapes[i].lieu.adresse, e.lieu.adresse)
    return { distanceKm: Math.round(km * 10) / 10, dureeMarcheMin: Math.round((km / VITESSE_MARCHE_KMH) * 60) }
  })
  const marche = troncons.reduce((t, x) => t + x.dureeMarcheMin, 0)
  const visites = etapes.reduce((t, e) => t + Number(e.dureeVisiteMin || 0), 0)
  return {
    ...circuit,
    distanceKm: Math.round(troncons.reduce((t, x) => t + x.distanceKm, 0) * 10) / 10,
    dureeMinutes: visites + marche,
    dureeVisitesMin: visites,
    dureeMarcheMin: marche,
    communeDepart: etapes[0]?.lieu.adresse?.commune?.nom ?? null,
    troncons,
    trace: etapes.map((e) => [e.lieu.adresse.latitude, e.lieu.adresse.longitude]),
  }
}

const codeAleatoire = () =>
  Array.from({ length: 8 }, () => 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789'[Math.floor(Math.random() * 32)]).join('')

// Circuits sauvegardés, gardés dans le navigateur pour que les liens de partage marchent en développement.
// Un circuit d'exemple « K7PQ2MXA » (celui de la maquette) est toujours disponible.
const CLE_STOCKAGE = 'artlas.circuitsFictifs'
function lireSauvegardes() {
  try { return JSON.parse(localStorage.getItem(CLE_STOCKAGE)) ?? {} } catch { return {} }
}
function ecrireSauvegarde(circuit) {
  try {
    localStorage.setItem(CLE_STOCKAGE, JSON.stringify({ ...lireSauvegardes(), [circuit.codePartage]: circuit }))
  } catch { /* stockage indisponible : le lien ne marchera que dans cet onglet */ }
}
const lieuFictif = (id) => LIEUX_FICTIFS.find((l) => l.id === id)
const EXEMPLE = {
  id: 'exemple',
  codePartage: 'K7PQ2MXA',
  nom: 'Rennes, cœur historique',
  description: 'Du Parlement aux quais de la Vilaine, une demi-journée à pied.',
  etapes: [
    { numOrdre: 1, dureeVisiteMin: 45, lieu: lieuFictif('l1') },
    { numOrdre: 2, dureeVisiteMin: 60, lieu: lieuFictif('l2') },
  ],
}
const sauvegardes = new Map()
// ----------------------------------------------------------------------

/** Distances, durées, tronçons et tracé d'un circuit (sans l'enregistrer). */
export async function calculer(circuit) {
  if (MOCK) {
    await attendre(350)
    return calculerFictif(circuit)
  }
  return post('/circuits/calcul', circuit)
}

/** Enregistre un nouveau circuit ; renvoie le circuit avec son id et son code de partage. */
export async function sauvegarder(circuit) {
  if (MOCK) {
    await attendre(500)
    const enregistre = { ...calculerFictif(circuit), id: crypto.randomUUID(), codePartage: codeAleatoire() }
    sauvegardes.set(enregistre.codePartage, enregistre)
    ecrireSauvegarde(enregistre)
    return enregistre
  }
  return post('/circuits', circuit)
}

export async function modifier(circuit) {
  if (MOCK) return sauvegarder(circuit)
  return put(`/circuits/${circuit.id}`, circuit)
}

/** Circuit partagé, en lecture seule. Renvoie null si le code est inconnu. */
export async function chargerParCode(codePartage) {
  if (MOCK) {
    await attendre(300)
    if (codePartage === EXEMPLE.codePartage) return calculerFictif(EXEMPLE)
    return sauvegardes.get(codePartage) ?? lireSauvegardes()[codePartage] ?? null
  }
  return get(`/circuits/partage/${encodeURIComponent(codePartage)}`)
}
