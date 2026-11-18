import Icone from '../Icone.jsx'

// Aperçus de la vignette Plan / Satellite (repris de la maquette CarteDesktop.dc.html)
const APERCU_SATELLITE = (
  <svg width="68" height="68" viewBox="0 0 68 68" aria-hidden="true">
    <rect width="68" height="68" fill="#5f5b51" /><rect x="4" y="6" width="18" height="14" fill="#7a746a" />
    <rect x="40" y="4" width="22" height="16" fill="#847e73" /><rect x="8" y="40" width="16" height="10" fill="#6b655b" />
    <circle cx="52" cy="44" r="11" fill="#3d4a31" />
    <path d="M-2 34 C18 26 34 40 70 30" fill="none" stroke="#2c3d44" strokeWidth="5" />
    <path d="M28 0 L36 68" stroke="#a6a196" strokeWidth="3" />
  </svg>
)
const APERCU_PLAN = (
  <svg width="68" height="68" viewBox="0 0 68 68" aria-hidden="true">
    <rect width="68" height="68" fill="#f2f1ee" /><rect x="4" y="6" width="18" height="14" fill="#e8e6e1" />
    <circle cx="52" cy="44" r="11" fill="#e3ebdf" />
    <path d="M-2 34 C18 26 34 40 70 30" fill="none" stroke="#d4dadc" strokeWidth="5" />
    <path d="M28 0 L36 68" stroke="#ffffff" strokeWidth="4" /><path d="M0 14 L68 10" stroke="#ffffff" strokeWidth="3" />
  </svg>
)

/**
 * Bas droite : vignette Plan / Satellite, « Autour de moi », zoom, assistant (V2).
 * La vignette montre l'AUTRE vue et son nom.
 */
export default function ControlesCarte({ simple = false, fond, surChangerFond, surAutourDeMoi, localisation, surZoomer, surDezoomer }) {
  const autre = fond === 'plan' ? 'satellite' : 'plan'
  const libelleAutre = autre === 'plan' ? 'Plan' : 'Satellite'

  const zoom = (
    <div className="atl-zoom" role="group" aria-label="Zoom">
      <button type="button" aria-label="Zoomer" onClick={surZoomer}><Icone nom="plus" taille={20} /></button>
      <button type="button" aria-label="Dézoomer" onClick={surDezoomer}><Icone nom="minus" taille={20} /></button>
    </div>
  )
  if (simple) return <div className="atl-controles">{zoom}</div>

  return (
    <div className="atl-controles">
      <button
        type="button"
        className={'atl-fond atl-fond--' + autre}
        aria-label={`Passer en vue ${libelleAutre.toLowerCase()}`}
        title={`Vue ${libelleAutre.toLowerCase()}`}
        onClick={() => surChangerFond(autre)}
      >
        {autre === 'satellite' ? APERCU_SATELLITE : APERCU_PLAN}
        <span className="atl-fond__nom">
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
            <path d="m12 4 9 5-9 5-9-5z" /><path d="m3 14 9 5 9-5" />
          </svg>
          {libelleAutre}
        </span>
      </button>

      <button
        type="button"
        className="atl-controle-rond"
        aria-label="Autour de moi"
        title="Autour de moi"
        aria-busy={localisation}
        onClick={surAutourDeMoi}
      >
        <Icone nom="locate" taille={20} />
      </button>

      {zoom}

      <button type="button" className="atl-assistant" aria-disabled="true" title="Assistant · bientôt disponible">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
          <path d="M4.5 5.5h15a1 1 0 0 1 1 1v9a1 1 0 0 1-1 1H10l-4.5 3.5v-3.5h-1a1 1 0 0 1-1-1v-9a1 1 0 0 1 1-1z" /><path d="M8 10h8M8 13h5" />
        </svg>
        Assistant · bientôt
      </button>
    </div>
  )
}
