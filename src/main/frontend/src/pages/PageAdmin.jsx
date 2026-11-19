import { Link, useNavigate } from 'react-router-dom'
import Logo from '../components/Logo.jsx'
import TableauSynchronisation from '../components/admin/TableauSynchronisation.jsx'
import FormulaireCorrectionLieu from '../components/admin/FormulaireCorrectionLieu.jsx'
import useAdminAuth from '../context/AdminAuthContext.jsx'
import './admin.css'

/** Route /admin (protégée) — synchronisation des sources et correction des lieux. */
export default function PageAdmin() {
  const { deconnecter } = useAdminAuth()
  const navigate = useNavigate()

  return (
    <div className="page-admin">
      <header className="admin-entete">
        <Logo pastille={false} symbole={false} />
        <span className="admin-entete__badge">Administration</span>
        <div className="admin-entete__droite">
          <Link to="/" className="atl-btn atl-btn--ghost atl-btn--sm">Voir la carte</Link>
          <button type="button" className="atl-btn atl-btn--secondaire atl-btn--sm" onClick={() => { deconnecter(); navigate('/admin/connexion') }}>
            Se déconnecter
          </button>
        </div>
      </header>

      <main className="admin-contenu">
        <TableauSynchronisation />
        <FormulaireCorrectionLieu />
      </main>
    </div>
  )
}
