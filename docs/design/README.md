# Artlas — maquettes et spécification d'interface

> Dossier de passation entre la maquette et le code du front (React + Leaflet).
> Il décrit **ce que l'utilisateur voit et fait**, pas les classes : si le diagramme de classes
> ou de composants évolue, on garde les comportements ci-dessous et on adapte seulement les noms.

- Maquette interactive : <https://claude.ai/artifact/2YVKKU76BqVv1RMdHye2Uk> (privée : la partager depuis le menu Partager avant de l'envoyer à l'équipe)
- Dernière mise à jour : 12 novembre 2026

## Contenu du dossier

| Fichier | Rôle |
| --- | --- |
| `README.md` | Cette spécification : écrans, comportements, états, textes, règles. **La source de vérité.** |
| `artlas-tokens.css` | Variables CSS (couleurs, typo, espacements, rayons, ombres). À importer une fois au démarrage de l'app. |
| `maquettes/*.dc.html` | Code source de chaque écran de la maquette. Référence pour les valeurs exactes (marges, tailles, textes, SVG des icônes). Ce n'est **pas** du code à copier tel quel : c'est un format de maquette (`<sc-if>`, `<sc-for>`, `{{…}}`). |
| `maquettes/ds/artlas/` | Feuilles de style du design system utilisées par les maquettes (`tokens.css`, `bundle.css` avec les classes `atl-*`, `bundle.js` avec les icônes). |

### Pour coder à partir de ce dossier (humain ou assistant)

1. Lire ce README en entier.
2. Importer `artlas-tokens.css` ; ne jamais écrire une couleur en dur.
3. Pour chaque composant, ouvrir le fichier de maquette indiqué dans la section « Correspondance » et reprendre les valeurs.
4. Le front **ne calcule rien de métier** : distances, durées, ordre des étapes, totaux et code de partage viennent de l'API. Il les formate seulement (`Intl.NumberFormat('fr-FR')`, `Intl.DateTimeFormat('fr-FR')`).

---

## 1. Fondations

### Style général

- Thème unique « Papier » : fond chaud, encre brune, une seule couleur d'action, le **garance** (`--color-accent`, #a8401f).
- Rendu **doux** : panneaux flottants arrondis (16 px), ombres légères, boutons et puces en pilule, pas de filets noirs, pas d'étiquettes en capitales dans les panneaux.
- La carte d'abord : les panneaux flottent au-dessus de la carte et n'en cachent jamais toute la largeur.

### Couleurs

Voir `artlas-tokens.css`. Règles :

- `--color-accent` uniquement pour l'action principale, le tracé du circuit, les numéros d'étape et la bulle de menu principale. Jamais comme couleur de type.
- Chaque type de lieu a une couleur **et** un pictogramme (jamais la couleur seule) :

| Code API | Libellé (singulier / pluriel) | Token | Pictogramme |
| --- | --- | --- | --- |
| `MUSEE` | Musée / Musées | `--color-type-musee` | fronton à colonnes |
| `MONUMENT` | Monument / Monuments | `--color-type-monument` | tour crénelée |
| `LIEU_EXPOSITION` | Lieu d'exposition / Expositions | `--color-type-exposition` | tableau sur chevalet |
| `THEATRE` | Théâtre / Théâtres | `--color-type-theatre` | masque |
| `CINEMA` | Cinéma / Cinémas | `--color-type-cinema` | clap |
| `BIBLIOTHEQUE` | Bibliothèque / Bibliothèques | `--color-type-bibliotheque` | livre ouvert |
| `AUTRE` | Autre lieu / Autres lieux | `--color-type-autre` | losange pointé |

- Sur un élément, poser `data-type="MUSEE"` : le CSS en déduit `--t` (voir la fin de `artlas-tokens.css`).
- Les statuts (succès, attention, erreur) se distinguent aussi par la forme ou un mot, jamais par la couleur seule.

### Typographie

- **Fraunces** (serif) : titres et noms de lieux, jamais sous 14 px dans les panneaux.
- **Source Sans 3** : interface, textes, chiffres (`font-variant-numeric: tabular-nums` pour toute valeur numérique).
- Chargement : `https://fonts.googleapis.com/css2?family=Fraunces:ital,opsz,wght@0,9..144,400..700;1,9..144,400..700&family=Source+Sans+3:ital,wght@0,400..700;1,400..700&display=swap`

