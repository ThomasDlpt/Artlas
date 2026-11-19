// Équivalent front de la classe FiltreRecherche du diagramme de classes :
// { texte, types, codeDepartement, label } + estValide() / estVide().
// codeDepartement est choisi parmi les départements renvoyés par l'API (ceux de Basilic).
// Tant que la saisie ne correspond à aucun d'eux, le filtre est invalide : aucun appel au serveur.
// Si la liste n'est pas disponible (chargement, erreur), on ne peut pas juger : le filtre reste valide.

// « Côte-d’Or » → « cote-d'or » : on compare sans accents ni majuscules
const normaliser = (s) =>
  String(s ?? '').normalize('NFD').replace(/[̀-ͯ]/g, '').replace(/[’‘]/g, "'").toLowerCase().trim()
const sansTirets = (s) => normaliser(s).replace(/[-'\s]+/g, ' ')

/** « 35 – Ille-et-Vilaine » */
export const libelleDepartement = (d) => `${d.code} – ${d.nom}`

/**
 * Le département désigné par la saisie, ou null.
 * Accepte « 35 », « 35 – Ille-et-Vilaine », « 35 - Ille-et-Vilaine » ou « ille et vilaine » (sans accent ni majuscule).
 */
export function trouverDepartement(departements, saisie) {
  const s = normaliser(saisie)
  if (!s) return null

  const m = s.match(/^(\w{2,3})(?:\s*[–—-]\s*(.*))?$/)
  if (m) {
    const d = departements.find((x) => normaliser(x.code) === m[1])
    if (d) return !m[2] || sansTirets(d.nom) === sansTirets(m[2]) ? d : null
  }
  return departements.find((d) => sansTirets(d.nom) === sansTirets(s)) ?? null
}

/** Départements dont le code commence comme la saisie, ou dont le nom la contient. */
export function chercherDepartements(departements, saisie, max = 5) {
  const s = normaliser(saisie)
  if (!s) return []
  const debut = departements.filter((d) => normaliser(d.code).startsWith(s) || normaliser(d.nom).startsWith(s))
  const contient = departements.filter((d) => !debut.includes(d) && normaliser(d.nom).includes(s))
  return [...debut, ...contient].slice(0, max)
}

/** Quand rien ne correspond : des départements « voisins » (même premier chiffre, ou mêmes trois premières lettres). */
export function departementsProches(departements, saisie, max = 5) {
  const s = normaliser(saisie)
  if (!s) return []
  if (/^\d/.test(s)) return departements.filter((d) => d.code[0] === s[0]).slice(0, max)
  return departements.filter((d) => normaliser(d.nom).includes(s.slice(0, 3))).slice(0, max)
}

/** Vrai si le champ est vide, désigne un département connu, ou si la liste n'est pas disponible. */
export const departementValide = (departements, codeDepartement) =>
  !codeDepartement || departements.length === 0 || trouverDepartement(departements, codeDepartement) !== null

/** Un filtre invalide ne déclenche aucun appel au serveur. */
export const estValide = (filtre, departements) => departementValide(departements, filtre.codeDepartement)

/** Aucun critère actif. */
export const estVide = (filtre) =>
  !filtre.texte?.trim() && !filtre.types?.length && !filtre.codeDepartement && !filtre.label
