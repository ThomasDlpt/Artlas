// Mise en forme des valeurs renvoyées par l'API (typographie française).
const nombre = new Intl.NumberFormat('fr-FR', { maximumFractionDigits: 1 })

export const formatKm = (km) => (km == null ? '—' : `${nombre.format(km)} km`)

// « 2026-11-10 » -> « 10 novembre 2026 » (mois : 'long' ou 'short').
// Une date seule est lue comme une date locale : new Date('2026-11-10') serait minuit UTC,
// soit le 9 novembre à l'ouest de Greenwich (Antilles, Guyane…).
export function formatDate(iso, mois = 'long') {
  if (!iso) return null
  const j = /^(\d{4})-(\d{2})-(\d{2})$/.exec(iso)
  const date = j ? new Date(Number(j[1]), Number(j[2]) - 1, Number(j[3])) : new Date(iso)
  if (Number.isNaN(date.getTime())) return null
  return new Intl.DateTimeFormat('fr-FR', { day: 'numeric', month: mois, year: 'numeric' }).format(date)
}

// 45 -> « 45 min », 116 -> « 1 h 56 », 120 -> « 2 h »
export function formatDuree(minutes) {
  if (minutes == null) return '—'
  const m = Math.round(minutes)
  if (m < 60) return `${m} min`
  const h = Math.floor(m / 60)
  const r = m % 60
  return r ? `${h} h ${String(r).padStart(2, '0')}` : `${h} h`
}