| Usage | Police | Taille / interligne | Graisse |
| --- | --- | --- | --- |
| Nom du lieu dans la fiche | Fraunces | 20 / 26 | 600 |
| Nom du circuit | Fraunces | 20 / 26 | 600 |
| Nom en liste, étape | Fraunces | 15–16 / 20–21 | 600 |
| Titre de page (admin, partage) | Fraunces | 22–24 / 28–30 | 600 |
| Texte courant | Source Sans 3 | 15 / 22–23 | 400 |
| Méta (type · commune) | Source Sans 3 | 13–14 / 18–20 | 400 |
| Libellé de champ | Source Sans 3 | 12 / 16 | 600 |
| Totaux du circuit | Source Sans 3 | 20 / 26 | 600, tabular-nums |

Tout `<input>` en 16 px minimum (évite le zoom automatique sur mobile).

### Icônes

Jeu maison au trait : grille 24, trait 1,75 px, extrémités arrondies, `currentColor`. Les tracés SVG sont dans `maquettes/ds/artlas/components/bundle.js` (objet `ICONS`) et dans `maquettes/IconeType.dc.html` (les 7 types). Icônes ajoutées par la maquette, à reprendre depuis `maquettes/CarteDesktop.dc.html` : menu (3 traits), filtres (curseurs), calques (plan/satellite), compte, discussion (V2), synchronisation, masquer.

### Mouvement

- Panneaux : apparition en 220–240 ms, `cubic-bezier(.2,.7,.2,1)` ; glissement de 10–14 px + fondu.
- Bulles secondaires : apparition en « pop » (échelle 0,4 → 1), décalées de 40 ms.
- Respecter `prefers-reduced-motion` : aucune animation, et `zoomAnimation: false` sur Leaflet.

---

## 2. PageCarte — route `/`

Fichier de référence : `maquettes/CarteDesktop.dc.html` (version cliquable, avec tous les états).

### Disposition (desktop)

La carte occupe tout l'écran. Tout le reste flotte au-dessus :

```
┌───────────────────────────────────────────────────────────────┐
│ [▲ Artlas] (🔍)                                     (compte)    │
│  (≡)  ·(circuit)                                                │
│     ·(lieux)              CARTE                  ┌───────────┐  │
│  ·(filtres)                                       │ FicheLieu │  │
│ ┌───────────┐                                     │ (si lieu  │  │
│ │ panneau   │                                     │  choisi)  │  │
│ │ déroulé   │                          [Satellite]│           │  │
│ │           │                          (◎)(+/−)   │           │  │
│ └───────────┘                   (pilule message)  └───────────┘  │
└───────────────────────────────────────────────────────────────┘
```

### 2.1 Haut gauche : nom, logo, loupe (`BarreRecherche`)

- Pastille blanche avec le symbole (triangle pointé) et le mot « Art*las* » (« las » en italique garance) ; lien vers l'accueil.
- À côté, un bouton rond **loupe** (48 px). **La barre de recherche n'est pas visible par défaut.**
- Clic sur la loupe : le bouton se déplie en champ de recherche (360 px, pilule) avec le focus dans le champ, placeholder « Un lieu, une commune… », et une croix « Fermer la recherche ».
- `Échap` ou la croix referment le champ.

### 2.2 Menu en bulles (sous le logo)

- **Bulle principale** : rond garance de 56 px avec l'icône 3 traits. `aria-expanded`. Au clic, elle devient une croix et trois **bulles secondaires** (46 px, blanches, icône garance) apparaissent en arc de cercle autour d'elle :
  - **Circuit** à droite (0°),
  - **Lieux visibles** en diagonale (45°),
  - **Filtres** en dessous (90°).
- Rayon de l'arc ≈ 78 px depuis le centre de la bulle principale.
- Chaque bulle secondaire porte son nom dans une petite pastille tant qu'aucun panneau n'est ouvert, et un **compteur** en haut à droite : nombre de types choisis (Filtres, masqué si 0), nombre de lieux à l'écran (Lieux visibles), nombre d'étapes (Circuit).
- Une bulle secondaire active passe en fond garance, icône blanche.
- **Un seul panneau secondaire ouvert à la fois.** Recliquer sur la bulle active le referme. Fermer la bulle principale referme tout.

### 2.3 Panneau Filtres (`PanneauFiltres`) — déroule **vers le bas**

