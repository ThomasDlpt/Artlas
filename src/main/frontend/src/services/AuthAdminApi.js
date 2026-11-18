import { post } from './ApiClient.js'

// V1 : un seul mot de passe administrateur, vérifié par le serveur.
// En simulation, le mot de passe est « artlas2026 » (celui de la maquette).
const MOCK = import.meta.env.VITE_USE_MOCK !== 'false'
const MDP_FICTIF = 'artlas2026'
const attendre = (ms) => new Promise((r) => setTimeout(r, ms))

export class MotDePasseIncorrect extends Error {}

/** Renvoie une session { jeton, expiration } ou lève MotDePasseIncorrect. */
export async function connexion(motDePasse) {
  if (MOCK) {
    await attendre(400)
    if (motDePasse !== MDP_FICTIF) throw new MotDePasseIncorrect()
    return { jeton: 'jeton-fictif-' + Date.now(), expiration: Date.now() + 30 * 60 * 1000 }
  }
  try {
    return await post('/admin/connexion', { motDePasse })
  } catch (e) {
    if (e.statut === 401 || e.statut === 403) throw new MotDePasseIncorrect()
    throw e
  }
}

export async function deconnexion() {
  if (MOCK) return
  await post('/admin/deconnexion', {}).catch(() => {})
}
