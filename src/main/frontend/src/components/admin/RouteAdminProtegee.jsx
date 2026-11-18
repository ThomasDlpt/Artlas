import { Navigate, useLocation } from 'react-router-dom'
import useAdminAuth from '../../context/AdminAuthContext.jsx'

// Le front ne fait que masquer la page : /api/admin/** refuse toute requête sans jeton valide.
export default function RouteAdminProtegee({ children }) {
  const { estConnecte } = useAdminAuth()
  const location = useLocation()
  if (!estConnecte) return <Navigate to="/admin/connexion" replace state={{ depuis: location.pathname }} />
  return children
}
