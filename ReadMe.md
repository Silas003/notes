# Notes API Documentation

## Project Overview
The Notes API is a Spring Boot application that allows users to register, login, and manage personal notes. Users can create, read, update, and delete notes. JWT-based authentication is implemented to secure the API endpoints.

## Architecture
- Spring Boot REST API
- JWT Authentication
- User and Note management
- Exception handling for invalid operations
- JUnit and Mockito for unit testing

## Security
- JWT token authentication
- Secured endpoints for note and user management
- Passwords hashed using `PasswordUtils`

## API Endpoints

### Health Check
**Request:**
```http
GET /api/v1/notes/health
```
**Response:**
```text
Notes API is running
```
**Status Codes:**
```text
200 OK
```

---

### Register User
**Request:**
```http
POST /api/v1/auth/register
Content-Type: application/json
```
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```
**Response:**
```text
User registered successfully
```
**Status Codes:**
```text
200 OK
400 Bad Request - User already exists
```

---

### Login User
**Request:**
```http
POST /api/v1/auth/login
Content-Type: application/json
```
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```
**Response:**
```json
{
  "token": "jwt-token-string"
}
```
**Status Codes:**
```text
200 OK
401 Unauthorized - Invalid credentials
```

---

### Create Note
**Request:**
```http
POST /api/v1/notes
Authorization: Bearer <jwt-token>
Content-Type: application/json
```
```json
{
  "title": "Note title",
  "content": "Note content"
}
```
**Response:**
```json
{
  "id": 1,
  "title": "Note title",
  "content": "Note content"
}
```
**Status Codes:**
```text
201 Created
400 Bad Request - Invalid title or content
401 Unauthorized
```

---

### Get All Notes
**Request:**
```http
GET /api/v1/notes
Authorization: Bearer <jwt-token>
```
**Response:**
```json
[
  {
    "id": 1,
    "title": "Note title",
    "content": "Note content"
  },
  {
    "id": 2,
    "title": "Second note",
    "content": "More content"
  }
]
```
**Status Codes:**
```text
200 OK
401 Unauthorized
```

---

### Get Note by ID
**Request:**
```http
GET /api/v1/notes/{id}
Authorization: Bearer <jwt-token>
```
**Response:**
```json
{
  "id": 1,
  "title": "Note title",
  "content": "Note content"
}
```
**Error Response:**
```json
{
  "details": "Note with id 999 not found"
}
```
**Status Codes:**
```text
200 OK
404 Not Found
401 Unauthorized
```

---

### Update Note
**Request:**
```http
PUT /api/v1/notes/{id}
Authorization: Bearer <jwt-token>
Content-Type: application/json
```
```json
{
  "title": "Updated title",
  "content": "Updated content"
}
```
**Response:**
```json
{
  "id": 1,
  "title": "Updated title",
  "content": "Updated content"
}
```
**Error Response (Validation):**
```json
{
  "details": "Title cannot be empty"
}
```
**Error Response (Not Found):**
```json
{
  "details": "Note with id 999 not found"
}
```
**Status Codes:**
```text
200 OK
400 Bad Request
404 Not Found
401 Unauthorized
```

---

### Delete Note
**Request:**
```http
DELETE /api/v1/notes/{id}
Authorization: Bearer <jwt-token>
```
**Response:**
```text
Note with id 1 deleted successfully
```
**Error Response:**
```json
{
  "details": "Note with id 999 not found"
}
```
**Status Codes:**
```text
200 OK
404 Not Found
401 Unauthorized
```

---

### Get User by ID
**Request:**
```http
GET /api/v1/users/{id}
Authorization: Bearer <jwt-token>
```
**Response:**
```json
{
  "email": "user@example.com"
}
```
**Status Codes:**
```text
200 OK
404 Not Found
401 Unauthorized
```

---

### Get All Users
**Request:**
```http
GET /api/v1/users
Authorization: Bearer <jwt-token>
```
**Response:**
```json
[
  {
    "email": "user1@example.com"
  },
  {
    "email": "user2@example.com"
  }
]
```
**Status Codes:**
```text
200 OK
401 Unauthorized
```

---

### Update User
**Request:**
```http
PUT /api/v1/users/{id}
Authorization: Bearer <jwt-token>
Content-Type: application/json
```
```json
{
  "email": "updated@example.com",
  "password": "newpassword123"
}
```
**Response:**
```json
{
  "email": "updated@example.com"
}
```
**Error Response:**
```json
{
  "details": "User not found"
}
```
**Status Codes:**
```text
200 OK
404 Not Found
401 Unauthorized
```

---

### Delete User
**Request:**
```http
DELETE /api/v1/users/{id}
Authorization: Bearer <jwt-token>
```
**Response:**
```text
User with id 1 deleted successfully
```
**Error Response:**
```json
{
  "details": "User not found"
}
```
**Status Codes:**
```text
200 OK
404 Not Found
401 Unauthorized
```

---

## Testing
- Unit tests written using JUnit 5 and Mockito
- Service and Controller layers tested
- Mocked JWT authentication for secured endpoints

## Database
- H2 in-memory database for development and testing
- Tables: `users`, `notes`
- User: id, email, password
- Note: id, title, content, user_id

## Error Handling
- `EntityNotFoundException` for missing users or notes
- `InvalidNoteException` for invalid note data
- `UserExists` for duplicate user registration
- `IllegalArgumentException` for invalid login credentials

## Authentication
- JWT-based authentication
- Passwords hashed before storing
- All endpoints (except health check, register, login) require JWT token in Authorization header