- Sous l'amas de bulles, largeur 280 px, coins 16 px.
- Titre discret « Que voulez-vous voir ? ».
- Liste verticale des 7 types : pastille ronde de la couleur du type avec son pictogramme, libellé au pluriel, coche garance si actif. Chaque ligne est un `<button aria-pressed>` de 44 px de haut. Multi-sélection ; aucun choix = tous les types.
- Lien « Département et label » qui déplie deux listes déroulantes : département avec recherche (« 35 – Ille-et-Vilaine ») et label (« Musée de France », « Monument historique », « Tous les labels »).
- Bas du panneau : bouton « Voir les N lieux » (ouvre le panneau Lieux visibles) et « Effacer ».
- Filtre invalide : message sous le champ concerné uniquement (voir § 5), sans appel au serveur.

### 2.4 Panneau Lieux visibles (`ListeResultats`) — déroule **vers le bas**

- Même position que Filtres, largeur 360 px, jusqu'en bas de l'écran (défilement interne).
- En-tête : « N lieux à l'écran », puis le résumé des filtres (« Musées, Monuments · la liste suit la carte »).
- Une ligne par lieu : pastille ronde de 32 px à la couleur du type, nom en Fraunces 16 px (coupé par « … » si trop long), méta « Type · Commune · Label ». Si le lieu est dans le circuit : pastille garance avec son numéro d'étape.
- Survol d'une ligne : fond `--color-paper-200`, et le marqueur correspondant grossit avec un anneau encre et une infobulle du nom.
- Clic : ouvre la FicheLieu à droite (la liste reste ouverte, la ligne passe en fond `--color-accent-tint`).
- La liste se met à jour **sans message** quand on déplace la carte (recherche dans la zone visible).

### 2.5 Panneau Circuit (`PanneauCircuit` + `EtapeItem`) — colonne **vers la droite**

