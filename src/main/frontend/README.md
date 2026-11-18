# Artlas — frontend

React 18 + Vite + Leaflet (+ `leaflet.markercluster`, `react-router-dom`).
Spécification d'interface : `docs/design/README.md` (à la racine du dépôt). Diagrammes : `docs/diagramme_composants_frontend.puml` et `docs/diagramme_classes_frontend.puml`.

```bash
cd src/main/frontend
npm install
npm run dev      # http://localhost:5173
npm run build    # production, dans dist/
```

## Données fictives ou vrai backend

- Par défaut (`VITE_USE_MOCK` absent ou différent de `false`), **les 4 services répondent avec des données fictives**, sans appel réseau :
  lieux de Rennes (`src/data/lieuxFictifs.js`), circuits gardés dans le `localStorage`, journaux de synchronisation et départements simulés.
  Le circuit d'exemple `K7PQ2MXA` est toujours disponible, et le mot de passe administrateur fictif est celui de la maquette (voir `AuthAdminApi.js`).
- Pour appeler le vrai backend (Spring Boot, port 8080) : créer `.env.local` avec `VITE_USE_MOCK=false`. Les appels `/api/...` sont relayés par Vite (`vite.config.js`). `VITE_API_URL` change l'URL de base (`/api` par défaut).
- Le front **ne calcule rien de métier** : distances, durées, ordre des étapes et code de partage viennent de l'API. En mode fictif, `CircuitApi.js` simule ce calcul (marcher à 5 km/h, détour ×1,3) ; cette simulation disparaîtra avec l'API.

## Pages

| Route | Page | Contenu |
| --- | --- | --- |
| `/` | `PageCarte` | Carte plein écran, recherche, filtres, liste des lieux visibles, fiche d'un lieu, circuit en cours |
| `/circuit/:codePartage` | `PageCircuitPartage` | Circuit en lecture seule, « Dupliquer et modifier », ou « circuit introuvable » |
| `/admin/connexion` | `PageConnexionAdmin` | Mot de passe administrateur |
| `/admin` | `PageAdmin` | Protégée (`RouteAdminProtegee`) : synchronisation des sources, correction et masquage des lieux |

`PageCarte` a deux dispositions, choisies par `useMobile()` (`matchMedia`, seuil 640 px) :

- **Bureau** : menu en bulles (Filtres, Lieux visibles, Circuit), loupe qui se déplie, panneaux flottants, fiche à droite.
- **Mobile** : recherche en pilule + puces de types, barre d'onglets Carte / Liste / Circuit, panneaux plein écran, fiche en volet du bas à trois crans (aperçu, mi-hauteur, plein écran ; on le glisse, on appuie sur la poignée, ou flèches haut/bas).

## Structure

```
src/
  services/    ApiClient (HTTP, jeton) · LieuApi · CircuitApi · AdminApi · AuthAdminApi
  hooks/       useLieux (recherche = carte = liste) · useFicheLieu · useDepartements
               useAdministration (synchro + correction des lieux) · useMobile
  context/     CircuitContext (circuit en cours) · AdminAuthContext (session) · NotificationContext (toasts)
  data/        referentiels (types, labels) · FiltreRecherche (validation du filtre) · format · lieuxFictifs
  components/  carte/ recherche/ fiche/ circuit/ admin/ mobile/  + MenuBulles, Icone, PastilleType, Logo
  pages/       PageCarte · PageCircuitPartage · PageConnexionAdmin · PageAdmin
  styles/      artlas-tokens.css (tokens de la maquette, jamais de couleur en dur) · global.css
```

Écarts avec le diagramme de composants : `MarqueurLieu` et `TraceCircuit` sont dans `CarteLeaflet.jsx` (marqueurs et `L.polyline`), et `SelecteurFondCarte` dans `ControlesCarte.jsx`.

## Ce que le front attend de l'API

Toutes les routes sont préfixées par `/api`. Les routes `/admin/**` reçoivent `Authorization: Bearer <jeton>`.
Les structures JSON sont décrites au § 7 de `docs/design/README.md` (`Lieu`, `Circuit`, `JournalSynchro`).

| Méthode | Route | Rôle | Service |
| --- | --- | --- | --- |
| GET | `/lieux` | Recherche avec filtre : `texte`, `types` (répété), `codeDepartement`, `label` | `LieuApi.rechercher` |
| GET | `/lieux/zone` | Idem dans la zone visible : `latMin`, `latMax`, `lonMin`, `lonMax` + filtre | `LieuApi.rechercherDansZone` |
| GET | `/lieux/{id}` | Fiche d'un lieu (404 si inconnu ou masqué) | `LieuApi.selectionner` |
| GET | `/departements` | Départements des lieux importés, `[{ code, nom }]` triés par code | `LieuApi.listerDepartements` |
| POST | `/circuits/calcul` | Distances, durées et tracé d'un circuit, sans l'enregistrer | `CircuitApi.calculer` |
| POST | `/circuits` | Sauvegarde ; renvoie le circuit avec son `codePartage` | `CircuitApi.sauvegarder` |
| PUT | `/circuits/{id}` | Modification | `CircuitApi.modifier` |
| GET | `/circuits/partage/{codePartage}` | Circuit partagé (404 si le code est inconnu) | `CircuitApi.chargerParCode` |
| POST | `/admin/connexion` | `{ motDePasse }` → `{ jeton, expiration }` ; 401 ou 403 si incorrect | `AuthAdminApi.connexion` |
| POST | `/admin/deconnexion` | Fin de session | `AuthAdminApi.deconnexion` |
| GET | `/admin/synchronisations` | Journaux, du plus récent au plus ancien | `AdminApi.listerJournaux` |
| POST | `/admin/synchronisations` | Lance une synchronisation ; renvoie les nouveaux journaux | `AdminApi.lancerSynchronisation` |
| GET | `/admin/lieux?texte=` | Recherche d'un lieu à corriger (masqués compris) | `AdminApi.rechercherLieux` |
| PUT | `/admin/lieux/{id}` | Enregistre les corrections d'un lieu | `AdminApi.corrigerLieu` |
| PUT | `/admin/lieux/{id}/visibilite` | `{ masque }` : masque ou réaffiche un lieu | `AdminApi.changerVisibilite` |

Une erreur réseau devient une `ErreurApi` de statut 0 (« Service indisponible, réessayez plus tard »).

## Filtre par département

La liste des départements vient de `GET /departements` (donc de la base, alimentée par Basilic) et non d'un fichier. Le champ accepte `35`, `35 – Ille-et-Vilaine` ou le nom ; une saisie qui ne correspond à aucun département rend le filtre invalide : message sous le champ, bouton « Voir les N lieux » désactivé et **aucun appel serveur**. Tant que la liste n'est pas chargée (ou en cas d'erreur), le filtre n'est pas jugé.

## Points ouverts

- Aucun test automatisé (ni Vitest, ni lint).
- Le backend n'expose pas encore d'API REST : le chemin `VITE_USE_MOCK=false` n'a pas été exercé.
- Le diagramme de composants prévoit encore `departements.json` pour `PanneauFiltres` ; il faudra le remplacer par `LieuApi`.
- Photos réelles des lieux : à brancher sur le champ `photo`.
- V2 : assistant et « Mon compte » (boutons déjà présents, désactivés).
