import { createContext, useCallback, useContext, useEffect, useRef, useState } from 'react'
import Icone from '../components/Icone.jsx'
import './ZoneNotifications.css'

const NotificationContext = createContext(null)

/**
 * Une seule notification à la fois, en pilule sombre en bas au centre.
 * Disparaît après 4 s, sauf une erreur. Action facultative : { libelle, action }.
 */
export function NotificationProvider({ children }) {
  const [notif, setNotif] = useState(null)
  const minuteur = useRef(null)

  const fermer = useCallback(() => setNotif(null), [])

  const notifier = useCallback((message, { erreur = false, action = null } = {}) => {
    setNotif({ message, erreur, action, cle: Date.now() })
  }, [])

  useEffect(() => {
    clearTimeout(minuteur.current)
    if (notif && !notif.erreur) minuteur.current = setTimeout(() => setNotif(null), 4000)
    return () => clearTimeout(minuteur.current)
  }, [notif])

  return (
    <NotificationContext.Provider value={{ notifier, fermer }}>
      {children}
      <div className="atl-notifs" aria-live="polite">
        {notif && (
          <div key={notif.cle} className="atl-notif" role={notif.erreur ? 'alert' : 'status'}>
            <span>{notif.message}</span>
            {notif.action && (
              <button type="button" className="atl-notif__action" onClick={() => { notif.action.action(); fermer() }}>
                {notif.action.libelle}
              </button>
            )}
            <button type="button" className="atl-notif__fermer" aria-label="Fermer" onClick={fermer}>
              <Icone nom="close" taille={16} />
            </button>
          </div>
        )}
      </div>
    </NotificationContext.Provider>
  )
}

export default function useNotification() {
  return useContext(NotificationContext)
}
