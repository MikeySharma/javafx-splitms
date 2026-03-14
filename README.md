# splitms-java

A split-expense desktop app built with JavaFX 25 and Maven, using FXML-based views, JPA/Hibernate for object mapping, and MySQL for persistence.

## Prerequisites

- Java 25+ (JDK)
- Maven 3.6+
- Docker (for local MySQL)

## Quickstart

**1. Start MySQL:**
```bash
docker compose -f docker/mysql-splitms/docker-compose.yml up -d
```

**2. Export environment variables:**
```bash
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=splitms
export DB_USER=<your_user>
export DB_PASSWORD=<your_password>
```

**3. Run Flyway migrations:**
```bash
mvn flyway:migrate
```

**4. Run the app:**
```bash
mvn clean javafx:run
```

## Build

```bash
mvn clean package
```

## Tests

Tests connect to the live MySQL database. The schema must be migrated before running tests (step 3 above).

```bash
mvn test
```

## Environment variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_HOST` | — | MySQL host |
| `DB_PORT` | `3306` | MySQL port |
| `DB_NAME` | — | Database name |
| `DB_USER` | — | MySQL username |
| `DB_PASSWORD` | — | MySQL password |

## Database schema

Managed by Flyway (`src/main/resources/db/migration`):

| Migration | Description |
|-----------|-------------|
| `V1__create_users.sql` | `users` table |
| `V2__create_groups.sql` | `` `group` `` table (FK → `users`) |
| `V3__create_group_members.sql` | `group_members` join table (FK → `` `group` `` and `users`) |

> **Note:** `group` is a reserved SQL keyword and is backtick-quoted in all raw SQL.

### Entity relationships

```
users  ──< group (user_id FK)
users  ──< group_members (user_id FK)
group  ──< group_members (group_id FK)
```

- A **user** owns many groups and can be a member of many groups.
- A **group** has one owner (user) and many members.
- **group_members** is a join table linking friends to groups; `(group_id, user_id)` is unique.

## Project layout

```
src/
  main/
    java/com/splitms/
      controllers/   # JavaFX FXML controllers
      entities/      # JPA entities (UserEntity, GroupsEntity, GroupMemberEntity)
      lib/           # Database and JPA helpers
      pages/         # App entry point and view navigator
      services/      # Business logic (UserService, GroupsService, GroupMembersService)
      utils/         # Env config, validation, normalization helpers
    resources/
      com/splitms/
        views/       # FXML layout files
        styles/      # CSS
      db/migration/  # Flyway SQL migrations
      META-INF/
        persistence.xml  # JPA/Hibernate configuration
  test/
    java/com/splitms/services/  # Integration tests (live DB)
```

## Tech stack

| Layer | Technology |
|-------|-----------|
| UI | JavaFX 25 + FXML |
| ORM | Hibernate 6.4.4 / Jakarta Persistence 3.1 |
| Database | MySQL (mysql-connector-j 9.4.0) |
| Migrations | Flyway 12.1.0 |
| Testing | JUnit 4.13.2 |
| Build | Maven 3, Java 25 |
