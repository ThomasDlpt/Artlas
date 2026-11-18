// Libellés des types de lieu (code API -> singulier / pluriel), dans l'ordre d'affichage.
export const TYPES = [
  { code: 'MUSEE', singulier: 'Musée', pluriel: 'Musées' },
  { code: 'MONUMENT', singulier: 'Monument', pluriel: 'Monuments' },
  { code: 'LIEU_EXPOSITION', singulier: 'Lieu d’exposition', pluriel: 'Expositions' },
  { code: 'THEATRE', singulier: 'Théâtre', pluriel: 'Théâtres' },
  { code: 'CINEMA', singulier: 'Cinéma', pluriel: 'Cinémas' },
  { code: 'BIBLIOTHEQUE', singulier: 'Bibliothèque', pluriel: 'Bibliothèques' },
  { code: 'AUTRE', singulier: 'Autre lieu', pluriel: 'Autres lieux' },
]

export const libelleType = (code) => (TYPES.find((t) => t.code === code) ?? TYPES[6]).singulier

// Les départements ne sont pas en dur : ils viennent de l'API (LieuApi.listerDepartements),
// qui renvoie ceux des lieux importés depuis Basilic.

export const LABELS = ['Musée de France', 'Monument historique', 'Maison des Illustres', 'Jardin remarquable']

// « Musées, Monuments » ou « Tous les types »
export function resumeTypes(codes) {
  if (!codes.length) return 'Tous les types'
  return TYPES.filter((t) => codes.includes(t.code)).map((t) => t.pluriel).join(', ')
}

// « 1 lieu » / « 3 lieux »
export const nbLieux = (n) => `${n.toLocaleString('fr-FR')} ${n > 1 ? 'lieux' : 'lieu'}`
