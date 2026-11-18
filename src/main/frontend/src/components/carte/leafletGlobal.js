// leaflet.markercluster s'attend à trouver Leaflet dans la variable globale `L`.
// Ce module est importé AVANT 'leaflet.markercluster' pour la lui fournir.
import L from 'leaflet'

window.L = L
export default L
