/**
 * Le backend stocke latitude et longitude en `double` et ses importeurs mettent 0.0 quand une valeur manque :
 * un 0 exact signifie donc « position inconnue » (aucun lieu réel n'a une coordonnée à 0,000000 pile).
 * À corriger à la source (Adresse en Double nullable) : ce test pourra alors se limiter à null.
 */
export const pointValide = (lat, lon) => Number.isFinite(lat) && Number.isFinite(lon) && lat !== 0 && lon !== 0

/**
 * Position [lat, lon] d'un lieu, ou null si son adresse est absente ou sans coordonnées.
 * Un lieu sans position n'est pas placé sur la carte (le backend accepte ces lieux, y compris dans un circuit).
 */
export function coordonnees(lieu) {
  const a = lieu?.adresse
  return a && pointValide(a.latitude, a.longitude) ? [a.latitude, a.longitude] : null
}
