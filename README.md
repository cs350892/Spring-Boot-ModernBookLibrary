# 📚 Book Library REST API — Architecture, Workflow & Learning Guide

A production-ready Spring Boot 3 / 4 RESTful microservice built to manage a book catalog, demonstrating enterprise backend design patterns (Layered Architecture, Dependency Injection, Spring Data JPA, and automated testing) through the lens of a Frontend & Node.js developer.

---

## 📑 Table of Contents
1. [Project Overview](#-project-overview)
2. [How the Project Was Approached](#-how-the-project-was-approached)
3. [Architecture & Workflow](#-architecture--workflow)
   - [Layered Architecture Diagram](#layered-architecture-diagram)
   - [Request-Response Lifecycle](#request-response-lifecycle)
   - [Mental Model: Node.js vs Spring Boot](#mental-model-nodejs-vs-spring-boot)
4. [Project Structure](#-project-structure)
5. [Commands Guide (Run, Test, Build, Stop)](#-commands-guide)
   - [Starting the Application](#1-starting-the-application)
   - [Stopping the Application](#2-stopping-the-application)
   - [Testing the Application](#3-running-automated-tests)
   - [Building for Production](#4-building-for-production)
6. [API Endpoints & Testing Examples](#-api-endpoints--testing-examples)
7. [Bugs Encountered & How They Were Solved](#-bugs-encountered--how-they-were-solved)
8. [What Did I Learn? (Key Takeaways)](#-what-did-i-learn-key-takeaways)

---

## 🌟 Project Overview

- **Framework**: Spring Boot 4 / Java 21+
- **Persistence**: Spring Data JPA & Hibernate
- **Databases**:
  - **In-Memory H2** (Active by default for zero-setup, instant testing)
  - **PostgreSQL** (Pre-configured; switchable via `application.properties`)
- **Testing**: JUnit 5 & Spring Boot Test
- **Build Tool**: Apache Maven (`mvnw`)

---

## 🛠 How the Project Was Approached

To build a clean, maintainable REST API, the project was approached systematically from the inside out:

```
  [1. Domain Entity] ➡️ [2. Data Repository] ➡️ [3. Business Service] ➡️ [4. REST Controller] ➡️ [5. Integration Tests]
```

1. **Step 1: Domain Entity (`Book.java`)**
   - Modeled the database table using JPA annotations (`@Entity`, `@Table`, `@Id`, `@GeneratedValue`, `@Column`).
   - Defined core attributes: `id`, `title`, `author`, `isbn` (unique constraint), `price`, and `publishedYear`.
   - Included both no-arg and parameterized constructors for Jackson and Hibernate reflection.

2. **Step 2: Data Access Layer (`BookRepository.java`)**
   - Created an interface extending `JpaRepository<Book, Long>`.
   - Avoided writing manual SQL queries. Spring Data JPA automatically provides standard CRUD operations (`save`, `findAll`, `findById`, `deleteById`) and dynamically derives custom SQL queries based on method signatures (`findByIsbn`, `findByAuthor`, `findByPriceLessThan`).

3. **Step 3: Service Layer (`BookService.java`)**
   - Encapsulated business logic away from HTTP concerns.
   - Injected `BookRepository` using modern **Constructor Injection** instead of field-based `@Autowired` (making it testable, immutable, and clean).
   - Handled null safety and optional values gracefully using Java's `Optional<Book>`.

4. **Step 4: Controller Layer (`BookController.java`)**
   - Annotated with `@RestController` and `@RequestMapping("/api/books")`.
   - Added `@CrossOrigin(origins = "*")` so single-page frontend apps (React, Vue, Angular, Vite) can query the API without browser CORS blocks.
   - Mapped HTTP verbs (`GET`, `POST`, `PUT`, `DELETE`) with proper HTTP status codes (`201 Created`, `200 OK`, `404 Not Found`).

5. **Step 5: Database & Testing Configuration**
   - Configured `application.properties` with an in-memory H2 database with automatic schema generation (`spring.jpa.hibernate.ddl-auto=update`) and the H2 Web Console.
   - Wrote comprehensive integration tests in `BookLibraryApplicationTests.java` verifying all endpoints, CRUD operations, and edge cases.

---

## 🏗 Architecture & Workflow

### Layered Architecture Diagram

```
+-------------------------------------------------------------------------+
|                              Client Layer                               |
|        (Browser, React / Vue Frontend, Postman, Mobile App)             |
+-------------------------------------------------------------------------+
                                    |
                                    | HTTP Requests (JSON)
                                    v
+-------------------------------------------------------------------------+
|                  Presentation Layer (@RestController)                   |
|  - BookController                                                       |
|  - Deserializes JSON payloads into Java objects                         |
|  - Handles HTTP status codes (200, 201, 404) & CORS                    |
+-------------------------------------------------------------------------+
                                    |
                                    | Method Invocations
                                    v
+-------------------------------------------------------------------------+
|                    Business Service Layer (@Service)                    |
|  - BookService                                                          |
|  - Executes business rules, validations, entity mutations              |
+-------------------------------------------------------------------------+
                                    |
                                    | Method Invocations
                                    v
+-------------------------------------------------------------------------+
|                  Data Access Layer (@Repository / JPA)                  |
|  - BookRepository extends JpaRepository<Book, Long>                     |
|  - Translates method calls into SQL via Hibernate ORM                   |
+-------------------------------------------------------------------------+
                                    |
                                    | JDBC / SQL Queries
                                    v
+-------------------------------------------------------------------------+
|                             Database Layer                              |
|   - In-Memory H2 DB (default)  OR  - PostgreSQL (localhost:5432)        |
+-------------------------------------------------------------------------+
```

### Request-Response Lifecycle

1. **Client Request**: A client sends `POST http://localhost:8080/api/books` with a JSON body.
2. **Tomcat & DispatcherServlet**: Embedded Tomcat web server forwards the HTTP request to Spring's `DispatcherServlet`.
3. **Controller Handling**: `BookController.addBook()` receives the request. Jackson deserializes the JSON body into a `Book` entity object.
4. **Service Execution**: Controller delegates to `BookService.addBook(book)`.
5. **Persistence**: `BookService` calls `BookRepository.save(book)`.
6. **Hibernate ORM**: Hibernate generates `INSERT INTO books (...) VALUES (...)` and executes it against the database (H2 or PostgreSQL).
7. **Response Serialization**: The saved entity with its generated `id` flows back up the stack. `BookController` returns a `ResponseEntity<Book>` with status `201 Created` which Jackson serializes back to JSON for the client.

---

### Mental Model: Node.js vs Spring Boot

For developers coming from Node.js (Express / TypeScript / NestJS / Prisma / Mongoose):

| Concept | Node.js / Express / TypeScript | Spring Boot (Java) | What it does |
| :--- | :--- | :--- | :--- |
| **Project & Dependency Spec** | `package.json` / `package-lock.json` | `pom.xml` (Maven) | Declares dependencies, build plugins, and Java version |
| **Server Startup** | `app.listen(8080)` in `index.ts` | `BookLibraryApplication.java` (`main()`) | Launches embedded Tomcat web server |
| **Routing / Controller** | `router.post('/books', ...)` / NestJS `@Controller` | `@RestController` + `@RequestMapping` | Maps URLs and HTTP methods to Java controller functions |
| **Business Logic** | `services/bookService.ts` / NestJS `@Injectable` | `@Service` | Contains domain business logic and data manipulation |
| **ORM / Data Access** | Prisma Client, TypeORM, Mongoose Model | `BookRepository` (`JpaRepository`) | Automatic query methods without writing manual SQL |
| **Data Schema / Model** | Prisma `schema.prisma`, Mongoose Schema | `@Entity` (`Book.java`) | Maps class fields to database table columns and constraints |
| **Configuration** | `.env` / `dotenv` | `application.properties` | Configures port, datasources, and JPA settings |
| **Dependency Injection** | `new Service()` or NestJS DI Container | Spring IoC Container (`@Autowired` / Constructor) | Automatically instantiates and wires beans together |

---

## 📁 Project Structure

```
bookLibrary/
├── .mvn/wrapper/                  # Maven wrapper executables
├── pom.xml                        # Dependencies & build configuration
├── HELP.md                        # Default Spring Boot help notes
├── README.md                      # This comprehensive architecture & learning guide
└── src/
    ├── main/
    │   ├── java/com/example/booklibrary/
    │   │   ├── BookLibraryApplication.java     # 🚀 Application Main Entrypoint
    │   │   ├── controller/
    │   │   │   └── BookController.java         # 🌐 REST Endpoints & HTTP handling
    │   │   ├── entity/
    │   │   │   └── Book.java                   # 📦 JPA Entity (Database Table Model)
    │   │   ├── repository/
    │   │   │   └── BookRepository.java         # 💾 Spring Data JPA Repository
    │   │   └── service/
    │   │       └── BookService.java            # ⚙️ Business Logic Service
    │   └── resources/
    │       └── application.properties          # ⚙️ DB & Server Settings (H2 & Postgres)
    └── test/
        └── java/com/example/booklibrary/
            └── BookLibraryApplicationTests.java # 🧪 Comprehensive Integration Tests
```

---

## 💻 Commands Guide

### 1. Starting the Application

#### In PowerShell:
```powershell
.\mvnw spring-boot:run
```

#### In Windows Command Prompt (CMD):
```cmd
mvnw.cmd spring-boot:run
```

#### Directly from your IDE:
Open `BookLibraryApplication.java` and click the **Run ▶** button on the `main` method.

> When started, the API runs at: **`http://localhost:8080`**  
> H2 Web Console is accessible at: **`http://localhost:8080/h2-console`**  
> *(JDBC URL: `jdbc:h2:mem:booklibrary_db`, User: `sa`, Password: leave blank)*

---

### 2. Stopping the Application

#### Standard Stop:
Press **`Ctrl + C`** in the terminal where the server is running (type `Y` and press Enter when prompted).

#### Force Stop (If port 8080 remains stuck in background):
Run this in **PowerShell**:
```powershell
Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue | ForEach-Object { Stop-Process -Id $_.OwningProcess -Force }
```
Or in **Command Prompt (CMD)**:
```cmd
for /f "tokens=5" %a in ('netstat -ano ^| findstr :8080') do taskkill /F /PID %a
```

---

### 3. Running Automated Tests

Run the test suite to verify all endpoints and database interactions:
```powershell
.\mvnw test
```

### 4. Building for Production

Compile, run tests, and package a standalone runnable `.jar` file:
```powershell
.\mvnw clean package
```
The compiled JAR is generated in `target/book-library-0.0.1-SNAPSHOT.jar` and can be executed anywhere with:
```powershell
java -jar target/book-library-0.0.1-SNAPSHOT.jar
```

---

## 📡 API Endpoints & Testing Examples

You can test these endpoints using PowerShell, Postman, or `curl`:

### 1. Create a Book (`POST /api/books`)
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/books" -Method Post -ContentType "application/json" -Body '{
  "title": "Clean Code",
  "author": "Robert C. Martin",
  "isbn": "978-0132350884",
  "price": 450,
  "publishedYear": 2008
}'
```

### 2. Get All Books (`GET /api/books`)
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/books" -Method Get
```

### 3. Get Book by ID (`GET /api/books/{id}`)
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/books/1" -Method Get
```

### 4. Get Book by ISBN (`GET /api/books/isbn/{isbn}`)
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/books/isbn/978-0132350884" -Method Get
```

### 5. Get Books by Author (`GET /api/books/author/{author}`)
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/books/author/Robert C. Martin" -Method Get
```

### 6. Filter Books Cheaper Than Price (`GET /api/books/price-less-than/{price}`)
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/books/price-less-than/500" -Method Get
```

### 7. Update a Book (`PUT /api/books/{id}`)
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/books/1" -Method Put -ContentType "application/json" -Body '{
  "title": "Clean Code (Updated Edition)",
  "author": "Robert C. Martin",
  "isbn": "978-0132350884",
  "price": 500,
  "publishedYear": 2023
}'
```

### 8. Delete a Book (`DELETE /api/books/{id}`)
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/books/1" -Method Delete
```

---

## 🐛 Bugs Encountered & How They Were Solved

During development and troubleshooting of this project, several important errors were diagnosed and resolved:

### 1. The Disappearing Main Class & Duplicate `BookService.java`
- **Symptom**: Compilation failed with `duplicate class: com.example.booklibrary.service.BookService` and `Cannot find main class`.
- **Cause**: While editing, the root `BookLibraryApplication.java` was accidentally replaced/renamed as a duplicate `BookService.java` placed in `com.example.booklibrary`, while another complete `BookService.java` already lived in `com.example.booklibrary.service`.
- **Solution**: Re-created [`BookLibraryApplication.java`](file:///c:/Users/chandra.shekhar/Downloads/Frontend-NAC/Tutorial/bookLibrary/src/main/java/com/example/booklibrary/BookLibraryApplication.java) with `@SpringBootApplication` and removed the extraneous root duplicate.

### 2. Java Package vs Directory Strictness
- **Symptom**: `package com.example.booklibrary.service does not match directory com/example/booklibrary`.
- **Cause**: In Node.js, file location can be relative and doesn't restrict class declaration. In Java, the `package` declaration must strictly match the folder path on disk.
- **Solution**: Organized classes strictly into their matching directories: `entity/`, `repository/`, `service/`, and `controller/`.

### 3. Syntax & Typos in JPA Entity
- **Symptom**: `package jakarta.persistance does not exist`, `@Coulumn cannot find symbol`.
- **Cause**: Typos in annotations (`persistance` with an 'a' instead of 'e', and `Coulumn` with a 'u').
- **Solution**: Corrected to `jakarta.persistence.*` and `@Column(...)`.

### 4. Method Naming in Spring Data JPA
- **Symptom**: `No property 'findPrice' found for type 'Book'`.
- **Cause**: Method named `findPriceLessThan` violated Spring Data JPA naming rules.
- **Solution**: Named it `findByPriceLessThan(Long price)`. Spring parses `findBy` + `Property` (`Price`) + `Keyword` (`LessThan`).

### 5. Missing Database Configuration on Startup
- **Symptom**: `Failed to configure a DataSource: 'url' attribute is not specified`.
- **Cause**: Spring Boot with `spring-boot-starter-data-jpa` requires an active database datasource at startup.
- **Solution**: Added `h2` dependency and configured zero-setup in-memory database in `application.properties`, keeping PostgreSQL configuration commented and ready for production use.

---

## 💡 What Did I Learn? (Key Takeaways)

### 1. Spring Data JPA Magic (Derived Queries)
In Node.js/Express, we write SQL strings (`SELECT * FROM books WHERE author = $1`) or ORM calls (`prisma.book.findMany({ where: { author } })`).  
In Spring Data JPA, **simply writing the method signature** in an interface causes Spring to generate the implementation and SQL at runtime:
```java
List<Book> findByAuthor(String author);
// Auto-generates: SELECT * FROM books WHERE author = ?

List<Book> findByPriceLessThan(Long price);
// Auto-generates: SELECT * FROM books WHERE price < ?
```

### 2. Constructor Injection vs Field `@Autowired`
While `@Autowired` on private fields is common in legacy tutorials, modern Spring best practices strongly favor **Constructor Injection**:
```java
@Service
public class BookService {
    private final BookRepository repository;

    public BookService(BookRepository repository) {
        this.repository = repository; // Clean, immutable, easily mocked in unit tests
    }
}
```
Spring automatically supplies the dependency without needing `@Autowired` above the constructor.

### 3. Clean Separation of Concerns (Layered Pattern)
- **Controller**: Understands HTTP, requests, response codes, URLs. Knows **nothing** about SQL or tables.
- **Service**: Understands business logic, validations, updates. Knows **nothing** about HTTP or JSON.
- **Repository**: Understands SQL queries and database interactions. Knows **nothing** about controllers.
This modularity ensures that changes to the database do not break controllers, and UI/API routing changes do not touch business logic.

### 4. Zero-Setup Rapid Prototyping with H2
By using an in-memory H2 database during development:
- The entire team can clone and immediately run `.\mvnw spring-boot:run` or `.\mvnw test` without installing or configuring a local database server.
- Tests remain isolated, fast, and repeatable.
- Switching to PostgreSQL in production only requires updating 4 lines in `application.properties`.

---
*Created as part of the Spring Boot Book Library Tutorial.*
