import { useEffect, useRef, useState } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import Logo from '../components/Logo.jsx'
import useAdminAuth from '../context/AdminAuthContext.jsx'
import { MotDePasseIncorrect } from '../services/AuthAdminApi.js'
import './admin.css'

/** Route /admin/connexion — un seul champ « Mot de passe ». */
export default function PageConnexionAdmin() {
  const { estConnecte, connecter } = useAdminAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [motDePasse, setMotDePasse] = useState('')
  const [visible, setVisible] = useState(false)
  const [erreur, setErreur] = useState(null) // null | 'incorrect' | 'service'
  const [envoi, setEnvoi] = useState(false)
  const champ = useRef(null)

  useEffect(() => { champ.current?.focus() }, [])

  if (estConnecte) return <Navigate to={location.state?.depuis ?? '/admin'} replace />

  const envoyer = async (e) => {
    e.preventDefault()
    setEnvoi(true)
    setErreur(null)
    try {
      await connecter(motDePasse)
      navigate(location.state?.depuis ?? '/admin', { replace: true })
    } catch (err) {
      setErreur(err instanceof MotDePasseIncorrect ? 'incorrect' : 'service')
      champ.current?.focus()
    } finally {
      setEnvoi(false)
    }
  }

  return (
    <div className="page-admin page-admin--connexion">
      <header className="admin-entete">
        <Logo pastille={false} symbole={false} />
        <Link to="/" className="atl-btn atl-btn--ghost atl-btn--sm admin-entete__droite">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><path d="M19 12H5M11 6l-6 6 6 6" /></svg>
          Retour à la carte
        </Link>
      </header>

      <main className="admin-connexion">
        <form className="admin-connexion__carte" aria-labelledby="titre" onSubmit={envoyer} noValidate>
          <div className="admin-connexion__titre">
            <h1 id="titre">Espace administrateur</h1>
            <p>Synchroniser les sources et corriger les fiches.</p>
          </div>

          <div>
            <label htmlFor="mdp" className="admin-libelle">Mot de passe</label>
            <div className={'admin-mdp' + (erreur === 'incorrect' ? ' admin-mdp--erreur' : '')}>
              <input
                id="mdp"
                ref={champ}
                type={visible ? 'text' : 'password'}
                autoComplete="current-password"
                value={motDePasse}
                onChange={(e) => { setMotDePasse(e.target.value); setErreur(null) }}
                aria-invalid={erreur === 'incorrect'}
                aria-describedby={erreur ? 'mdp-msg' : undefined}
              />
              <button type="button" className="atl-btn atl-btn--ghost atl-btn--sm" aria-pressed={visible} onClick={() => setVisible((v) => !v)}>
                {visible ? 'Masquer' : 'Afficher'}
              </button>
            </div>
            {erreur && (
              <p id="mdp-msg" role="alert" className="admin-erreur">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><circle cx="12" cy="12" r="8.5" /><path d="M12 7.5v5.5" /><circle cx="12" cy="16.3" r=".6" /></svg>
                {erreur === 'incorrect'
                  ? <span><b>Mot de passe incorrect.</b> Vérifiez les majuscules et réessayez.</span>
                  : <span><b>Service indisponible.</b> Réessayez dans un instant.</span>}
              </p>
            )}
          </div>

          <button type="submit" className="atl-btn atl-btn--primary" disabled={envoi || !motDePasse}>
            {envoi ? 'Connexion…' : 'Se connecter'}
          </button>
          <p className="admin-connexion__note">Réservé à l’équipe Artlas · déconnexion après 30 min d’inactivité</p>
        </form>
      </main>
    </div>
  )
}
