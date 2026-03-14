# splitms-java

JavaFX app built with Maven, using FXML-based views and MySQL persistence.

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

## Database (MySQL)

Start local MySQL:

```bash
docker compose -f docker/mysql-splitms/docker-compose.yml up -d
```

Run migrations:
Keep in mind first to export the ENV variables to run this.
```bash
mvn flyway:migrate
```

## Backend logic and DB operations

Add backend or data access code under `src/main/java`, for example:

- `com.splitms.db` for database access
- `com.splitms.service` for business logic

This repo reads these env vars:

- `DB_HOST`
- `DB_PORT` (defaults to 3306 for local Docker)
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
- `src/main/resources/com/splitms/views` - FXML view files
- `pom.xml` - Maven build and JavaFX plugin config

## Additional docs

- `UI_DESIGN_AND_PAGINATION.md` - explains current UI design and page navigation flow
