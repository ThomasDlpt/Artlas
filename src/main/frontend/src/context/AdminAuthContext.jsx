import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { definirJeton } from '../services/ApiClient.js'
import * as AuthAdminApi from '../services/AuthAdminApi.js'

const AdminAuthContext = createContext(null)
const CLE = 'artlas.sessionAdmin'
const INACTIVITE_MAX = 30 * 60 * 1000 // déconnexion après 30 min d'inactivité

function lireSession() {
  try {
    const s = JSON.parse(sessionStorage.getItem(CLE))
    return s && s.expiration > Date.now() ? s : null
  } catch { return null }
}
function ecrireSession(s) {
  try {
    if (s) sessionStorage.setItem(CLE, JSON.stringify(s))
    else sessionStorage.removeItem(CLE)
  } catch { /* stockage indisponible : la session ne survivra pas au rechargement */ }
}

/**
 * Session administrateur (V1 : un seul mot de passe, défini côté serveur).
 * Le front ne fait que masquer les pages admin : la vraie protection est sur le backend.
 */
export function AdminAuthProvider({ children }) {
  const [session, setSession] = useState(lireSession)

  useEffect(() => {
    definirJeton(session?.jeton ?? null)
    ecrireSession(session)
  }, [session])

  const connecter = useCallback(async (motDePasse) => {
    const s = await AuthAdminApi.connexion(motDePasse) // lève MotDePasseIncorrect si refusé
    setSession({ jeton: s.jeton, expiration: Date.now() + INACTIVITE_MAX })
  }, [])

  const deconnecter = useCallback(() => {
    AuthAdminApi.deconnexion()
    setSession(null)
  }, [])

  // Inactivité : chaque action repousse l'expiration ; on vérifie toutes les 30 s
  useEffect(() => {
    if (!session) return
    let derniere = Date.now()
    const activite = () => { derniere = Date.now() }
    const evenements = ['pointerdown', 'keydown', 'scroll']
    evenements.forEach((e) => window.addEventListener(e, activite, { passive: true }))
    const minuteur = setInterval(() => {
      if (Date.now() - derniere > INACTIVITE_MAX) setSession(null)
      else ecrireSession({ ...session, expiration: derniere + INACTIVITE_MAX })
    }, 30000)
    return () => {
      evenements.forEach((e) => window.removeEventListener(e, activite))
      clearInterval(minuteur)
    }
  }, [session])

  const valeur = useMemo(() => ({ estConnecte: Boolean(session), connecter, deconnecter }), [session, connecter, deconnecter])
  return <AdminAuthContext.Provider value={valeur}>{children}</AdminAuthContext.Provider>
}

export default function useAdminAuth() {
  return useContext(AdminAuthContext)
}
