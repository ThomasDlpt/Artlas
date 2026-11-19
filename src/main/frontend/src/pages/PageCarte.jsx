import { useEffect, useState } from 'react'
import { useLocation, useSearchParams } from 'react-router-dom'
import CarteLeaflet from '../components/carte/CarteLeaflet.jsx'
import BarreRecherche from '../components/recherche/BarreRecherche.jsx'
import MenuBulles from '../components/MenuBulles.jsx'
import PanneauFiltres from '../components/recherche/PanneauFiltres.jsx'
import ListeResultats from '../components/recherche/ListeResultats.jsx'
import FicheLieu from '../components/fiche/FicheLieu.jsx'
import PanneauCircuit from '../components/circuit/PanneauCircuit.jsx'
import useLieux from '../hooks/useLieux.js'
import useFicheLieu from '../hooks/useFicheLieu.js'
import useCircuit from '../context/CircuitContext.jsx'
import useMobile from '../hooks/useMobile.js'
import RechercheMobile from '../components/mobile/RechercheMobile.jsx'
import BarreOnglets from '../components/mobile/BarreOnglets.jsx'
import VoletBas from '../components/mobile/VoletBas.jsx'
import './PageCarte.css'

/**
 * Écran principal : la carte occupe tout l'écran, les panneaux flottent au-dessus.
 * Fait : carte + marqueurs, loupe, menu en bulles, Filtres, Lieux visibles, FicheLieu, Circuit.
 * Avec : contrôles de la carte (plan/satellite, autour de moi, zoom), regroupement des marqueurs.
 * Mobile (≤ 640 px) : recherche en pilule + puces de types, fiche en volet du bas à trois crans,
 * barre d'onglets Carte / Liste / Circuit (les panneaux deviennent des écrans plein écran).
 */
export default function PageCarte() {
  const { filtre, filtreValide, departements, resultats, chargement, erreur, tropLarge, modifierFiltre, reinitialiser, surDeplacementCarte, reessayer } = useLieux()
  const fiche = useFicheLieu()
  const { etapes, numeroEtape, calcul } = useCircuit()
  const [idSurvole, setIdSurvole] = useState(null)
  const mobile = useMobile()
  const location = useLocation()
  // Arrivée depuis « Dupliquer et modifier » : le panneau Circuit est déjà ouvert
  const [panneau, setPanneau] = useState(location.state?.panneau ?? null) // 'filtres' | 'lieux' | 'circuit' | null
  const [parametres] = useSearchParams()

  // Lien partagé : /?lieu=<id> ouvre directement la fiche
  useEffect(() => {
    const id = parametres.get('lieu')
    if (id) fiche.ouvrir(id)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  return (
    <main className={'page-carte' + (fiche.idLieu ? ' page-carte--fiche' : '') + (mobile ? ' page-carte--mobile' : '')}>
      <CarteLeaflet
        lieux={resultats}
        idSelectionne={fiche.idLieu}
        idSurvole={idSurvole}
        numeroEtape={numeroEtape}
        trace={etapes.length >= 2 ? calcul?.trace : null}
        surDeplacement={surDeplacementCarte}
        surClicLieu={fiche.ouvrir}
        surSurvolLieu={setIdSurvole}
      />

      {tropLarge && <p className="page-carte__conseil" role="status">Zoomez pour voir les lieux de cette zone</p>}

      {!mobile && <button type="button" className="page-carte__compte" aria-disabled="true" aria-label="Mon compte, bientôt disponible" title="Mon compte · bientôt">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
          <circle cx="12" cy="8.5" r="3.5" /><path d="M5 20c.8-3.5 3.6-5.5 7-5.5s6.2 2 7 5.5" />
        </svg>
      </button>}

      {mobile ? (
        <>
          <RechercheMobile
            texte={filtre.texte}
            surChangement={(t) => modifierFiltre('texte', t)}
            types={filtre.types}
            modifierFiltre={modifierFiltre}
            filtresOuverts={panneau === 'filtres'}
            surFiltres={() => setPanneau((p) => (p === 'filtres' ? null : 'filtres'))}
            afficherPuces={panneau === null || panneau === 'filtres'}
          />
          <BarreOnglets panneau={panneau} surChangementPanneau={setPanneau} nbEtapes={etapes.length} />
        </>
      ) : (
        <>
          <BarreRecherche texte={filtre.texte} surChangement={(t) => modifierFiltre('texte', t)} />
          <MenuBulles
            ouvertInitial={Boolean(location.state?.panneau)}
            panneau={panneau}
            surChangementPanneau={setPanneau}
            compteurs={{ filtres: filtre.types.length, lieux: resultats.length, circuit: etapes.length }}
          />
        </>
      )}

      {panneau === 'filtres' && (
        <PanneauFiltres
          filtre={filtre}
          departements={departements}
          modifierFiltre={modifierFiltre}
          nbResultats={resultats.length}
          surVoirLieux={() => setPanneau('lieux')}
        />
      )}

      {panneau === 'lieux' && (
        <ListeResultats
          resultats={resultats}
          chargement={chargement}
          erreur={erreur}
          filtre={filtre}
          filtreValide={filtreValide}
          idSelectionne={fiche.idLieu}
          idSurvole={idSurvole}
          surSurvol={setIdSurvole}
          surClicLieu={(id) => {
            fiche.ouvrir(id)
            if (mobile) setPanneau(null) // la liste est plein écran : on revient à la carte pour voir le lieu
          }}
          surReessayer={reessayer}
          surReinitialiser={reinitialiser}
        />
      )}

      {panneau === 'circuit' && <PanneauCircuit surFermer={() => setPanneau(null)} />}

      {fiche.idLieu && (() => {
        const ficheLieu = (
          <FicheLieu
            lieu={fiche.lieu}
            chargement={fiche.chargement}
            erreur={fiche.erreur}
            introuvable={fiche.introuvable}
            surFermer={fiche.fermer}
            surReessayer={fiche.reessayer}
          />
        )
        return mobile ? <VoletBas>{ficheLieu}</VoletBas> : ficheLieu
      })()}
    </main>
  )
}
