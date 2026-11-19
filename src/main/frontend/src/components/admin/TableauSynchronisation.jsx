import { Fragment, useEffect, useState } from 'react'
import useAdministration from '../../hooks/useAdministration.js'

const NOMS_SOURCES = { BASILIC: 'Basilic', DATATOURISME: 'DataTourisme' }
const nombre = new Intl.NumberFormat('fr-FR')

// Statut = point + phrase + précision (jamais la couleur seule, pas de badge)
const STATUTS = {
  SUCCES: { phrase: 'Terminé', teinte: 'success' },
  PARTIEL: { phrase: 'Terminé, à vérifier', teinte: 'warning' },
  ECHEC: { phrase: 'Interrompu', teinte: 'error' },
}

// « 11/11/2026 · 9 h 14 »
function formatDateHeure(iso) {
  const d = new Date(iso)
  const date = new Intl.DateTimeFormat('fr-FR', { day: '2-digit', month: '2-digit', year: 'numeric' }).format(d)
  return `${date} · ${d.getHours()} h ${String(d.getMinutes()).padStart(2, '0')}`
}

// « aujourd’hui à 9 h 14 », « hier à 9 h 14 » ou « le 04/11/2026 à 9 h 10 »
function dernierPassage(iso) {
  const d = new Date(iso)
  const heure = `${d.getHours()} h ${String(d.getMinutes()).padStart(2, '0')}`
  const jour = (x) => new Date(x.getFullYear(), x.getMonth(), x.getDate()).getTime()
  const ecart = Math.round((jour(new Date()) - jour(d)) / 86400000)
  if (ecart === 0) return `aujourd’hui à ${heure}`
  if (ecart === 1) return `hier à ${heure}`
  return `le ${new Intl.DateTimeFormat('fr-FR').format(d)} à ${heure}`
}

// « Ligne 1 284 : Code postal manquant… » -> { ref: 'Ligne 1 284', message: 'Code postal manquant…' }
function decouper(erreur) {
  const i = erreur.indexOf(' : ')
  return i > 0 ? { ref: erreur.slice(0, i), message: erreur.slice(i + 3) } : { ref: '', message: erreur }
}

const pluriel = (n, un, plusieurs) => `${nombre.format(n)} ${n > 1 ? plusieurs : un}`

