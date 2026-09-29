# GenX

**GenX** est un outil Java en ligne de commande développé par **HForge** pour automatiser la **génération de code Java** et l'**extraction d'informations géographiques depuis les fichiers OpenStreetMap `.osm.pbf`**.

Le projet est conçu comme un moteur polyvalent permettant de transformer des fichiers de configuration ou des données géographiques en résultats directement exploitables.

## Fonctionnalités

### Génération de code Java

GenX peut générer automatiquement le squelette d'une application Java à partir d'un fichier YAML :

- Entités JPA ;
- Repositories Spring Data JPA ;
- DTO ;
- Énumérations Java ;
- autres composants Java selon les générateurs disponibles.

La génération repose sur des templates **FreeMarker** situés dans :

```text
src/main/resources/templates/
```

Le package de base, le répertoire de sortie et les éléments à générer sont définis dans le fichier YAML.

### Extraction de données OpenStreetMap

GenX permet également d'extraire des informations à partir de fichiers :

```text
.osm.pbf
```

Ces fichiers sont issus des données **OpenStreetMap** et peuvent contenir des millions d'objets géographiques : nœuds, chemins et relations.

GenX peut notamment rechercher et extraire des informations correspondant à certains types d'objets géographiques.

Exemple :

```text
mosque
```

permet d'extraire les mosquées présentes dans les données OpenStreetMap.

Les résultats peuvent être exportés dans différents formats, notamment :

```text
CSV
```

---

# Architecture générale

GenX est organisé autour de deux capacités principales :

```text
                    ┌──────────────────┐
                    │      GenX        │
                    │     HForge       │
                    └────────┬─────────┘
                             │
              ┌──────────────┴──────────────┐
              │                             │
              ▼                             ▼
     ┌─────────────────┐           ┌──────────────────┐
     │ Code Generation │           │ OSM Extraction   │
     └────────┬────────┘           └────────┬─────────┘
              │                             │
              ▼                             ▼
        YAML + Templates                .osm.pbf
              │                             │
              ▼                             ▼
       Java source code                  CSV / Data
```

---

# Prérequis

GenX nécessite :

- **Java 17** ou une version plus récente ;
- le fichier JAR généré par le projet.

Exemple :

```text
target/genx-1.0-SNAPSHOT.jar
```

Vérifiez votre installation Java avec :

```cmd
java -version
```

---

# Installation

Clonez le projet puis construisez le JAR avec Maven :

```cmd
mvn clean package
```

Le fichier généré se trouve ensuite dans :

```text
target/
```

Par exemple :

```text
target/genx-1.0-SNAPSHOT.jar
```

---

# 1. Génération de code Java

## Configuration YAML

Créez un fichier de configuration, par exemple :

```text
generator.yml
```

Exemple :

```yaml
project:
  name: User Library
  groupId: com.hforge
  artifactId: user-library
  version: 1.0.0
  basePackage: com.example.user
  outputDirectory: D:/generated

entities:
  - name: User
    tableName: users
    fields:
      - name: id
        type: UUID
        id: true
        generated: true

      - name: username
        type: String
        nullable: false

enums:
  - name: UserRole
    values:
      - ADMIN
      - USER
```

`outputDirectory` correspond au répertoire dans lequel GenX va créer les fichiers générés.

Le répertoire est créé automatiquement s'il n'existe pas.

## Générer tous les composants

```cmd
java -jar target/genx-1.0-SNAPSHOT.jar --config generator.yml
```

## Générer uniquement certains composants

Entités :

```cmd
java -jar target/genx-1.0-SNAPSHOT.jar --config generator.yml --type entity
```

Repositories :

```cmd
java -jar target/genx-1.0-SNAPSHOT.jar --config generator.yml --type repository
```

DTO :

```cmd
java -jar target/genx-1.0-SNAPSHOT.jar --config generator.yml --type dto
```

Enums :

```cmd
java -jar target/genx-1.0-SNAPSHOT.jar --config generator.yml --type enums
```

Plusieurs types peuvent être générés simultanément :

```cmd
java -jar target/genx-1.0-SNAPSHOT.jar --config generator.yml --type entity,repository,dto
```

`--type all` est équivalent à l'absence de l'option `--type`.

---

# 2. Extraction OpenStreetMap

GenX peut analyser des fichiers OpenStreetMap au format :

```text
.osm.pbf
```

et extraire les informations correspondant à un type d'objet donné.

## Exemple : extraction des mosquées du Cameroun

Téléchargez d'abord le fichier OpenStreetMap du Cameroun :

**Source : Geofabrik**

https://download.geofabrik.de/africa/cameroon-latest.osm.pbf

Puis lancez :

```cmd
java -jar target/genx-1.0-SNAPSHOT.jar --type mosque --input cameroon-latest.osm.pbf --output mosques_cameroun.csv --verbose
```

Cette commande demande à GenX de :

1. lire le fichier `cameroon-latest.osm.pbf` ;
2. rechercher les objets correspondant au type `mosque` ;
3. appliquer le filtre géographique configuré ;
4. extraire les informations disponibles ;
5. générer le fichier :

```text
mosques_cameroun.csv
```

---

# Options d'extraction

Les principales options disponibles sont :

| Option | Description |
|---|---|
| `--type` | Type d'information à extraire, par exemple `mosque`. |
| `--input` | Fichier source `.osm.pbf`. |
| `--output` | Fichier de sortie, par exemple `.csv`. |
| `--verbose` | Active les informations détaillées pendant l'exécution. |
| `--no-bbox` | Désactive le filtre géographique rectangulaire du Cameroun. |
| `--dry-run` | Exécute le traitement sans produire normalement le résultat final, selon l'implémentation actuelle. |
| `--overwrite` | Contrôle l'écrasement du fichier de sortie existant. |

