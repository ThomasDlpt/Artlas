# CAHIER DE RECETTE — Artlas (V1)

```Ce document liste les scénarios de test fonctionnels permettant de valider que l'application répond aux besoins exprimés (cf. README.md et diagramme de cas d'utilisation V1).```

## INFORMATIONS DE RECETTE

| Élément | Valeur |
|---|---|
| Version testée | V1 (SP1) |
| Date de recette | JJ/MM/AAAA |
| Testeur(s) | |
| Environnement | Navigateur (Chrome / Firefox), backend Spring Boot local |
| Jeu de données | Base Muséofile / data.gouv.fr (import du JJ/MM/AAAA) |

## LÉGENDE DES STATUTS

- ✅ **OK** : ```Le résultat obtenu est conforme au résultat attendu.```
- ❌ **KO** : ```Le résultat obtenu diffère du résultat attendu (créer une issue et noter son numéro).```
- ⏸️ **NT** : ```Non testé.```

---

## 1. VISUALISER LA CARTE

| ID | Scénario | Préconditions | Étapes | Résultat attendu | Résultat obtenu | Statut |
|---|---|---|---|---|---|---|
| CA-01 | Affichage de la carte | Application lancée | Ouvrir la page d'accueil | La carte OpenStreetMap s'affiche avec les marqueurs des lieux culturels | | NT |
| CA-02 | Zoomer / Dézoomer | Carte affichée | Utiliser la molette ou les boutons +/- | Le niveau de zoom change, les marqueurs restent positionnés correctement | | NT |
| CA-03 | Déplacement sur la carte | Carte affichée | Cliquer-glisser sur la carte | La carte se déplace, les marqueurs de la nouvelle zone s'affichent | | NT |

## 2. RECHERCHER UN LIEU

| ID | Scénario | Préconditions | Étapes | Résultat attendu | Résultat obtenu | Statut |
|---|---|---|---|---|---|---|
| RE-01 | Recherche par nom existant | Carte affichée | Saisir « Louvre » dans la barre de recherche | Le lieu apparaît dans les résultats et la carte se centre dessus | | NT |
| RE-02 | Recherche sans résultat | Carte affichée | Saisir « zzzzzz » | Un message « Aucun lieu trouvé » s'affiche | | NT |
| RE-03 | Recherche vide | Carte affichée | Valider une recherche vide | Aucune erreur, tous les lieux restent affichés | | NT |

## 3. FILTRER LES LIEUX

| ID | Scénario | Préconditions | Étapes | Résultat attendu | Résultat obtenu | Statut |
|---|---|---|---|---|---|---|
| FI-01 | Filtre valide (nominal) | Carte affichée | Sélectionner un filtre (ex : région « Bretagne ») | Seuls les lieux correspondant au filtre sont affichés | | NT |
| FI-02 | Filtres erronés | Carte affichée | Saisir une valeur de filtre invalide | Un message d'erreur explicite s'affiche, la carte n'est pas modifiée | | NT |
| FI-03 | Base de données inaccessible | Base de données arrêtée | Lancer une recherche avec filtres | Un message « Service indisponible » s'affiche, l'application ne plante pas | | NT |
| FI-04 | Réinitialiser les filtres | Filtre actif | Cliquer sur « Réinitialiser » | Tous les lieux sont de nouveau affichés | | NT |

## 4. CONSULTER LA FICHE DESCRIPTIVE

| ID | Scénario | Préconditions | Étapes | Résultat attendu | Résultat obtenu | Statut |
|---|---|---|---|---|---|---|
| FD-01 | Ouverture d'une fiche | Carte affichée | Cliquer sur un marqueur | La fiche s'affiche (nom, adresse, type, horaires, description…) | | NT |
| FD-02 | Ouverture depuis la recherche | Résultats de recherche affichés | Cliquer sur un résultat | La fiche du lieu s'affiche | | NT |
| FD-03 | Lieu avec informations manquantes | Lieu sans description en base | Ouvrir sa fiche | Les champs manquants sont indiqués « Non renseigné », pas d'erreur | | NT |

## 5. CRÉER UN CIRCUIT

| ID | Scénario | Préconditions | Étapes | Résultat attendu | Résultat obtenu | Statut |
|---|---|---|---|---|---|---|
| CI-01 | Ajouter une étape | Fiche d'un lieu ouverte | Cliquer sur « Ajouter au circuit » | Le lieu est ajouté à la liste des étapes | | NT |
| CI-02 | Réordonner les étapes | Circuit avec au moins 2 étapes | Déplacer une étape dans la liste | L'ordre est mis à jour et le tracé sur la carte suit le nouvel ordre | | NT |
| CI-03 | Supprimer une étape | Circuit avec au moins 1 étape | Cliquer sur « Supprimer » d'une étape | L'étape est retirée du circuit | | NT |
| CI-04 | Sauvegarder le circuit | Circuit avec au moins 2 étapes | Cliquer sur « Sauvegarder » | Un message de confirmation s'affiche, le circuit est retrouvé après rechargement | | NT |
| CI-05 | Ajouter un lieu en double | Lieu déjà présent dans le circuit | Ajouter à nouveau le même lieu | Un message d'avertissement s'affiche | | NT |

## 6. ADMINISTRATION / DONNÉES

| ID | Scénario | Préconditions | Étapes | Résultat attendu | Résultat obtenu | Statut |
|---|---|---|---|---|---|---|
| AD-01 | Synchronisation manuelle | Connecté en administrateur | Lancer une synchronisation manuelle | Les données sont importées, un récapitulatif (nb de lieux importés) s'affiche | | NT |
| AD-02 | Import avec lignes erronées | Fichier source contenant des lignes invalides | Lancer l'import | Les lignes valides sont importées, les lignes erronées sont ignorées et listées dans un rapport | | NT |
| AD-03 | Masquer un lieu | Connecté en administrateur | Masquer un lieu | Le lieu n'apparaît plus sur la carte côté utilisateur | | NT |
| AD-04 | Corriger un lieu | Connecté en administrateur | Modifier l'adresse d'un lieu | La fiche affiche la nouvelle adresse | | NT |

---

## BILAN DE LA RECETTE

| Total | ✅ OK | ❌ KO | ⏸️ NT |
|---|---|---|---|
| 22 | 0 | 0 | 22 |

**Anomalies relevées :**

| ID test | Description de l'anomalie | Gravité (Bloquante / Majeure / Mineure) | Issue |
|---|---|---|---|
| | | | |

**Décision :** ```Recette validée / Recette validée avec réserves / Recette refusée```
