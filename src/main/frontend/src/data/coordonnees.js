/**
 * Position [lat, lon] d'un lieu, ou null si son adresse est absente ou sans coordonnées.
 * Un lieu sans position n'est pas placé sur la carte (le backend accepte ces lieux, y compris dans un circuit).
 */
export function coordonnees(lieu) {
  const a = lieu?.adresse
  return a && Number.isFinite(a.latitude) && Number.isFinite(a.longitude) ? [a.latitude, a.longitude] : null
}
