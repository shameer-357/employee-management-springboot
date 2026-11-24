# 🏢 Employee Management System - Spring Boot Backend

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.5-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6.x-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-JJWT_0.12.5-black?style=for-the-badge&logo=jsonwebtokens&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.4-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Swagger](https://img.shields.io/badge/OpenAPI-Swagger_UI-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)

A production-grade, enterprise-ready RESTful backend built with **Spring Boot 3.3.5**, **Spring Data JPA**, and **Spring Security 6**. The application implements robust Role-Based Access Control (RBAC) via stateless JWT tokens, input validation, structured error handling, automated database initialization, and full containerization with multi-stage Docker builds.

---

## 🌟 Key Features

- **🔐 JWT Authentication & Stateless Architecture:**
  - Secure login with BCrypt password hashing (`PasswordEncoder.matches()`).
  - Access tokens generated via JJWT 0.12.5 using cryptographic keys (`Keys.hmacShaKeyFor`) and role claims.
  - Fully stateless security session policy (`SessionCreationPolicy.STATELESS`).
- **🛡️ Role-Based Access Control (RBAC):**
  - Granular authorities (`ROLE_ADMIN`, `ROLE_USER`) populated in `SecurityContextHolder`.
  - Sensitive operations (creating, updating, deleting employees and managing users) restricted strictly to `ADMIN`.
  - Profile retrieval and employee queries accessible to authenticated users.
- **👤 User Management & Profile:**
  - Self-service endpoints for authenticated users to view profile details and change passwords.
  - Admin endpoints to provision users with custom roles and inspect all user records.
  - Automatic database seeding of a default `ADMIN` account on application startup (`DataInitializer`).
- **📄 Pagination & Sorting:**
  - Employee list endpoint supporting Spring Data `Pageable` for high-performance pagination and sorting.
- **✅ Jakarta Validation:**
  - Strict input validation using `spring-boot-starter-validation` (`@NotBlank`, `@Email`, `@Size`, `@NotNull`).
- **⚠️ Structured Global Exception Handling:**
  - Centralized `@RestControllerAdvice` mapping exceptions to standard RFC-compliant JSON responses (`400`, `401`, `403`, `404`, `409`, `500`).
- **🐳 Multi-Stage Docker & Compose Orchestration:**
  - Multi-stage Dockerfile compiling with Maven on Java 17 and running inside a lightweight JRE Alpine image with a non-root user.
  - Complete `docker-compose.yml` orchestrating MySQL 8.4, backend API, and frontend reverse proxy with automated health checks.

---

## 🛠️ Tech Stack

| Technology | Purpose |
| :--- | :--- |
| **Java 17 (LTS)** | Core programming language |
| **Spring Boot 3.3.5** | Core application framework |
| **Spring Security 6** | Authentication, authorization, and endpoint filtering |
| **JJWT (io.jsonwebtoken 0.12.5)** | HMAC-SHA signed JSON Web Tokens |
| **Spring Data JPA & Hibernate** | Object-Relational Mapping (ORM) and persistence |
| **MySQL 8.4** | Relational Database Management System |
| **Spring Boot Validation** | Bean Validation (Hibernate Validator) |
| **Springdoc OpenAPI (Swagger UI)** | Interactive API documentation |
| **JUnit 5 & Mockito** | Comprehensive unit & integration testing suite |
| **Docker & Docker Compose** | Containerization and multi-container orchestration |

---

## 📋 API Endpoints

### 🔑 Authentication (`/auth`)
| Method | Endpoint | Access Role | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/auth/register` | Public | Register a new user account (safely defaults to `USER` role) |
| `POST` | `/auth/login` | Public | Authenticate user credentials and receive a signed JWT token |

### 👤 User Management & Profile (`/users`)
| Method | Endpoint | Access Role | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/users/me` | Authenticated (`USER`, `ADMIN`) | Fetch profile details of currently logged-in user |
| `PUT` | `/users/change-password` | Authenticated (`USER`, `ADMIN`) | Verify current password and update to new password |
| `GET` | `/users` | `ADMIN` only | Retrieve a list of all registered users |
| `POST` | `/users` | `ADMIN` only | Provision a new user with an assigned role (`ADMIN`/`USER`) |

### 👔 Employee Management (`/employees`)
| Method | Endpoint | Access Role | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/employees` | Authenticated (`USER`, `ADMIN`) | Fetch all employees |
| `GET` | `/employees/page` | Authenticated (`USER`, `ADMIN`) | Fetch paginated list of employees (`page`, `size`, `sort`) |
| `POST` | `/employees` | `ADMIN` only | Create a new employee record |
| `PUT` | `/employees/{id}` | `ADMIN` only | Update an existing employee record |
| `DELETE` | `/employees/{id}` | `ADMIN` only | Delete an employee record |

### 📖 API Documentation & Swagger
| Method | Endpoint | Access Role | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/swagger-ui/index.html` | Public | Interactive Swagger UI API exploration |
| `GET` | `/v3/api-docs` | Public | OpenAPI 3.0 JSON specification |

---

## 🚀 Getting Started

### Prerequisites
- **JDK 17** or higher installed
- **MySQL 8.0+** running locally (or via Docker)
- **Git**

---

### Option 1: Local Development

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-username/employee-management-springboot.git
   cd employee-management-springboot
   ```

2. **Configure environment variables:**
   Copy the example environment file:
   ```bash
   cp .env.example .env
   ```
   Update the database credentials in `.env` or set environment variables if needed. Safe local defaults are configured out-of-the-box:
   ```properties
   DB_URL=jdbc:mysql://localhost:3306/employee_db
   DB_USERNAME=root
   DB_PASSWORD=Root@123
   JWT_SECRET=1CodeWithPurpose2BuildWithPassion3NeverStopLearning4
   DEFAULT_ADMIN_USERNAME=admin
   DEFAULT_ADMIN_PASSWORD=Admin@123
   ```

3. **Build and execute unit tests:**
   ```bash
   # Windows
   .\mvnw.cmd clean test

   # Linux / macOS
   ./mvnw clean test
   ```

4. **Run the Spring Boot application:**
   ```bash
   # Windows
   .\mvnw.cmd spring-boot:run

   # Linux / macOS
   ./mvnw spring-boot:run
   ```

   The application will boot at `http://localhost:8080`.
   Explore the Swagger documentation at: `http://localhost:8080/swagger-ui/index.html`.

---

### Option 2: Docker & Docker Compose

Launch the entire stack (MySQL 8.4 database, Spring Boot backend, and frontend) with a single command:

1. **Start all services:**
   ```bash
   docker compose up --build -d
   ```

2. **Check status of running containers:**
   ```bash
   docker compose ps
   ```

3. **Inspect application logs:**
   ```bash
   docker compose logs -f backend
   ```

4. **Stop the environment:**
   ```bash
   docker compose down
   ```
   *(To wipe persisted database volumes, add `-v`: `docker compose down -v`)*

---

## 🧪 Testing

The test suite contains clean, isolated unit tests written with **JUnit 5** and **Mockito** (no live database required for unit tests):

- **Service Layer Unit Tests:**
  - `EmployeeServiceImplTest`: Tests creation, retrieval, pagination (`Pageable`), update, deletion, and `ResourceNotFoundException`.
  - `UserServiceImplTest`: Tests registration (default `USER` role), duplicate username conflict (`409`), password modification, and invalid current password validation.
- **Controller Unit Tests:**
  - `AuthControllerTest`: Tests registration DTO flow and password verification (401 on bad credentials).
  - `UserControllerTest`: Tests `/users/me`, `/users/change-password`, `/users` admin controls.
  - `EmployeeValidationAndExceptionTest`: Tests bean validation (`@NotBlank`, `@Email`) and structured JSON error responses.
- **Security & Data Initialization Tests:**
  - `JwtServiceTest`: Tests JJWT 0.12.5 token generation, signing key verification, and role claim resolution.
  - `DataInitializerTest`: Verifies default admin provisioning.

Run all tests:
```bash
.\mvnw.cmd test
```

---

## 📄 License
This project is open-source and available under the [MIT License](LICENSE).
