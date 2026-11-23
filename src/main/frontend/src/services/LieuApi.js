import { get } from './ApiClient.js'
import { LIEUX_FICTIFS } from '../data/lieuxFictifs.js'

// Tant que le backend n'expose pas d'API REST, on travaille sur des données fictives.
// Passer VITE_USE_MOCK=false (fichier .env.local) pour appeler le vrai backend.
const MOCK = import.meta.env.VITE_USE_MOCK !== 'false'

function correspond(lieu, filtre) {
  if (lieu.masque) return false // masqué par un administrateur
  if (!filtre) return true
  const { texte, types = [], codeDepartement, label } = filtre
  if (types.length && !types.includes(lieu.type)) return false
  if (codeDepartement && lieu.adresse.commune.departement.code !== codeDepartement) return false
  if (label && !lieu.labels.includes(label)) return false
  if (texte && !lieu.nom.toLowerCase().includes(texte.toLowerCase())) return false
  return true
}

const attendre = (ms) => new Promise((r) => setTimeout(r, ms))

// Simulation : en vrai, ce sont les départements des lieux importés de Basilic (champs N_Département / Département).
const DEPARTEMENTS_FICTIFS = [
  { code: '22', nom: 'Côtes-d’Armor' },
  { code: '29', nom: 'Finistère' },
  { code: '35', nom: 'Ille-et-Vilaine' },
  { code: '44', nom: 'Loire-Atlantique' },
  { code: '49', nom: 'Maine-et-Loire' },
  { code: '50', nom: 'Manche' },
  { code: '53', nom: 'Mayenne' },
  { code: '56', nom: 'Morbihan' },
  { code: '72', nom: 'Sarthe' },
  { code: '85', nom: 'Vendée' },
]

/** Départements connus de la base, triés par code : [{ code, nom }]. */
export async function listerDepartements() {
  if (MOCK) {
    await attendre(100)
    return DEPARTEMENTS_FICTIFS
  }
  return get('/departements')
}

export async function rechercher(filtre) {
  if (MOCK) {
    await attendre(150)
    return LIEUX_FICTIFS.filter((l) => correspond(l, filtre))
  }
  return get('/lieux', filtre)
}

export async function rechercherDansZone(bbox, filtre) {
  if (MOCK) {
    await attendre(150)
    return LIEUX_FICTIFS.filter((l) => {
      const { latitude: lat, longitude: lon } = l.adresse
      return lat >= bbox.latMin && lat <= bbox.latMax && lon >= bbox.lonMin && lon <= bbox.lonMax && correspond(l, filtre)
    })
  }
  return get('/lieux/zone', { ...bbox, ...filtre })
}

export async function selectionner(idLieu) {
  if (MOCK) {
    await attendre(100)
    return LIEUX_FICTIFS.find((l) => l.id === idLieu && !l.masque) ?? null
  }
  try {
    return await get(`/lieux/${encodeURIComponent(idLieu)}`)
  } catch (e) {
    if (e?.statut === 404) return null // lieu inconnu ou masqué : même réponse qu'en simulation
    throw e
  }
}
