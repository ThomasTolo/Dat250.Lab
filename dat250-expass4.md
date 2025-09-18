# Why We Use `persistence.xml`

The `persistence.xml` file is a standard configuration file required by JPA. It defines the persistence unit, which includes settings for the database connection, JPA provider (such as Hibernate), entity scanning, and additional properties (like dialect, schema generation, and connection pool settings). This file allows the application to know how to connect to the database and how to manage entities. Without `persistence.xml`, JPA cannot initialize the persistence context or entity manager.

# Why We Removed `import jakarta.persistence.PersistenceConfiguration;`

The import `jakarta.persistence.PersistenceConfiguration;` was removed because it does not exist in the standard JPA API. JPA configuration is handled via the `persistence.xml` file and not through a `PersistenceConfiguration` class. Keeping this import would cause compile errors. All necessary configuration should be placed in `persistence.xml` or as properties in the application configuration files.
# DAT250 Experiment Assignment 4 Report

## Technical Problems Encountered with JPA

During the installation and use of Java Persistence Architecture (JPA) with Hibernate and H2, several technical issues were encountered:

- **Identifier Generation Exception:** Initially, entities used `UUID` as IDs, which caused `IdentifierGenerationException` because JPA/Hibernate expects auto-generated numeric IDs by default. This was resolved by switching all entity IDs to `Long` and annotating them with `@GeneratedValue(strategy = GenerationType.IDENTITY)`.
- **Lock Timeout Errors:** The H2 in-memory database would sometimes throw lock timeout errors during tests. This was fixed by adding `;LOCK_TIMEOUT=10000` to the JDBC URL in `persistence.xml` for both main and test resources.
- **Type Mismatches:** After converting IDs to `Long`, some service and controller methods still used `UUID`, causing compile errors. All method signatures and usages were updated to use `Long` for consistency.
- **JPA Provider Not Found:** Early on, a missing persistence provider error was resolved by ensuring the correct Hibernate dependencies were included in the Gradle build file.

## Link to Code (Experiment 2)

[GitHub Repository - Dat250.Lab](https://github.com/ThomasTolo/Dat250.Lab)

All test cases, including the provided test for experiment 2, pass successfully after the above fixes.

## Inspecting Database Tables

To inspect the database tables created by JPA/Hibernate, the H2 web console was used. The following steps were taken:

1. Started the application with the H2 database enabled. (/Users/thoma/.gradle/caches/modules-2/files-2.1/com.h2database/h2/...)
2. Accessed the H2 console at `http://localhost:8082` (or as configured).
3. Connected using the JDBC URL from `persistence.xml`.
4. Ran `SHOW TABLES;` and `SELECT * FROM <table>;` to view the structure and contents.

**Tables Created:**
- USER
- POLL
- VOTE_OPTION
- VOTE

These tables correspond to the JPA entities. Screenshots of the H2 console showing the tables and sample data are included below:

![Database](image.png)

## Pending Issues

At the time of submission, all major issues have been resolved. All tests pass, and the application works as expected. No critical pending issues remain.

---

*This report is part of DAT250 Experiment Assignment 4. For questions, contact Thomas Tolo.*
