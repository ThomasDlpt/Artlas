import { useEffect, useLayoutEffect, useRef, useState } from 'react'
import L from './leafletGlobal.js'
import 'leaflet.markercluster'
import 'leaflet.markercluster/dist/MarkerCluster.css'
import { svgIcone, nomIconeType } from '../Icone.jsx'
import { libelleType } from '../../data/referentiels.js'
import { coordonnees, pointValide } from '../../data/coordonnees.js'
import useNotification from '../../context/NotificationContext.jsx'
import ControlesCarte from './ControlesCarte.jsx'
import './carte.css'

const CENTRE_RENNES = [48.1113, -1.6800]
// Valeur par défaut stable : une nouvelle fonction à chaque rendu reconstruirait tous les marqueurs
const AUCUNE_ETAPE = () => null

// CARTO exige désormais une clé d'API : on utilise les tuiles OpenStreetMap (usage léger, attribution obligatoire).
// Pour la production, prévoir un fournisseur dédié (IGN Géoplateforme « Plan IGN », MapTiler, etc.).
const FONDS = {
  plan: {
    url: 'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
    options: { attribution: '© OpenStreetMap contributors', maxZoom: 19 },
  },
  // Orthophotos de l'IGN (Géoplateforme, accès libre sans clé)
  satellite: {
    url:
      'https://data.geopf.fr/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&LAYER=ORTHOIMAGERY.ORTHOPHOTOS' +
      '&STYLE=normal&TILEMATRIXSET=PM&FORMAT=image/jpeg&TILEMATRIX={z}&TILEROW={y}&TILECOL={x}',
    options: { attribution: 'Orthophotos © IGN · © OpenStreetMap contributors', maxZoom: 19, maxNativeZoom: 19 },
  },
}

const CLE_FOND = 'artlas.fond'
function lireFond() {
  try { return localStorage.getItem(CLE_FOND) === 'satellite' ? 'satellite' : 'plan' } catch { return 'plan' }
}
function memoriserFond(fond) {
  try { localStorage.setItem(CLE_FOND, fond) } catch { /* stockage indisponible : tant pis */ }
}

function bboxDepuis(bounds) {
  return { latMin: bounds.getSouth(), latMax: bounds.getNorth(), lonMin: bounds.getWest(), lonMax: bounds.getEast() }
}