### Exemple avec le filtre géographique

```cmd
java -jar target/genx-1.0-SNAPSHOT.jar ^
  --type mosque ^
  --input cameroon-latest.osm.pbf ^
  --output mosques_cameroun.csv ^
  --verbose
```

### Désactiver le filtre géographique

Pour analyser les données sans appliquer le rectangle géographique du Cameroun :

```cmd
java -jar target/genx-1.0-SNAPSHOT.jar ^
  --type mosque ^
  --input cameroon-latest.osm.pbf ^
  --output mosques.csv ^
  --no-bbox
```

### Empêcher l'écrasement d'un fichier existant

```cmd
java -jar target/genx-1.0-SNAPSHOT.jar ^
  --type mosque ^
  --input cameroon-latest.osm.pbf ^
  --output mosques.csv ^
  --overwrite false
```

---

# Format des données OpenStreetMap

Les fichiers `.osm.pbf` sont des fichiers binaires compressés contenant des données OpenStreetMap.

Ils peuvent contenir notamment :

```text
Nodes
Ways
Relations
```

avec des informations telles que :

```text
name
amenity
shop
highway
building
latitude
longitude
```

GenX exploite ces informations pour identifier les objets correspondant au type demandé.

Par exemple, une mosquée peut être identifiée à partir de tags OpenStreetMap tels que :

```text
amenity=place_of_worship
religion=muslim
```

Les informations extraites peuvent ensuite être exportées dans un fichier CSV exploitable avec Excel, Python, Java, PostgreSQL/PostGIS ou d'autres outils d'analyse de données.

---

# Exemple de workflow

Un workflow typique avec GenX peut être :

```text
             OpenStreetMap
                    │
                    ▼
        cameroon-latest.osm.pbf
                    │
                    ▼
             ┌─────────────┐
             │    GenX     │
             │   HForge    │
             └──────┬──────┘
                    │
                    ▼
          Extraction des données
                    │
                    ▼
            mosques_cameroun.csv
```

Le fichier CSV peut ensuite être utilisé pour :

- analyser les données ;
- créer une carte ;
- alimenter une base de données ;
- effectuer des statistiques ;
- développer une application Java ;
- effectuer des traitements avec Python ;
- importer les données dans un SIG.

---

# Arguments disponibles

| Argument | Exemple | Description |
|---|---|---|
| `--config` | `generator.yml` | Fichier YAML utilisé pour la génération de code. |
| `--type` | `entity` | Type de génération ou d'extraction. |
| `--input` | `cameroon-latest.osm.pbf` | Fichier `.osm.pbf` source. |
| `--output` | `mosques.csv` | Fichier de sortie. |
| `--verbose` | — | Affiche davantage d'informations pendant l'exécution. |
| `--no-bbox` | — | Désactive le filtre géographique du Cameroun. |
| `--dry-run` | — | Mode simulation, selon le traitement concerné. |
| `--overwrite` | `true` / `false` | Autorise ou non l'écrasement des fichiers existants. |

---

# Exemple complet

Extraction des mosquées du Cameroun :

```cmd
java -jar target/genx-1.0-SNAPSHOT.jar ^
  --type mosque ^
  --input cameroon-latest.osm.pbf ^
  --output mosques_cameroun.csv ^
  --verbose
```

Avec le filtre géographique désactivé :

```cmd
java -jar target/genx-1.0-SNAPSHOT.jar ^
  --type mosque ^
  --input cameroon-latest.osm.pbf ^
  --output mosques.csv ^
  --no-bbox ^
  --verbose
```

---

# Génération de code et extraction

GenX regroupe donc deux domaines complémentaires :

### Code Generation

```text
YAML
 │
 ▼
GenX
 │
 ├── Entity
 ├── Repository
 ├── DTO
 └── Enum
 │
 ▼
Java source code
```

### OSM Data Extraction

```text
.osm.pbf
 │
 ▼
GenX
 │
 ├── Filtering
 ├── Extraction
 └── Transformation
 │
 ▼
CSV / Data
```

Cette architecture permet à GenX d'évoluer progressivement vers une plateforme d'automatisation capable de combiner **génération de code, extraction, transformation et traitement de données**.

---

# Limites actuelles

Certaines fonctionnalités peuvent encore être en cours de développement.

Concernant la génération Java :

- les fichiers générés constituent principalement des squelettes ;
- certaines annotations Spring/JPA ou certains imports peuvent nécessiter des ajustements ;
- les générateurs de services et contrôleurs peuvent exister dans le code source sans être encore exposés par l'application principale.

Concernant l'extraction OSM :

- les types d'objets disponibles dépendent des extracteurs implémentés ;
- la structure des données extraites dépend des tags présents dans OpenStreetMap ;
- les résultats peuvent varier selon la version du fichier `.osm.pbf` utilisée.

---

# Développement

Le projet est développé en **Java** et utilise notamment :

- Java 17+ ;
- Maven ;
- FreeMarker pour la génération de code ;
- osm4j pour la lecture des données OpenStreetMap ;
- Protocol Buffers pour le traitement des données PBF ;
- Jackson/YAML pour la configuration.

---

# HForge

**GenX** est un projet développé et maintenu par **HForge**.

> **GenX — Generate. Extract. Transform.**

HForge développe des outils logiciels destinés à automatiser la génération de code et le traitement des données.