export default function TableauSynchronisation() {
  const { journaux, erreurJournaux: erreur, enCours, chargerJournaux, lancerSynchronisation: lancer } = useAdministration()
  const [ouverts, setOuverts] = useState([])
  const [essai, setEssai] = useState(0)

  useEffect(() => {
    let actuel = true
    chargerJournaux().then((j) => {
      if (!actuel || !j) return
      // le premier journal à vérifier est déplié d'office
      const aVoir = j.find((x) => x.statut !== 'SUCCES')
      setOuverts(aVoir ? [aVoir.id] : [])
    })
    return () => { actuel = false }
  }, [chargerJournaux, essai])

  const basculer = (id) => setOuverts((o) => (o.includes(id) ? o.filter((x) => x !== id) : [...o, id]))

  return (
    <section className="admin-bloc" aria-labelledby="sync-titre">
      <div className="admin-bloc__entete">
        <div>
          <h1 id="sync-titre">Synchronisation des données</h1>
          <p>
            Basilic et DataTourisme
            {journaux?.length > 0 && <> · dernier passage <span className="tabular">{dernierPassage(journaux[0].dateSynchro)}</span></>}
          </p>
        </div>
        <button type="button" className="atl-btn atl-btn--primary" onClick={lancer} disabled={enCours}>
          <svg className={enCours ? 'admin-tourne' : undefined} width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
            <path d="M19.5 8.5A8 8 0 0 0 5 7.5" /><path d="M4.5 3.5v4h4" /><path d="M4.5 15.5A8 8 0 0 0 19 16.5" /><path d="M19.5 20.5v-4h-4" />
          </svg>
          {enCours ? 'Synchronisation en cours…' : 'Lancer une synchronisation'}
        </button>
      </div>

      {erreur ? (
        <div className="admin-message" role="alert">
          <p><b>Service indisponible, réessayez plus tard.</b> Les journaux n’ont pas pu être chargés.</p>
          <button type="button" className="atl-btn atl-btn--primary atl-btn--sm" onClick={() => setEssai((n) => n + 1)}>Réessayer</button>
        </div>
      ) : (
        <div className="admin-tableau-defile">
          <table className="admin-tableau" aria-busy={!journaux}>
            <caption className="atl-sr-only">Journaux de synchronisation</caption>
            <thead>
              <tr>
                <th scope="col">Date</th>
                <th scope="col">Source</th>
                <th scope="col" className="admin-num">Ajouts</th>
                <th scope="col" className="admin-num">Mises à jour</th>
                <th scope="col" className="admin-num">Rejets</th>
                <th scope="col">Statut</th>
                <th scope="col" className="admin-num"><span className="atl-sr-only">Détails</span></th>
              </tr>
            </thead>
            <tbody>
              {!journaux && [0, 1, 2].map((i) => (
                <tr key={i}><td colSpan={7}><span className="atl-fantome" style={{ height: 16, margin: '6px 0' }} /></td></tr>
              ))}
              {journaux?.length === 0 && (
                <tr><td colSpan={7} className="admin-vide">Aucune synchronisation pour l’instant.</td></tr>
              )}
              {journaux?.map((j) => {
                const s = STATUTS[j.statut] ?? STATUTS.ECHEC
                const echec = j.statut === 'ECHEC'
                const ouvert = ouverts.includes(j.id)
                const note = echec ? 'Aucune donnée modifiée' : j.nbRejets > 0 ? pluriel(j.nbRejets, 'ligne écartée', 'lignes écartées') : 'Rien à signaler'
                const val = (n) => (echec || n == null ? '—' : nombre.format(n))
                return (
                  <Fragment key={j.id}>
                    <tr className={ouvert ? 'admin-ligne--ouverte' : undefined}>
                      <td className="tabular admin-date">{formatDateHeure(j.dateSynchro)}</td>
                      <td className="admin-source">{NOMS_SOURCES[j.source] ?? j.source}</td>
                      <td className="admin-num tabular">{val(j.nbAjouts)}</td>
                      <td className="admin-num tabular">{val(j.nbMisesAJour)}</td>
                      <td className={'admin-num tabular' + (!echec && j.nbRejets > 0 ? ' admin-rejets' : ' admin-discret')}>{val(j.nbRejets)}</td>
                      <td>
                        <span className="admin-statut" title={`Statut API : ${j.statut}`}>
                          <span className={`admin-point admin-point--${j.statut.toLowerCase()}`} aria-hidden="true" />
                          <span>
                            <span className="admin-statut__phrase">{s.phrase}</span>
                            <span className="admin-statut__note">{note}</span>
                          </span>
                        </span>
                      </td>
                      <td className="admin-num">
                        {j.erreurs?.length > 0 && (
                          <button type="button" className="atl-btn atl-btn--ghost atl-btn--sm" aria-expanded={ouvert} onClick={() => basculer(j.id)}>
                            {ouvert ? 'Masquer' : 'Détails'}
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true" style={{ transform: ouvert ? 'rotate(180deg)' : 'none', transition: 'transform .2s' }}>
                              <path d="m6 9 6 6 6-6" />
                            </svg>
                          </button>
                        )}
                      </td>
                    </tr>
                    {ouvert && (
                      <tr className="admin-detail">
                        <td colSpan={7}>
                          <div className={`admin-detail__boite admin-detail__boite--${s.teinte}`}>
                            <p>{echec ? 'Pourquoi ça a échoué' : pluriel(j.nbRejets, 'ligne mise de côté', 'lignes mises de côté')}</p>
                            <ul>
                              {j.erreurs.map((e, i) => {
                                const { ref, message } = decouper(e)
                                return (
                                  <li key={i}>
                                    <span className="tabular">{ref}</span>
                                    <span>{message}</span>
                                  </li>
                                )
                              })}
                            </ul>
                          </div>
                        </td>
                      </tr>
                    )}
                  </Fragment>
                )
              })}
            </tbody>
          </table>
        </div>
      )}
    </section>
  )
}
