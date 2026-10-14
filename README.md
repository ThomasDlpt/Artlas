# Artlas
```Application web cartographique pour la découverte et la valorisation du patrimoine culturel (musées, galeries, monuments).```

## OBJECTIF DU PROJET

```
L'objectif du projet est de concevoir une application cartographique interactive permettant aux utilisateurs d'explorer, localiser et de découvrir
les différents lieux culturels en France. Artlas vise à faciliter l'accès à la culture en s'appuyant sur les données de l'État. À terme, un chatbot sera 
mis en place afin de suggérer des itinéraires de visite et répondre aux questions.
```

## CONTEXTE

```
La France dispose d’un patrimoine culturel d’une richesse exceptionnelle, comptant des milliers de musées, galeries d'art et monuments historiques répartis 
sur l'ensemble du territoire. Le gouvernement met à disposition des bases de données ouvertes exhaustives (data.gouv.fr, base Muséofile), ces données restent brutes 
et peu accessibles au grand public. Les outils existants se réduisent à des répertoires textuels ou à des cartographies marchandes (Google Maps, Apple Plan ...), favorisant la 
visibilité des pôles touristiques majeurs au détriment du patrimoine de proximité.
```

## FONCTIONNALITÉS (V1)

- **Carte Interactive**: 

```Rendu cartographique les lieux culturels grace à la géolocalisation sur OpenStreetMap.```

- **Cherche et Filtres**: 

```Recherche textuelle et filtrage des lieux par localisation géographique.```

- **Fiches descriptives**: 

```Visualisation détaillée des informations sur le lieu sélectionné par l'utilisateur.```

## FONCTIONNALITÉS À DEVELOPPER PLUS TARD (V2)

- **IA/CHATBOT**: 

```Aide l'utilisateur à découvrir de nouveaux lieux et de nouveaux itinéraires en le guidant dans sa recherche à l'aide de filtres.```


## TECHNOLOGIES UTILISÉES

* **[BACKEND]**: 
    * **JAVA**: ```Gestion de la logique métier.```

* **[DATA]**: 
  * **JAVA (SpringBoot)**: ```Filtrage des données, normalisation de la donnée.```

* **[CARTOGRAPHIE]**:
    * **OpenStreetMap**: ```Rendu cartographique.```
  
* **[FRONTEND]**: ```À déterminer``` 

* **[IA/CHATBOT]**: 
  * **PYTHON** 
  * **(Ollama ?)**

## COUVERTURE DE TEST

* **SP1 (V1)** : ```Objectif minimal de **50 %** de couverture de code.```
* **SP2 (V2)** : ```Objectif cible de **70 % à 80 %** sur les services métier.```

## PARTICIPANTS

```
- KARLOV Youri
- BEUREL Antoine
- HAMAZ Chihab
- DELAPORTE Thomas
```