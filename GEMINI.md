# Project Overview

This project is the backend for a friend-group application, built to serve a Flutter frontend.
The core domain is **Cashpools**: a Splitwise-like feature that splits multiple bills or payments amongst a group of
people to ensure everyone pays their fair share.

# Tech Stack

* **Language:** Kotlin (Latest version)
* **Framework:** Ktor Server (Netty)
* **Database / ORM:** PostgreSQL, H2 (for testing), JetBrains Exposed (DAO & JDBC)
* **Validation:** Konform
* **Security:** Argon2 (Password hashing), JWT (Auth)
* **Testing:** `kotlin.test`, Ktor `testHost`, `MockK`

# Architectural Rules

The backend strictly follows Clean Architecture, Separation of Concerns, Single Source of Truth, and Layered
Architecture.

* **`/controller`**: Handles HTTP routing and Ktor `Resources`. Extracts data from requests and delegates to Services.
* **`/service`**: Contains business orchestration logic. Coordinates between Domain commands/validations and
  Repositories.
* **`/domain`**: The core. Contains `models`, `entities`, `commands`, `validations`, `repositories` (
  interfaces/implementations), and Exposed `tables`.
* **`/dto`**: Request and Response data transfer objects.
* **`/core`**: Global exceptions, extensions, and custom serializers.

**Constraint:** Ensure any generated code rigidly adheres to this structure. Do not leak database logic (Exposed) into
controllers, and do not leak HTTP logic (Ktor) into the domain.

# AI Directives: Testing (CRITICAL)

My primary use of AI in this project is to write tests for existing code. You must adhere to the following rules:

1. **Strictly DRY (Don't Repeat Yourself):** Tests must not duplicate setup code or assertions.
    * **Controllers:** Must inherit from `BaseControllerTest`.
    * **Services:** Must inherit from `BaseServiceTest`.
2. **Use Test Utilities:** Common testing logic is in `src/test/kotlin/testutils`. Before writing new setup code, assume
   a utility exists.
    * Reference `testutils/Users.kt` for dummy user generation/data.
    * Reference `testutils/Commands.kt` for reusable command setups.
3. **Create Generic Utils:** If you encounter a situation that violates DRY and requires a new utility, you are
   permitted to create it. However, you MUST ensure it is completely generic and placed in `src/test/kotlin/testutils`.
4. **Dependency Injection & Mocking:** * Use `MockK` for all mocking. Use `coEvery` and `coVerify` for suspend
   functions.
    * The project uses the official Ktor DI (`io.ktor.server.plugins.di`). For integration/controller tests, override
      bindings within `testApplication` using `application { dependencies { provide<Service> { mockService } } }`.
5. **API Testing:** Use Ktor's `testApplication` and `client` for controller tests. Validate HTTP status codes and
   content negotiation (JSON serialization) explicitly.
6. **Coverage Target (~80%):** Aim for roughly 80% branch coverage. Use `MockK` aggressively to isolate the System Under
   Test. Ensure all logical branches—including the "happy path", validation failures (Konform), authorization blocks,
   and custom domain exceptions (e.g., `UserNotFound`, `DataQualityException`)—are explicitly tested.