## Table of Contents

1. [Product Vision](#product-vision)
2. [Codebase](#codebase)
3. [Sprint 0: Planning](#sprint-0-planning)
   - [Product Backlog](#product-backlog)
   - [User Stories](#user-stories)
   - [Definition of Done (DOD)](#definition-of-done-dod)
4. [Plan Sprint 1](#plan-sprint-1)
5. [Sprint 1: Execution](#sprint-1-execution)
   - [Sprint 1 Review](#sprint-1-review)
   - [Sprint 1 Retrospective](#sprint-1-retrospective)
6. [Plan Sprint 2](#plan-sprint-2)
   - [Sprint 2 Review](#sprint-2-review)
   - [Sprint 2 Retrospective](#sprint-2-retrospective)

---

## Product Vision

To create a secure Notes API that allows users to register, log in, and perform full CRUD operations on notes with JWT authentication, proper exception handling, and a robust CI/CD pipeline for automated testing and deployment.

---


## Sprint 0: Planning

### Product Backlog

### User Stories

---

#### US1 – User Registration

> *As a user, I want to register with the API so that I can create an account to manage my notes.*

**Acceptance Criteria**
- User can register with a valid email and password.
- Duplicate emails are not allowed.
- Password is hashed before saving.
- Success message returned after registration.

| Priority | Story Points |
|----------|-------------|
| High     | 3           |

---

#### US2 – User Login

> *As a user, I want to log in to the API so that I can access my notes securely.*

**Acceptance Criteria**
- User can log in with a valid email and password.
- Invalid credentials return an error message.
- JWT token is generated and returned on successful login.

| Priority | Story Points |
|----------|-------------|
| High     | 3           |

---

#### US3 – Create Note

> *As a user, I want to create a note so that I can save my thoughts or tasks.*

**Acceptance Criteria**
- Authenticated user can create a note with a title and content.
- Validation ensures title and content are not empty.
- Newly created note is stored in the database.

| Priority | Story Points |
|----------|-------------|
| High     | 2           |

---

#### US4 – Read Notes

> *As a user, I want to retrieve my notes so that I can view them.*

**Acceptance Criteria**
- Authenticated user can get all their notes.
- User can get a single note by ID.
- Returns 404 if note does not exist.

| Priority | Story Points |
|----------|-------------|
| High     | 2           |

---

#### US5 – Update Note

> *As a user, I want to update a note so that I can modify its content.*

**Acceptance Criteria**
- Authenticated user can update title and content of a note.
- Returns 404 if note does not exist.
- Validation ensures updated fields are not empty.

| Priority | Story Points |
|----------|-------------|
| High     | 2           |

---

#### US6 – Delete Note

> *As a user, I want to delete a note so that I can remove unwanted content.*

**Acceptance Criteria**
- Authenticated user can delete a note by ID.
- Returns 404 if note does not exist.

| Priority | Story Points |
|----------|-------------|
| High     | 2           |

---

#### US7 – Health Check

> *As a developer, I want a health check endpoint so that I can verify the API is running.*

**Acceptance Criteria**
- Endpoint returns `200 OK` with message: `"Notes API is running."`

| Priority | Story Points |
|----------|-------------|
| Medium   | 1           |

---

#### US8 – Exception Handling

> *As a developer, I want structured API error responses so that clients can handle errors consistently.*

**Acceptance Criteria**
- Returns JSON object with status and details for errors like invalid input or missing resources.

| Priority | Story Points |
|----------|-------------|
| Medium   | 2           |

---

#### US9 – CI/CD Pipeline

> *As a developer, I want automated tests and builds to run on GitHub Actions so that code quality is continuously verified.*

**Acceptance Criteria**
- Tests run automatically on every push.
- Build fails if any test fails.
- Success or failure notifications are sent.

| Priority | Story Points |
|----------|-------------|
| High     | 3           |

---

#### US10 – JWT Security Validation

> *As a developer, I want JWT validation to secure endpoints so that only authenticated users can access protected routes.*

**Acceptance Criteria**
- Protected endpoints reject requests without a valid JWT.
- JWT filter sets the SecurityContext correctly.

| Priority | Story Points |
|----------|-------------|
| High     | 2           |

---

### Definition of Done (DOD)

A user story is considered complete when:

- The functionality described in the acceptance criteria is fully implemented.
- Automated tests validating the acceptance criteria are written and passing locally.
- Code is committed with clear, descriptive messages.
- No critical defects remain.
- The project builds successfully in the local development environment.
- CI/CD pipeline runs successfully in GitHub Actions.
- Test execution logs clearly show which tests passed and failed.
- All relevant documentation is updated.

---

## Plan Sprint 1

### Sprint 1 Goal

To deliver the first working increment of the Notes API, including user registration, login, note CRUD operations, the health check endpoint, structured exception handling, and an automated CI/CD pipeline.

### Sprint 1 Duration

Simulated 1-week sprint

### Sprint 1 Backlog (Selected User Stories)

| Story | Description         |
|-------|---------------------|
| US3   | Create Note         |
| US4   | Read Notes          |
| US7   | Health Check        |
| US8   | Exception Handling  |
| US9   | CI/CD Pipeline      |

---

## Sprint 1: Execution

### Sprint 1 Review

**Objective:** Deliver a functional Notes API with core features implemented and tested.

#### Summary of Work Completed

| Story | Status | Notes |
|-------|--------|-------|
| US3 – Create Note | ✅ Done | Note creation endpoint functional with input validation. |
| US4 – Read Notes | ✅ Done | Retrieve all notes and single note by ID; returns 404 if not found. |
| US7 – Health Check | ✅ Done | Endpoint returns `200 OK` with `"Notes API is running."` |
| US8 – Exception Handling | ✅ Done | Structured JSON error responses implemented for invalid input, missing resources, and auth errors. |
| US9 – CI/CD Pipeline | ✅ Done | GitHub Actions pipeline configured to run tests on every push; build fails on test failure. |

---

### Sprint 1 Retrospective

**Objective:** Reflect on the sprint to identify successes and areas for improvement.

#### What Went Well

- Core endpoints implemented and functional.
- Unit and integration tests written for all endpoints.

#### Challenges / What Could Be Improved

- CI/CD pipeline initially failed to catch some test errors.

#### Team Morale & Sentiment

The sprint was productive; team is confident about completing remaining improvements in Sprint 2.

#### Conclusion

Sprint 1 successfully delivered the Notes API core functionality, exception handling, and the initial CI/CD pipeline. Remaining tasks in Sprint 2 will focus on completing note update/delete operations, JWT security validation.

---

## Plan Sprint 2

### Sprint 2 Goal

To complete the Notes API with full CRUD support, end-to-end JWT security validation, improved test coverage, and CI/CD pipeline notification enhancements.

### Sprint 2 Duration

Simulated 2-week sprint

### Sprint 2 Backlog (Selected User Stories)

| Story | Description              |
|-------|--------------------------|
| US5   | Update Note              |
| US6   | Delete Note              |
| US10  | JWT Security Validation  |

---

## Sprint 2: Execution & Improvement

**Objective:** Integrate feedback from Sprint 1, complete remaining CRUD operations, enforce JWT security across all protected endpoints, and improve test coverage and pipeline reliability.

### Sprint 2 Review

**Objective:** Deliver a fully featured and secured Notes API with all user stories completed.

#### Summary of Work Completed

| Story | Status | Notes |
|-------|--------|-------|
| US1 – User Registration | ✅ Done | Registration endpoint implemented with hashed passwords and duplicate email checks. |
| US2 – User Login | ✅ Done | Login endpoint implemented with JWT generation and invalid credential handling. |
| US5 – Update Note | ✅ Done | Update endpoint implemented with validation; returns 404 for missing notes. |
| US6 – Delete Note | ✅ Done | Delete endpoint functional; returns 404 if note does not exist. |
| US10 – JWT Security Validation | ✅ Done | JWT filter fully tested; protected endpoints correctly reject requests with invalid or missing tokens. |

---

### Sprint 2 Retrospective

**Objective:** Reflect on the final sprint to consolidate learnings and close out the project.

#### What Went Well

- All Sprint 2 stories completed on time.
- Significantly improved test coverage across unit and integration layers.
- JWT validation working end-to-end; SecurityContext correctly populated on all protected routes.

#### Challenges / What Could Be Improved

- Test flakiness was encountered during integration testing, requiring reruns and additional investigation to isolate non-deterministic behaviour.
- JWT filter complexity increased debugging time, particularly around token extraction and authority mapping edge cases.

#### Team Morale & Sentiment

Team morale was mixed during this sprint. While all stories were delivered, some blockers — particularly around test stability and JWT filter behaviour — slowed progress and added pressure toward the end of the sprint. The team acknowledged these as learning opportunities and noted that earlier investment in test isolation strategies would have helped.

#### Conclusion

Sprint 2 successfully completed the Notes API, delivering full CRUD functionality, robust JWT security validation, and improved test coverage. The project now meets all acceptance criteria defined in Sprint 0 and satisfies the product vision of a secure, well-tested, and continuously integrated Notes API.
