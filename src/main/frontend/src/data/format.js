// Mise en forme des valeurs renvoyées par l'API (typographie française).
const nombre = new Intl.NumberFormat('fr-FR', { maximumFractionDigits: 1 })

export const formatKm = (km) => (km == null ? '—' : `${nombre.format(km)} km`)

// 45 -> « 45 min », 116 -> « 1 h 56 », 120 -> « 2 h »
export function formatDuree(minutes) {
  if (minutes == null) return '—'
  const m = Math.round(minutes)
  if (m < 60) return `${m} min`
  const h = Math.floor(m / 60)
  const r = m % 60
  return r ? `${h} h ${String(r).padStart(2, '0')}` : `${h} h`
}
