/**
 * Position [lat, lon] d'un lieu, ou null si son adresse est absente ou sans coordonnées.
 * Un lieu sans position n'est pas placé sur la carte (le backend accepte ces lieux, y compris dans un circuit).
 *
 * Le backend stocke latitude et longitude en `double` : une position manquante à l'import arrive en (0, 0).
 * Ce point est en plein golfe de Guinée, aucun lieu culturel n'y est : on le traite comme « sans position ».
 */
export function coordonnees(lieu) {
  const a = lieu?.adresse
  if (!a || !Number.isFinite(a.latitude) || !Number.isFinite(a.longitude)) return null
  if (a.latitude === 0 && a.longitude === 0) return null
  return [a.latitude, a.longitude]
}
