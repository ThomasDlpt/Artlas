// Client HTTP unique : tous les services passent par lui.
export class ErreurApi extends Error {
  constructor(statut, message) {
    super(message)
    this.statut = statut
  }
}

const URL_BASE = import.meta.env.VITE_API_URL ?? '/api'
let jeton = null

export function definirJeton(valeur) {
  jeton = valeur
}

async function requete(methode, chemin, { params, corps } = {}) {
  const url = new URL(URL_BASE + chemin, window.location.origin)
  Object.entries(params ?? {}).forEach(([cle, valeur]) => {
    if (valeur === undefined || valeur === null || valeur === '') return
    if (Array.isArray(valeur)) valeur.forEach((v) => url.searchParams.append(cle, v))
    else url.searchParams.set(cle, valeur)
  })

  let reponse
  try {
    reponse = await fetch(url, {
      method: methode,
      headers: {
        ...(corps ? { 'Content-Type': 'application/json' } : {}),
        ...(jeton ? { Authorization: `Bearer ${jeton}` } : {}),
      },
      body: corps ? JSON.stringify(corps) : undefined,
    })
  } catch {
    throw new ErreurApi(0, 'Service indisponible')
  }
  if (!reponse.ok) throw new ErreurApi(reponse.status, await reponse.text())
  return reponse.status === 204 ? null : reponse.json()
}

export const get = (chemin, params) => requete('GET', chemin, { params })
export const post = (chemin, corps) => requete('POST', chemin, { corps })
export const put = (chemin, corps) => requete('PUT', chemin, { corps })
export const del = (chemin) => requete('DELETE', chemin)
