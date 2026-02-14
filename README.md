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
