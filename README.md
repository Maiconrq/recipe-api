# Recipe API

A RESTful API built with Java and Spring Boot to manage cooking recipes. This project was developed as part of the ReciMe Backend Developer Coding Challenge and adheres to REST principles, proper design patterns, and clean coding practices.

## Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [API Endpoints](#api-endpoints)
- [Filtering Capabilities](#filtering-capabilities)
- [Exception Handling](#exception-handling)
- [Testing](#testing)
- [Design Decisions](#design-decisions)
- [Getting Started](#getting-started)
- [How to Run](#how-to-run)

---

## Overview

The API allows clients to manage recipes by performing standard CRUD operations and executing dynamic search queries. Each recipe contains:

- Title
- Description
- List of ingredients
- Instructions
- Vegetarian flag
- Number of servings

In addition to basic CRUD, the API supports advanced search capabilities using optional filters.

---

## Tech Stack

- **Java 17**
- **Spring Boot 3.5.0**
- **Spring Data JPA**
- **PostgreSQL 14.7**
- **Project Lombok**
- **Maven**
- **JUnit 5**

---

## Project Structure

```
recipe-api/
├── controller/        # REST controllers
├── dto/               # Data Transfer Objects (DTOs)
├── exception/         # Custom exceptions and global handler
├── model/             # Entity model
├── repository/        # Spring Data JPA repository
├── service/           # Business logic layer
├── specification/     # JPA Specifications for dynamic filtering
├── resources/
│   └── application.properties
└── test/              # Unit tests
```

---

## API Endpoints

| Method | Endpoint            | Description                      |
| ------ | ------------------- | -------------------------------- |
| GET    | `/api/recipes`      | Retrieve all or filtered recipes |
| GET    | `/api/recipes/{id}` | Retrieve a recipe by ID          |
| POST   | `/api/recipes`      | Create a new recipe              |
| PUT    | `/api/recipes/{id}` | Update an existing recipe        |
| DELETE | `/api/recipes/{id}` | Delete a recipe by ID            |

---

## Filtering Capabilities

The `GET /api/recipes` endpoint supports the following optional query parameters:

| Parameter            | Type      | Description                                      |
| -------------------- | --------- | ------------------------------------------------ |
| `vegetarian`         | `boolean` | Filters by vegetarian recipes                    |
| `servings`           | `int`     | Filters by number of servings                    |
| `includeIngredients` | `List`    | Includes only recipes with these ingredients     |
| `excludeIngredients` | `List`    | Excludes recipes with these ingredients          |
| `instructionSearch`  | `String`  | Performs case-insensitive search on instructions |

##### API Usage Examples:

###### Create a Recipe:
```
curl -X POST http://localhost:8080/api/recipes \
  -H "Content-Type: application/json" \
  -d '{
        "title": "Pasta",
        "description": "Delicious Italian pasta.",
        "ingredients": ["pasta", "tomato sauce"],
        "instructions": "Boil pasta and add sauce.",
        "vegetarian": true,
        "servings": 2
      }'
```

###### Create Recipes (Bulk Insert):
```
curl -X POST http://localhost:8080/recipes/bulk \
  -H "Content-Type: application/json" \
  -d '[
    {
      "title": "Spaghetti",
      "description": "Classic pasta",
      "ingredients": ["pasta", "tomato sauce", "cheese"],
      "instructions": "Boil pasta. Add sauce. Serve.",
      "vegetarian": true,
      "servings": 2
    },
    {
      "title": "Grilled Chicken",
      "description": "Tasty grilled chicken",
      "ingredients": ["chicken breast", "salt", "pepper"],
      "instructions": "Grill chicken until cooked.",
      "vegetarian": false,
      "servings": 1
    }
  ]'
```
###### Get Recipes (with optional filters):
```
curl -X GET "http://localhost:8080/api/recipes?vegetarian=true&servings=2&includeIngredients=tomato%20sauce&excludeIngredients=meat&instructionSearch=boil"
```

###### Get All Recipes (without optional filters):
```
curl -X GET http://localhost:8080/api/recipes
```

###### Get Recipe by ID:
```
curl -X GET http://localhost:8080/api/recipes/1
```

###### Update a Recipe:
```
curl -X PUT http://localhost:8080/api/recipes/1 \
  -H "Content-Type: application/json" \
  -d '{
        "title": "Updated Pasta",
        "description": "Even better pasta.",
        "ingredients": ["pasta", "basil"],
        "instructions": "Boil pasta and add basil.",
        "vegetarian": true,
        "servings": 3
      }'
```

###### Delete a Recipe:
```
curl -X DELETE http://localhost:8080/api/recipes/1
```

---

## Exception Handling

The API implements a global exception handler to ensure all errors are returned in a structured JSON format, improving API consistency.

### Custom exceptions include:

- `RecipeNotFoundException` for 404 errors;
- `MethodArgumentNotValidException` for cases when arguments is not valid;
- `EmptyResultDataAccessException` for safe delete operations;

The exception handler ensures meaningful error messages with appropriate HTTP status codes.

---

## Testing

- `RecipeServiceTest` includes unit tests for edge cases and exception paths
- Mocks and assertions ensure the core logic is well-covered
- Test coverage includes create, read, update, delete, and filtering

---

## Design Decisions

### Use of DTOs
All interactions use DTOs (`RecipeCreateDTO`, `RecipeUpdateDTO`, `RecipeResponseDTO`) to decouple internal entity logic from external exposure. This prevents over-posting attacks and enables easier validation and transformation.

### Layered Architecture
- Controller handles HTTP requests
- Service layer encapsulates business logic
- Repository abstracts database operations
- Specification pattern allows composable filtering

### Specification API
To support dynamic and scalable query filtering, the JPA Specification pattern was used. This ensures modular and reusable filter logic without bloating query methods.

### Case-insensitive Search
Instruction-based search uses case-insensitive `LIKE` queries (`lower(...) LIKE lower(...)`) to ensure user-friendly search experience.

### Bulk Insert Design Choice
An additional `POST /recipes/bulk` endpoint was implemented to allow clients to insert multiple recipes at once. This design decision was made considering:

- **Performance**: Bulk inserts reduce the number of HTTP round-trips between client and server, improving throughput.

- **Scalability**: Useful for importing or syncing large datasets, especially when integrating with other systems.

- **Extensibility**: Easily allows future improvements, such as batch validation or partial success reporting.

This was added while still keeping the original `POST /recipes` endpoint for single-entity operations, preserving REST semantics and client simplicity.

### Pagination
Pagination was added to the recipe filtering endpoint to ensure the API remains performant and scalable even with large datasets. Spring Data's native support for `Pageable` and `Page<T>` allows easy implementation, helping to reduce memory usage and improve client-side experience by allowing incremental data retrieval.

### Clean Code & Conventions
- Follows standard Java naming conventions
- Uses meaningful class and method names
- Clear package separation for each responsibility

---

## Getting Started

### Prerequisites

- Java 17
- Maven
- Docker (for local PostgreSQL)

### Clone the repository

```bash
git clone https://github.com/maiconrq/recipe-api.git
cd recipe-api
```

---

## How to Run

### 1. Start PostgreSQL with Docker

```bash
docker-compose up -d
```

### 2. Run the application

```bash
./mvnw spring-boot:run
```

The API will be available at: `http://localhost:8080/api/recipes`

---

## Conclusion

This project was built with scalability, clarity, and maintainability in mind. The use of Spring Boot idioms, layered architecture, DTOs, and specifications demonstrates a thoughtful backend design that can evolve over time.