- S'ouvre à droite de l'amas de bulles (left ≈ 168 px, top ≈ 80 px), largeur 360 px, jusqu'en bas.
- En-tête : « Votre circuit · N étapes » (garance, 13 px) et une croix de fermeture.
- Nom du circuit modifiable (Fraunces 20 px, soulignement pointillé), puis description sur fond doux.
- Étapes : poignée de glisser-déposer, numéro dans un rond garance, nom (Fraunces 15 px), « Type · Commune », durée de visite modifiable en minutes (pilule), bouton retirer. Entre deux étapes : ligne pointillée garance et « 0,9 km · 11 min à pied » (valeurs de l'API).
- Récapitulatif dans un encadré doux : Distance (km), Durée (visites + marche à 5 km/h, calculée par l'API), commune de Départ.
- Bouton « Sauvegarder et partager » : **actif seulement à partir de 2 étapes**. Sinon désactivé avec la phrase « Encore un lieu et vous pourrez le sauvegarder ».
- Après sauvegarde : encadré vert doux « Sauvegardé · code de partage » avec le code (8 caractères, ex. `K7PQ2MXA`) et « Copier le lien ».
- Réordonner au clavier : flèches haut/bas puis Espace, annoncé par une région `role="status"`. Pendant le recalcul par l'API, les totaux affichent un squelette et le bouton est indisponible.

### 2.6 FicheLieu — à droite

- Panneau flottant à droite (380 px, du haut au bas, coins 16 px). Peut rester ouvert en même temps qu'un panneau de gauche.
- Bandeau photo de 128 px. Sans photo : fond hachuré doux avec le pictogramme du type et « Pas de photo fournie par la source ».
- Nom (Fraunces 20 px) ; ligne « [picto] Type · Commune, Département » ; labels en pastilles.
- Actions en pilule : « Ajouter au circuit » (principal) ou, si déjà présent, « Déjà dans le circuit » désactivé ; bouton rond site web ; bouton rond partager.
- Description (15 px), puis lignes avec icône : adresse (rue, code postal, commune), site web, « Mis à jour le … ».
- Pied : « Source : Basilic — ministère de la Culture · importé le … » et/ou « Source : DataTourisme · … ».
- Croix ronde en haut à droite pour fermer.

### 2.7 Bas droite : contrôles de la carte

De haut en bas :

1. **Vignette Plan / Satellite** (72 × 72, coins 14 px, bord blanc) : elle montre un aperçu de l'**autre** vue et son nom (« Satellite » en vue plan, « Plan » en vue satellite). Un clic bascule la carte.
2. « Autour de moi » (rond).
3. Zoom + / −.
4. « Assistant · bientôt » (V2, désactivé, bord pointillé).

Ces contrôles se décalent à gauche de la fiche quand elle est ouverte. En haut à droite : bouton rond « Mon compte » (V2, désactivé).

### 2.8 Carte (`CarteLeaflet`, `MarqueurLieu`, `TraceCircuit`)

- Vue plan : tuiles CartoDB Positron `https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png`, mention « © OpenStreetMap contributors © CARTO ». Ton papier : `filter: sepia(.12) saturate(.75)` sur `.leaflet-tile-pane` (jamais sur les marqueurs).
- Vue satellite : orthophotos IGN (Géoplateforme, couche `ORTHOIMAGERY.ORTHOPHOTOS`) ou Esri World Imagery. **Pas de filtre sépia.** Mention « Orthophotos © IGN · © OpenStreetMap contributors ». Les noms de quartiers passent en blanc avec un contour sombre.
- Basculer = retirer un `L.tileLayer` et ajouter l'autre ; mémoriser le choix (localStorage) si souhaité.
- Marqueur : `L.divIcon` rond de 32 px, fond couleur du type, pictogramme blanc, bord blanc 2 px, cible tactile 44 px, `aria-label` = « Type, Nom du lieu[, étape N] », focusable au clavier. Sélectionné : 40 px + anneau encre. Étape du circuit : petit numéro garance en haut à droite.
- Regroupement (Leaflet.markercluster) : rond encre avec le nombre (le nombre vient de la librairie).
- Tracé du circuit : `L.polyline` garance, `weight: 3`, `dashArray: '1 8'`, `lineCap: 'round'`, coordonnées fournies par l'API.

### 2.9 Notifications (`ZoneNotifications`)

- Une seule à la fois, en **pilule sombre** en bas au centre : « Musée des Beaux-Arts de Rennes ajouté à votre circuit », « Circuit sauvegardé · code K7PQ2MXA », « Lien copié ». Action éventuelle à droite (« Annuler », « Réessayer »).
- Disparaît après 4 s, sauf une erreur.
- On ne notifie que ce que la personne vient de faire. Les erreurs de chargement s'affichent **à l'endroit** où l'on attendait le contenu, pas en double dans un toast.
- Conseil discret en haut au centre si la zone est trop large : « Zoomez pour voir les lieux de cette zone ».

### 2.10 Mobile (390 px)

Fichier : `maquettes/CarteMobile.dc.html`. Carte plein écran, recherche en pilule en haut, fiche en volet du bas (3 crans : aperçu, mi-hauteur, plein écran), barre d'onglets Carte / Liste / Circuit de 64 px.
**À mettre à jour** : la maquette mobile n'a pas encore le menu en bulles ni la loupe repliée (voir § 9).

---

## 3. PageCircuitPartage — route `/circuit/:codePartage`

Fichiers : `maquettes/CircuitPartage.dc.html`, `maquettes/CircuitIntrouvable.dc.html`.

- **Lecture seule** : carte avec le tracé et les marqueurs numérotés ; panneau à gauche avec logo, pastille « Circuit partagé », nom, description en italique, « Code K7PQ2MXA · en lecture seule », récapitulatif, liste des étapes (sans poignée ni champ modifiable), boutons « Dupliquer et modifier » (principal) et « Copier le lien ». Phrase : « La copie devient votre circuit ; l'original ne bouge pas. » Bouton « Explorer la carte » en haut à droite.
- **Code introuvable** : carte estompée, carte centrée « Ce circuit est introuvable », texte « Aucun circuit ne correspond au code … Le lien a peut-être été mal copié », champ « Code de partage » (8 lettres ou chiffres, en majuscules) + « Ouvrir », lien « Explorer la carte ».

## 4. Administration

### PageConnexionAdmin — `/admin/connexion`

Fichier : `maquettes/ConnexionAdmin.dc.html`. Carte centrée « Espace administrateur », un seul champ « Mot de passe » (pilule, bouton « Afficher »), bouton « Se connecter ». Erreur : contour rouge du champ et, **sous le champ**, « **Mot de passe incorrect.** Vérifiez les majuscules et réessayez. » (`role="alert"`, `aria-invalid`).

### PageAdmin — `/admin` (protégée)

Fichier : `maquettes/Admin.dc.html`. Deux cartes arrondies sur fond papier.

- **TableauSynchronisation** : titre « Synchronisation des données », « dernier passage aujourd'hui à 9 h 14 », bouton « Lancer une synchronisation ». Tableau : Date, Source (Basilic / DataTourisme), Ajouts, Mises à jour, Rejets (en rouge si > 0), Statut, Détails.
  - Statut = point + phrase + précision, **pas de badge** :
    - `SUCCES` → point plein vert, « Terminé », « Rien à signaler »
    - `PARTIEL` → point à moitié plein ambre, « Terminé, à vérifier », « 6 lignes écartées »
    - `ECHEC` → anneau rouge vide, « Interrompu », « Aucune donnée modifiée » ; Ajouts / Mises à jour / Rejets affichent « — ».
  - Le code API reste lisible au survol (attribut `title`).
  - « Détails » déplie la liste des lignes écartées (n° de ligne + raison) dans un encadré teinté arrondi.
- **FormulaireCorrectionLieu** : recherche d'un lieu (pilule), puis champs Nom, Description, Type, Site web, Photo (adresse de l'image, avec vignette), Adresse (Rue, Code postal, Commune). Boutons « Enregistrer les corrections » / « Annuler ». Encadré « Visibilité » avec « Masquer ce lieu » → confirmation en place : « Masquer « Opéra de Rennes » ? Le public ne le verra plus. Vous pourrez le réafficher d'ici à tout moment. » + « Oui, masquer » / « Annuler ».

---

## 5. États à couvrir

Fichiers : `maquettes/EtatsRecherche.dc.html`, `maquettes/EtatsCircuit.dc.html`.

| État | Où | Ce qu'on affiche |
| --- | --- | --- |
| Chargement de la liste | Panneau Lieux visibles | « On cherche les lieux autour de … » + lignes fantômes (rond + 2 barres) qui pulsent doucement |
| Chargement de la fiche | FicheLieu | Même silhouette que la fiche finale en squelette ; le marqueur est déjà sélectionné |
| Aucun résultat | Panneau Lieux visibles | Illustration, « Aucun lieu ne correspond à votre recherche », explication à partir des filtres actifs, boutons « Retirer le label » / « Tout réinitialiser » |
| Filtre invalide | Champ concerné | Contour rouge, « Ce département n'existe pas. », suggestions cliquables ; « Rechercher » désactivé ; **aucun appel serveur** |
| Serveur ou base inaccessible | Là où on attendait les résultats | « Service indisponible, réessayez plus tard » + « Nous n'arrivons pas à joindre nos sources pour le moment. Votre circuit, lui, est bien gardé. » + « Réessayer » |
| Circuit vide | PanneauCircuit | « Votre circuit commence ici » + mode d'emploi ; « Sauvegarder » désactivé, « Disponible à partir de 2 lieux » |
| Circuit à 1 étape | PanneauCircuit | Une place en pointillés « Choisissez un deuxième lieu sur la carte » ; 2 points de progression ; bouton désactivé |
| Circuit valide | PanneauCircuit | Totaux, « Sauvegarder et partager » actif |
| Réordonnancement | PanneauCircuit | Ligne soulevée (ombre, légère mise à l'échelle) ; totaux en squelette « On recalcule le trajet… » |
| Sauvegarde échouée | PanneauCircuit | Phrase avec icône au-dessus du bouton « Réessayer » : « Vos étapes restent ici, rien n'est perdu. » |
| Code de partage introuvable | `/circuit/:code` | Voir § 3 |

## 6. Textes et ton

- Vouvoiement, phrases courtes, verbes d'action (« Ajouter au circuit », « Voir les 4 lieux »).
- Majuscule initiale seulement. Typographie française : espace insécable avant `: ; ! ?` et dans « … », virgule décimale (`0,9 km`), heures `1 h 56`, milliers séparés par une espace fine.
- Pas d'emoji, pas de point d'exclamation, pas de « Erreur · » en préfixe.
- Toujours citer la source des données en bas de fiche.

## 7. Données attendues de l'API

Le front affiche ces champs (structure fournie par l'équipe ; les noms peuvent évoluer, le rendu reste le même) :

```json
Lieu: { "id", "nom", "description?", "type", "siteWeb?", "photo?", "labels": [],
        "dateMiseAJour",
        "adresse": { "rue", "codePostal", "latitude", "longitude",
                     "commune": { "codeInsee", "nom",
                                  "departement": { "code", "nom", "region": { "code", "nom" } } } },
        "source": [{ "source": "BASILIC|DATATOURISME", "idExterne", "dateImport" }] }

Circuit: { "id", "nom", "description", "codePartage", "distanceKm", "dureeMinutes",
           "communeDepart", "etapes": [{ "numOrdre", "dureeVisiteMin", "lieu": Lieu }] }
```

Données d'exemple utilisées dans la maquette : Parlement de Bretagne (MONUMENT, Monument historique), Musée des Beaux-Arts de Rennes (MUSEE, Musée de France), Les Champs Libres, Opéra de Rennes, Cinéma l'Arvor, Bibliothèque des Champs Libres, Cathédrale Saint-Pierre, Portes Mordelaises, Frac Bretagne, Les Ateliers du Vent. Circuit « Rennes, cœur historique », 2 étapes, 0,9 km, 1 h 56, code `K7PQ2MXA`.

## 8. Accessibilité

- Contraste AA sur toutes les paires texte / fond.
- Vrais `<button>`, `<a href>`, `<input>` + `<label>` ; `aria-label` sur chaque bouton icône ; `aria-expanded` sur la loupe, la bulle principale et les bulles secondaires ; `aria-pressed` sur les lignes de type.
- Anneau de focus `--color-focus` 2 px, décalage 2 px, sur `:focus-visible`, marqueurs Leaflet compris.
- Le panneau Lieux visibles est l'alternative complète à la carte pour les lecteurs d'écran.
- Cibles tactiles ≥ 44 px.

## 9. Correspondance maquette ↔ composants

Noms issus de `docs/diagramme_composants_frontend.puml`. Si le diagramme change, mettre à jour cette colonne seulement.

| Composant | Rôle dans la maquette | Fichier de référence |
| --- | --- | --- |
| `PageCarte` | Écran principal, carte plein écran | `CarteDesktop.dc.html`, `CarteMobile.dc.html` |
| `BarreRecherche` | Logo + loupe qui se déplie | `CarteDesktop.dc.html` (« Haut gauche ») |
| *(nouveau)* `MenuBulles` | Bulle principale + 3 bulles secondaires | `CarteDesktop.dc.html` (« Bulles ») |
| `PanneauFiltres` | Panneau déroulé de la bulle Filtres | `CarteDesktop.dc.html` (« Panneau Filtres ») |
| `ListeResultats` | Panneau déroulé de la bulle Lieux visibles | `CarteDesktop.dc.html` (« Panneau Lieux visibles ») |
| `PanneauCircuit`, `EtapeItem` | Colonne de la bulle Circuit | `CarteDesktop.dc.html`, `EtatsCircuit.dc.html` |
| `FicheLieu` | Panneau de droite | `CarteDesktop.dc.html`, `EtatsRecherche.dc.html` |
| `CarteLeaflet`, `MarqueurLieu`, `TraceCircuit` | Carte, marqueurs, clusters, tracé | `CarteDesktop.dc.html` |
| *(nouveau)* `SelecteurFondCarte` | Vignette Plan / Satellite | `CarteDesktop.dc.html` (« Bas droite ») |
| `ZoneNotifications` | Pilule sombre en bas | `CarteDesktop.dc.html`, `EtatsRecherche.dc.html` |
| `PageCircuitPartage` | Lecture seule + code introuvable | `CircuitPartage.dc.html`, `CircuitIntrouvable.dc.html` |
| `PageConnexionAdmin` | Mot de passe | `ConnexionAdmin.dc.html` |
| `PageAdmin`, `TableauSynchronisation`, `FormulaireCorrectionLieu` | Administration | `Admin.dc.html` |

Autres fichiers : `Main.dc.html` (page Design system), `Logo.dc.html` (pistes de logo, la B « point géodésique » est retenue), `Tokens.dc.html` (tokens), `IconeType.dc.html` (pictogrammes des 7 types).

## 10. Points ouverts

- La maquette **mobile** et la page **Design system** ne reprennent pas encore le menu en bulles, la loupe repliée ni le style doux partout.
- Photos réelles des lieux : à brancher sur le champ `photo` quand il existe.
- V2 : assistant (bouton flottant prévu en bas à droite) et « Mon compte » (bouton prévu en haut à droite).
