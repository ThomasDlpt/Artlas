import { useEffect, useRef, useState } from 'react'
import L from './leafletGlobal.js'
import 'leaflet.markercluster'
import 'leaflet.markercluster/dist/MarkerCluster.css'
import { svgIcone, nomIconeType } from '../Icone.jsx'
import { libelleType } from '../../data/referentiels.js'
import useNotification from '../../context/NotificationContext.jsx'
import ControlesCarte from './ControlesCarte.jsx'
import './carte.css'

const CENTRE_RENNES = [48.1113, -1.6800]

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
  lieux, idSelectionne, idSurvole, numeroEtape = () => null, trace = null,
  surDeplacement, surClicLieu, surSurvolLieu,
  regrouper = true, // regroupement des marqueurs proches
  controles = 'complets', // 'complets' (fond, autour de moi, zoom, assistant) ou 'zoom'
  cadrage = null, // [[lat, lon], …] : la carte s'ajuste pour montrer ces points
  margeGauche = 0, // largeur (px) cachée par un panneau à gauche, pour le cadrage
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

  // Marqueurs
  useEffect(() => {
    if (!groupe.current) return
    groupe.current.clearLayers()
    groupeEtapes.current.clearLayers()
    const marqueurs = []
    lieux.forEach((lieu) => {
      const { latitude, longitude } = lieu.adresse
      const survole = lieu.id === idSurvole
      const etape = numeroEtape(lieu.id)
      const marqueur = L.marker([latitude, longitude], {
        icon: creerIcone(lieu, lieu.id === idSelectionne || survole, survole, etape),
        alt: `${libelleType(lieu.type)}, ${lieu.nom}${etape ? `, étape ${etape}` : ''}`,
        keyboard: true,
        zIndexOffset: survole || lieu.id === idSelectionne ? 1000 : 0,
      })
        .on('click', () => surClicLieu?.(lieu.id))
        .on('mouseover', () => surSurvolLieu?.(lieu.id))
        .on('mouseout', () => surSurvolLieu?.(null))
      if (etape && regrouper) groupeEtapes.current.addLayer(marqueur)
      else marqueurs.push(marqueur)
    })
    if (groupe.current.addLayers) groupe.current.addLayers(marqueurs)
    else marqueurs.forEach((m) => groupe.current.addLayer(m))
  }, [lieux, idSelectionne, idSurvole, numeroEtape, surClicLieu, surSurvolLieu])

  // Tracé du circuit (coordonnées fournies par l'API)
  useEffect(() => {
    if (!coucheTrace.current) return
    coucheTrace.current.clearLayers()
    if (trace?.length >= 2) {
      L.polyline(trace, { className: 'atl-trace', weight: 3, dashArray: '1 8', lineCap: 'round', lineJoin: 'round', interactive: false })
        .addTo(coucheTrace.current)
    }
  }, [trace])

  // Cadrage sur un ensemble de points (ex. les étapes d'un circuit partagé)
  useEffect(() => {
    if (!carte.current || !cadrage?.length) return
    carte.current.fitBounds(L.latLngBounds(cadrage), {
      paddingTopLeft: [margeGauche + 48, 64],
      paddingBottomRight: [64, 64],
      maxZoom: 17,
    })
  }, [cadrage, margeGauche])

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
