import { Routes, Route } from 'react-router-dom'
import PageCarte from './pages/PageCarte.jsx'
import PageCircuitPartage from './pages/PageCircuitPartage.jsx'
import PageConnexionAdmin from './pages/PageConnexionAdmin.jsx'
import PageAdmin from './pages/PageAdmin.jsx'
import RouteAdminProtegee from './components/admin/RouteAdminProtegee.jsx'

// Routes prévues (voir docs/design/README.md) :
//   /                      PageCarte
//   /circuit/:codePartage  PageCircuitPartage
//   /admin/connexion       PageConnexionAdmin
//   /admin                 PageAdmin (protégée)
export default function App() {
  return (
    <Routes>
      <Route path="/" element={<PageCarte />} />
      <Route path="/circuit/:codePartage" element={<PageCircuitPartage />} />
      <Route path="/admin/connexion" element={<PageConnexionAdmin />} />
      <Route path="/admin" element={<RouteAdminProtegee><PageAdmin /></RouteAdminProtegee>} />
      <Route path="*" element={<p style={{ padding: 24 }}>Page introuvable.</p>} />
    </Routes>
  )
}
