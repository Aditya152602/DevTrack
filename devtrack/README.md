# DevTrack (reconstructed snapshot)

This ZIP recreates the project structure and code based on the implementation recorded in the conversation. It is not a recovered copy of the deleted original folder and has not been tested against your local MySQL server in this environment.

## Included features
- CRUD REST API at `/api/tasks`
- MySQL persistence with Spring Data JPA
- `TaskRequest` input DTO and title validation (not blank, 3–100 characters)
- `TaskResponse` output DTO for POST, GET-all, GET-by-ID and PUT
- Consistent JSON validation error handling
- 201 on create, 200 on reads/updates, 204 on successful delete, 404 for missing task IDs

## Requirements
- JDK 25 or a compatible JDK for the selected Spring Boot version
- MySQL Server and Maven (or IntelliJ IDEA Maven integration)

## Database setup
Create the database if it does not already exist:

```sql
CREATE DATABASE devtrack_db;
```

Set environment variable `DEVTRACK_DB_PASSWORD` to your local MySQL password, or replace `YOUR_MYSQL_PASSWORD` in `src/main/resources/application.properties`. Never commit real credentials.

Open this folder in IntelliJ IDEA, import Maven dependencies, and run `DevtrackApplication`.

## Endpoints
| Method | Path | Purpose |
|---|---|---|
| GET | `/api/tasks` | List tasks |
| GET | `/api/tasks/{id}` | Get task by ID |
| POST | `/api/tasks` | Create task |
| PUT | `/api/tasks/{id}` | Update task |
| DELETE | `/api/tasks/{id}` | Delete task |

Example request body:
```json
{
  "title": "Learn DTOs in DevTrack",
  "completed": false
}
```

Your original MySQL database is stored separately from this project folder and may still exist on your computer.