const echapper = (t) => String(t).replace(/[&<>"]/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' })[c])

function creerIcone(lieu, actif, survole, etape) {
  const taille = actif ? 40 : 32
  const infobulle = survole ? `<span class="atl-marqueur__infobulle">${echapper(lieu.nom)}</span>` : ''
  const numero = etape ? `<span class="atl-marqueur__etape">${etape}</span>` : ''
  return L.divIcon({
    className: 'atl-marqueur-conteneur',
    html: `<span class="atl-marqueur${actif ? ' atl-marqueur--actif' : ''}" data-type="${lieu.type}">${svgIcone(nomIconeType(lieu.type), 16)}${numero}</span>${infobulle}`,
    iconSize: [taille, taille],
    iconAnchor: [taille / 2, taille / 2],
  })
}

// Regroupement : rond encre avec le nombre de lieux (le nombre vient de la librairie)
function creerIconeGroupe(groupe) {
  const n = groupe.getChildCount()
  const taille = n >= 10 ? 48 : 40
  return L.divIcon({
    className: 'atl-marqueur-conteneur',
    html: `<span class="atl-groupe${n >= 10 ? ' atl-groupe--grand' : ''}" aria-label="${n} lieux, zoomer pour les voir">${n}</span>`,
    iconSize: [taille, taille],
  })
}

/**
 * Carte plein écran : fonds plan / satellite, marqueurs regroupés, tracé du circuit, contrôles.
 * Elle signale la zone visible et le niveau de zoom via `surDeplacement(bbox, zoom)`.
 */
export default function CarteLeaflet({
  lieux, idSelectionne, idSurvole, numeroEtape = AUCUNE_ETAPE, trace = null,
  surDeplacement, surClicLieu, surSurvolLieu,
  regrouper = true, // regroupement des marqueurs proches
  controles = 'complets', // 'complets' (fond, autour de moi, zoom, assistant) ou 'zoom'
  cadrage = null, // [[lat, lon], …] : la carte s'ajuste pour montrer ces points
  margeGauche = 0, // largeur (px) cachée par un panneau à gauche, pour le cadrage
  partieBasCachee = 0, // part de la hauteur (0 à 1) cachée par un volet en bas (mobile), pour le cadrage
}) {
  const { notifier } = useNotification()
  const conteneur = useRef(null)
  const carte = useRef(null)
  const groupe = useRef(null)
  const groupeEtapes = useRef(null) // étapes du circuit : jamais regroupées
  const coucheTrace = useRef(null)
  const coucheFond = useRef(null)
  const position = useRef(null)
  const precision = useRef(null)
  const [fond, setFond] = useState(lireFond)
  const [localisation, setLocalisation] = useState(false)
  const marqueurs = useRef(new Map()) // id du lieu -> { marqueur, lieu, etape }
  const misEnAvant = useRef({ selectionne: null, survole: null }) // état déjà appliqué aux icônes
  // Valeurs du dernier rendu validé, lues par les marqueurs : un survol ne doit pas reconstruire toute la carte.
  // Mises à jour en effet de mise en page : avant les effets ci-dessous, jamais pendant le rendu.
  const courant = useRef({})
  useLayoutEffect(() => {
    courant.current = { idSelectionne, idSurvole, surClicLieu, surSurvolLieu }
  })

  // Création unique de la carte
  useEffect(() => {
    const reduireMouvement = window.matchMedia('(prefers-reduced-motion: reduce)').matches
    const c = L.map(conteneur.current, {
      center: CENTRE_RENNES,
      zoom: 15,
      minZoom: 5,
      maxZoom: 19, // requis par le regroupement des marqueurs
      zoomControl: false,
      zoomAnimation: !reduireMouvement,
    })
    coucheTrace.current = L.layerGroup().addTo(c) // sous les marqueurs
    groupe.current = (regrouper
      ? L.markerClusterGroup({
        showCoverageOnHover: false,
        maxClusterRadius: 50,
        disableClusteringAtZoom: 17,
        spiderfyOnMaxZoom: true,
        animate: !reduireMouvement,
        iconCreateFunction: creerIconeGroupe,
      })
      : L.featureGroup()
    ).addTo(c)
    groupeEtapes.current = L.featureGroup().addTo(c)
    carte.current = c

    const signaler = () => surDeplacement?.(bboxDepuis(c.getBounds()), c.getZoom())
    c.on('moveend', signaler)
    signaler()

    return () => {
      c.remove()
      carte.current = null
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  // Fond de carte : on retire une couche de tuiles et on ajoute l'autre
  useEffect(() => {
    const c = carte.current
    if (!c) return
    if (coucheFond.current) c.removeLayer(coucheFond.current)
    coucheFond.current = L.tileLayer(FONDS[fond].url, FONDS[fond].options).addTo(c)
    coucheFond.current.bringToBack()
    memoriserFond(fond)
  }, [fond])

  // Marqueurs : reconstruits seulement quand les lieux ou les numéros d'étape changent
  useEffect(() => {
    if (!groupe.current) return
    groupe.current.clearLayers()
    groupeEtapes.current.clearLayers()
    marqueurs.current.clear()
    const { idSelectionne: selectionne, idSurvole: survole } = courant.current
    const aRegrouper = []
    lieux.forEach((lieu) => {
      const point = coordonnees(lieu)
      if (!point) return // adresse inconnue : le lieu reste dans la liste, mais pas sur la carte
      const etape = numeroEtape(lieu.id)
      const actif = lieu.id === selectionne || lieu.id === survole
      const libelle = `${libelleType(lieu.type)}, ${lieu.nom}${etape ? `, étape ${etape}` : ''}`
      const marqueur = L.marker(point, {
        icon: creerIcone(lieu, actif, lieu.id === survole, etape),
        keyboard: true, // focusable, role=button
        zIndexOffset: actif ? 1000 : 0,
      })
        // L'option alt de Leaflet ne vaut que pour les icônes <img> : nos icônes sont des <div>.
        // L'élément est recréé chaque fois que le regroupement réaffiche le marqueur, d'où l'événement add.
        .on('add', (e) => e.target.getElement()?.setAttribute('aria-label', libelle))
        .on('click', () => courant.current.surClicLieu?.(lieu.id))
        .on('mouseover', () => courant.current.surSurvolLieu?.(lieu.id))
        .on('mouseout', () => courant.current.surSurvolLieu?.(null))
      marqueurs.current.set(lieu.id, { marqueur, lieu, etape })
      if (etape && regrouper) groupeEtapes.current.addLayer(marqueur)
      else aRegrouper.push(marqueur)
    })
    if (groupe.current.addLayers) groupe.current.addLayers(aRegrouper)
    else aRegrouper.forEach((m) => groupe.current.addLayer(m))
    misEnAvant.current = { selectionne, survole }
  }, [lieux, numeroEtape, regrouper])

  // Survol et sélection : seuls les marqueurs concernés changent d'icône.
  // setIcon réutilise l'élément HTML du marqueur : le focus clavier n'est pas perdu.
  useEffect(() => {
    const avant = misEnAvant.current
    new Set([avant.selectionne, avant.survole, idSelectionne, idSurvole]).forEach((id) => {
      const m = id != null && marqueurs.current.get(id)
      if (!m) return
      const survole = id === idSurvole
      const actif = survole || id === idSelectionne
      m.marqueur.setIcon(creerIcone(m.lieu, actif, survole, m.etape))
      m.marqueur.setZIndexOffset(actif ? 1000 : 0)
    })
    misEnAvant.current = { selectionne: idSelectionne, survole: idSurvole }
  }, [idSelectionne, idSurvole])

  // Tracé du circuit (coordonnées fournies par l'API)
  useEffect(() => {
    if (!coucheTrace.current) return
    coucheTrace.current.clearLayers()
    // Comme pour les marqueurs, une étape sans position est sautée (pas de trait vers le golfe de Guinée)
    const points = (trace ?? []).filter(([lat, lon]) => pointValide(lat, lon))
    if (points.length >= 2) {
      L.polyline(points, { className: 'atl-trace', weight: 3, dashArray: '1 8', lineCap: 'round', lineJoin: 'round', interactive: false })
        .addTo(coucheTrace.current)
    }
  }, [trace])

  // Cadrage sur un ensemble de points (ex. les étapes d'un circuit partagé)
  useEffect(() => {
    const c = carte.current
    if (!c || !cadrage?.length) return
    // Les marges, toutes ensemble, laissent toujours au moins 15 % de la carte visible dans chaque sens :
    // sinon Leaflet calcule un zoom infini (ou NaN) et cadre le circuit hors de l'écran (téléphone à l'horizontale).
    const taille = c.getSize()
    const haut = Math.min(64, taille.y * 0.1)
    const droite = Math.min(64, taille.x * 0.1)
    const bas = Math.min(64 + taille.y * partieBasCachee, taille.y * 0.85 - haut)
    const gauche = Math.min(margeGauche + 48, taille.x * 0.85 - droite)
    c.fitBounds(L.latLngBounds(cadrage), {
      paddingTopLeft: [gauche, haut],
      paddingBottomRight: [droite, bas],
      maxZoom: 17,
    })
  }, [cadrage, margeGauche, partieBasCachee])

  const autourDeMoi = () => {
    if (!navigator.geolocation) {
      notifier('Votre navigateur ne permet pas de vous localiser.')
      return
    }
    setLocalisation(true)
    navigator.geolocation.getCurrentPosition(
      ({ coords }) => {
        setLocalisation(false)
        const c = carte.current
        if (!c) return
        const ici = [coords.latitude, coords.longitude]
        if (position.current) c.removeLayer(position.current)
        if (precision.current) c.removeLayer(precision.current)
        // Cercle de précision (rayon en mètres, fourni par le navigateur) : montre que la position est approximative
        if (coords.accuracy > 25) {
          precision.current = L.circle(ici, { radius: coords.accuracy, className: 'atl-precision', weight: 1, interactive: false }).addTo(c)
        }
        // Marqueur HTML (taille fixe à l'écran) : un circleMarker SVG grossirait pendant le flyTo
        position.current = L.marker(ici, {
          icon: L.divIcon({ className: 'atl-position', html: '<span></span>', iconSize: [20, 20] }),
          interactive: false,
          keyboard: false,
          zIndexOffset: -500,
        }).addTo(c)
        c.flyTo(ici, Math.max(c.getZoom(), 15))
      },
      () => {
        setLocalisation(false)
        notifier('Nous n’arrivons pas à vous localiser. Vérifiez que la localisation est autorisée.')
      },
      { enableHighAccuracy: true, timeout: 10000 },
    )
  }

  return (
    <>
      <div ref={conteneur} className={'atl-carte atl-carte--' + fond} />
      <ControlesCarte
        simple={controles === 'zoom'}
        fond={fond}
        surChangerFond={setFond}
        surAutourDeMoi={autourDeMoi}
        localisation={localisation}
        surZoomer={() => carte.current?.zoomIn()}
        surDezoomer={() => carte.current?.zoomOut()}
      />
    </>
  )
}
