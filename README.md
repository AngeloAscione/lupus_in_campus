# Lupus in Campus – Backend Server

**Lupus in Campus** is a multiplayer Android app inspired by the classic deduction game *Lupus in Tabula* (Werewolf), developed to bring the thrilling gameplay experience into the digital world — even remotely. This document describes the backend server component, implemented in **Java with Spring Boot**, designed to manage game logic, user data, matchmaking, and communication.

---

## Project Purpose

The backend provides RESTful services that support the following core features:

- Player registration and authentication
- Creation and management of public/private/local lobbies
- Automatic role assignment and turn-based logic
- Chat system (text and voice)
- Friends list and social interactions
- Notification management (invites, errors, updates)

The system is designed with educational, collaborative, and entertainment goals, reflecting the student life and culture of the **University of Salerno**.

---

## System Architecture

The backend follows the **MVC pattern**, with the following major modules:

- **Authentication**: Login, logout, registration, password reset
- **User Management**: Profile, match history, friend list
- **Lobby Management**: Create, join, leave, and invite to lobbies
- **Game Management**: Turn control, vote handling, role assignment
- **Communication System**: Text and voice chat logic
- **Notification System**: Push notifications (game state, invites)
- **Persistence**: JPA-based data storage on Azure SQL/MySQL

Each subsystem is mapped to dedicated controllers, services, and repositories.

---

## Tech Stack

- Java 17
- Spring Boot 3
- Spring MVC
- Spring Data JPA (Hibernate)
- MySQL / Azure SQL
- JUnit 5 + Mockito (unit testing)
- JaCoCo (code coverage analysis)
- Maven (build & dependency management)
- REST APIs (JSON over HTTP)

---

## Security

- Password hashing via BCrypt
- Token-based user session management
- Role-based access control
- Data access auditing and logging
- Conforms to ISO/IEC 27001 standards for data protection

---

## Testing & Validation

All core use cases are fully covered with unit tests:

- UC_RG: Register a Player
- UC_CL: Create a Lobby
- UC_ELOPM: Join a Public Lobby
- UC_IRRM: Add Friend by Manual Search

Test framework and tools:

- JUnit 5
- Mockito (mocking)
- JaCoCo (minimum 70% branch coverage)
- Maven Surefire Plugin

**Test Results**: 18/18 tests passed, 0 failures.

---

## Directory Structure

```
lupus-server/
|
├── src/
│   ├── main/java/
│   │   └── it/unisa/lupus/
│   │       ├── controller/     # REST endpoints
│   │       ├── service/        # Business logic
│   │       ├── model/          # JPA entities
│   │       └── repository/     # DB access
│   └── resources/
│       └── application.properties
├── pom.xml                     # Maven config
```

---

## Configuration

Example `application.properties`:

```
spring.datasource.url=jdbc:mysql://localhost:3306/lupus
spring.datasource.username=root
spring.datasource.password=yourpassword
spring.jpa.hibernate.ddl-auto=update
```

---

## How to Run

### Prerequisites

- JDK 17+
- MySQL or Azure SQL instance
- Maven 3.x

### Launch Server

```
mvn clean install
mvn spring-boot:run
```

Server will be available at: `http://localhost:8080`

---

## Contributors

- Angelo Ascione – a.ascione19@studenti.unisa.it
- Federica Graziuso – f.graziuso1@studenti.unisa.it
- Stefano Gagliarde – s.gagliarde@studenti.unisa.it
- Christian Izzo – c.izzo43@studenti.unisa.it

---

## License

This project is licensed under the MIT License. Feel free to use, learn from, and adapt it for educational or research purposes.

---

> Developed for Prof. Carmine Gravino – University of Salerno, Academic Year 2024/2025.
