import { useEffect, useMemo, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import CarteLeaflet from '../components/carte/CarteLeaflet.jsx'
import Icone from '../components/Icone.jsx'
import Logo from '../components/Logo.jsx'
import * as CircuitApi from '../services/CircuitApi.js'
import useCircuit from '../context/CircuitContext.jsx'
import useNotification from '../context/NotificationContext.jsx'
import useMobile from '../hooks/useMobile.js'
import { libelleType } from '../data/referentiels.js'
import { formatDuree, formatKm } from '../data/format.js'
import { coordonnees } from '../data/coordonnees.js'
import '../components/circuit/PanneauCircuit.css' // récapitulatif et numéros d'étape
import './PageCircuitPartage.css'

const LARGEUR_PANNEAU = 400
const HAUTEUR_VOLET_MOBILE = 0.6 // le panneau devient un volet en bas, sur 60 % de la hauteur (cf. CSS)
const FORMAT_CODE = /^[A-Z0-9]{8}$/

const ICONE_CARTE = (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
    <path d="M3.5 6.5 9 4l6 2.5L20.5 4v13.5L15 20l-6-2.5-5.5 2.5z" /><path d="M9 4v13.5M15 6.5V20" />
  </svg>
)

/** Route /circuit/:codePartage — un circuit partagé, en lecture seule. */
export default function PageCircuitPartage() {
  const { codePartage = '' } = useParams()
  const code = codePartage.toUpperCase()
  const [etat, setEtat] = useState({ statut: 'chargement', circuit: null }) // chargement | ok | introuvable | erreur
  const [essai, setEssai] = useState(0)

  useEffect(() => {
    let actif = true
    setEtat({ statut: 'chargement', circuit: null })
    if (!FORMAT_CODE.test(code)) {
      setEtat({ statut: 'introuvable', circuit: null })
      return
    }
    CircuitApi.chargerParCode(code)
      .then((c) => actif && setEtat(c ? { statut: 'ok', circuit: c } : { statut: 'introuvable', circuit: null }))
      .catch((e) => actif && setEtat({ statut: e?.statut === 404 ? 'introuvable' : 'erreur', circuit: null }))
    return () => { actif = false }
  }, [code, essai])

  if (etat.statut === 'introuvable') return <CircuitIntrouvable code={code} />

  return (
    <main className="page-partage">
      <CircuitEnLecture circuit={etat.circuit} statut={etat.statut} code={code} surReessayer={() => setEssai((n) => n + 1)} />
      <Link to="/" className="page-partage__explorer">{ICONE_CARTE}Explorer la carte</Link>
    </main>
  )
}

function CircuitEnLecture({ circuit, statut, code, surReessayer }) {
  const navigate = useNavigate()
  const { remplacerPar } = useCircuit()
  const { notifier } = useNotification()
  const mobile = useMobile()

  const etapes = circuit?.etapes ?? []
  const lieux = useMemo(() => etapes.map((e) => e.lieu), [etapes])
  // Une étape sans adresse reste dans la liste, mais n'entre pas dans le cadrage de la carte
  const cadrage = useMemo(() => lieux.map(coordonnees).filter(Boolean), [lieux])
  const numeroEtape = useMemo(() => {
    const index = new Map(etapes.map((e, i) => [e.lieu.id, i + 1]))
    return (id) => index.get(id) ?? null
  }, [etapes])

  const dupliquer = () => {
    remplacerPar(circuit)
    navigate('/', { state: { panneau: 'circuit' } })
  }

  const copierLien = async () => {
    try {
      await navigator.clipboard.writeText(window.location.href)
      notifier('Lien copié')
    } catch {
      notifier(`Lien : ${window.location.href}`)
    }
  }

  const departement = etapes[0]?.lieu.adresse?.commune?.departement

  return (
    <>
      <CarteLeaflet
        lieux={lieux}
        numeroEtape={numeroEtape}
        trace={circuit?.trace}
        cadrage={cadrage}
        margeGauche={mobile ? 0 : LARGEUR_PANNEAU + 16}
        partieBasCachee={mobile ? HAUTEUR_VOLET_MOBILE : 0}
        regrouper={false}
        controles="zoom"
      />

      <aside className="atl-partage" aria-label="Circuit partagé" aria-busy={statut === 'chargement'}>
        <div className="atl-partage__haut">
          <Logo pastille={false} />
          <span className="atl-partage__badge"><Icone nom="share" taille={14} />Circuit partagé</span>
        </div>

        {statut === 'erreur' && (
          <div className="atl-partage__message" role="alert">
            <p className="atl-partage__message-titre">Service indisponible, réessayez plus tard</p>
            <p>Nous n’arrivons pas à charger ce circuit pour le moment.</p>
            <button type="button" className="atl-btn atl-btn--primary atl-btn--sm" onClick={surReessayer}>Réessayer</button>
          </div>
        )}

        {statut === 'chargement' && (
          <div className="atl-partage__entete">
            <span className="atl-fantome" style={{ width: '80%', height: 24 }} />
            <span className="atl-fantome" style={{ width: '95%', marginTop: 10 }} />
            <span className="atl-fantome" style={{ width: '40%', marginTop: 10 }} />
            <span className="atl-fantome" style={{ width: '100%', height: 64, marginTop: 20, borderRadius: 12 }} />
          </div>
        )}

        {statut === 'ok' && (
          <>
            <div className="atl-partage__entete">
              <h1>{circuit.nom}</h1>
              {circuit.description && <p className="atl-partage__desc">{circuit.description}</p>}
              <p className="atl-partage__code">
                Code <span className="tabular">{code}</span> · en lecture seule
              </p>
            </div>

            <div className="atl-recap atl-partage__recap" role="group" aria-label="Récapitulatif">
              <div><span className="atl-recap__libelle">Distance</span><span className="atl-recap__valeur tabular">{formatKm(circuit.distanceKm)}</span></div>
              <div><span className="atl-recap__libelle">Durée</span><span className="atl-recap__valeur tabular">{formatDuree(circuit.dureeMinutes)}</span></div>
              <div><span className="atl-recap__libelle">Départ</span><span className="atl-recap__valeur atl-recap__valeur--texte">{circuit.communeDepart ?? '—'}</span></div>
            </div>

            <ol className="atl-partage__etapes" aria-label="Étapes">
              {etapes.map((e, i) => {
                const troncon = circuit.troncons?.[i]
                return (
                  <li key={e.lieu.id}>
                    <div className="atl-partage__etape">
                      <span className="atl-etape__numero tabular" aria-hidden="true">{i + 1}</span>
                      <span className="atl-partage__etape-texte">
                        <span className="atl-partage__etape-nom"><span className="atl-sr-only">Étape {i + 1} : </span>{e.lieu.nom}</span>
                        <span className="atl-partage__etape-meta">{[libelleType(e.lieu.type), e.lieu.labels?.[0]].filter(Boolean).join(' · ')}</span>
                      </span>
                      <span className="atl-partage__etape-duree tabular">{formatDuree(e.dureeVisiteMin)}</span>
                    </div>
                    {i < etapes.length - 1 && troncon && (
                      <p className="atl-partage__troncon tabular">
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                          <circle cx="13" cy="4.5" r="1.6" /><path d="m10 21 2-6 2.5 2.5V21" /><path d="m8 11 3-3.5h2.5l2 3.5 2.5 1" />
                        </svg>
                        {formatKm(troncon.distanceKm)} · {formatDuree(troncon.dureeMarcheMin)} à pied
                      </p>
                    )}
                  </li>
                )
              })}
            </ol>

            {circuit.dureeVisitesMin != null && (
              <p className="atl-partage__note">
                {formatDuree(circuit.dureeVisitesMin)} de visite et {formatDuree(circuit.dureeMarcheMin)} de marche
                {departement ? ` · ${departement.nom}${departement.region ? `, ${departement.region.nom}` : ''}` : ''}
              </p>
            )}

            <div className="atl-partage__espace" />

            <div className="atl-partage__actions">
              <button type="button" className="atl-btn atl-btn--primary" onClick={dupliquer}>
                <Icone nom="copy" taille={18} />Dupliquer et modifier
              </button>
              <button type="button" className="atl-btn atl-btn--secondaire" onClick={copierLien}>
                <Icone nom="share" taille={18} />Copier le lien
              </button>
              <p>La copie devient votre circuit ; l’original ne bouge pas.</p>
            </div>
          </>
        )}
      </aside>
    </>
  )
}

function CircuitIntrouvable({ code }) {
  const navigate = useNavigate()
  const [saisie, setSaisie] = useState('')
  const [invalide, setInvalide] = useState(false)

  const ouvrir = (e) => {
    e.preventDefault()
    const c = saisie.trim().toUpperCase()
    if (!FORMAT_CODE.test(c)) {
      setInvalide(true)
      return
    }
    navigate(`/circuit/${c}`)
  }

  return (
    <div className="page-introuvable">
      <div className="page-introuvable__fond" aria-hidden="true" />
      <div className="page-introuvable__logo"><Logo symbole={false} /></div>
      <main className="page-introuvable__carte">
        <h1>Ce circuit est introuvable</h1>
        <p>
          Aucun circuit ne correspond au code <span className="tabular page-introuvable__code">{code || '—'}</span>.
          Le lien a peut-être été mal copié : vérifiez-le, ou saisissez le code ci-dessous.
        </p>
        <form onSubmit={ouvrir} noValidate>
          <label htmlFor="code">Code de partage</label>
          <div className={'page-introuvable__champ' + (invalide ? ' page-introuvable__champ--invalide' : '')}>
            <input
              id="code"
              className="tabular"
              maxLength={8}
              autoComplete="off"
              placeholder="K7PQ2MXA"
              value={saisie}
              aria-invalid={invalide}
              aria-describedby="code-aide"
              onChange={(e) => { setSaisie(e.target.value.toUpperCase()); setInvalide(false) }}
            />
            <button type="submit" className="atl-btn atl-btn--primary">Ouvrir</button>
          </div>
          <p id="code-aide" className={invalide ? 'page-introuvable__aide--erreur' : undefined} role={invalide ? 'alert' : undefined}>
            {invalide ? 'Le code fait 8 lettres ou chiffres.' : '8 lettres ou chiffres'}
          </p>
        </form>
        <Link to="/" className="atl-btn atl-btn--ghost atl-btn--sm">{ICONE_CARTE}Explorer la carte</Link>
      </main>
    </div>
  )
}
