# splitms-java

JavaFX app built with Maven.

## Prerequisites

- Java 25+ (JDK)
- Maven 3.6+

## Start (run the app)

```bash
mvn clean javafx:run
```

## Build

```bash
mvn clean package
```

## Backend logic and DB operations

Add backend or data access code under `src/main/java`, for example:

- `com.splitms.db` for database access
- `com.splitms.service` for business logic

This repo includes a simple PostgreSQL helper in `com.splitms.db.Database` that reads these env vars:

- `DB_HOST`
- `DB_PORT` (defaults to 5432 if not set)
- `DB_NAME`
- `DB_USER`
- `DB_PASSWORD`

## Project initialization

```bash
mvn archetype:generate \
	-DarchetypeGroupId=org.openjfx \
	-DarchetypeArtifactId=javafx-archetype-simple \
	-DarchetypeVersion=0.0.6 \
	-DgroupId=com.splitms \
	-DartifactId=splitms-java \
	-Dversion=1.0-SNAPSHOT \
	-DinteractiveMode=false
```

## Project layout

- `src/main/java` - Java sources
- `pom.xml` - Maven build and JavaFX plugin config
