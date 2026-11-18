// Données fictives (Rennes) pour travailler sans backend. Coordonnées approximatives.
// Même structure que celle attendue de l'API (voir docs/design/README.md, section 7).
const rennes = {
  codeInsee: '35238',
  nom: 'Rennes',
  departement: { code: '35', nom: 'Ille-et-Vilaine', region: { code: '53', nom: 'Bretagne' } },
}

const SITES = { l2: 'https://mba.rennes.fr', l3: 'https://www.leschampslibres.fr', l4: 'https://opera-rennes.fr' }

const lieu = (id, nom, type, labels, lat, lon, rue, cp, description) => ({
  id,
  nom,
  description,
  type,
  siteWeb: SITES[id] ?? null,
  photo: null,
  labels,
  dateMiseAJour: '2026-11-10',
  adresse: { rue, codePostal: cp, latitude: lat, longitude: lon, commune: rennes },
  source: [
    { source: 'BASILIC', idExterne: id, dateImport: '2026-10-02' },
    ...(type === 'THEATRE' || type === 'CINEMA' ? [{ source: 'DATATOURISME', idExterne: 'dt-' + id, dateImport: '2026-10-15' }] : []),
  ],
})

export const LIEUX_FICTIFS = [
  lieu('l1', 'Parlement de Bretagne', 'MONUMENT', ['Monument historique'], 48.1137, -1.6836, 'Place du Parlement de Bretagne', '35000', 'Ancien siège du parlement de la province de Bretagne.'),
  lieu('l2', 'Musée des Beaux-Arts de Rennes', 'MUSEE', ['Musée de France'], 48.1112, -1.6741, '20 quai Émile Zola', '35000', 'Collections de peinture, dessin et sculpture du XIVe siècle à nos jours.'),
  lieu('l3', 'Les Champs Libres', 'MUSEE', ['Musée de France'], 48.1056, -1.6759, '10 cours des Alliés', '35000', 'Espace culturel mêlant musée de Bretagne, bibliothèque et Espace des sciences.'),
  lieu('l4', 'Opéra de Rennes', 'THEATRE', [], 48.1117, -1.6794, 'Place de la Mairie', '35000', null),
  lieu('l5', "Cinéma l'Arvor", 'CINEMA', [], 48.1128, -1.6735, '29 rue d’Antrain', '35000', null),
  lieu('l6', 'Bibliothèque des Champs Libres', 'BIBLIOTHEQUE', [], 48.1054, -1.6762, '10 cours des Alliés', '35000', null),
  lieu('l7', 'Cathédrale Saint-Pierre', 'MONUMENT', ['Monument historique'], 48.1131, -1.6820, 'Rue de la Monnaie', '35000', null),
  lieu('l8', 'Portes Mordelaises', 'MONUMENT', ['Monument historique'], 48.1139, -1.6811, 'Place des Lices', '35000', null),
  lieu('l9', 'Frac Bretagne', 'LIEU_EXPOSITION', [], 48.1071, -1.6577, '19 avenue André Mussat', '35000', null),
  lieu('l10', 'Les Ateliers du Vent', 'LIEU_EXPOSITION', [], 48.1101, -1.6849, '59 rue Alphonse Guérin', '35000', null),
]
