# Spring Boot Blog App Development Workflow

## Project Overview
A Spring Boot RESTful Blog Application with MongoDB as the database and JWT (JSON Web Tokens) for stateless authentication.

**Technology Stack:**
- Spring Boot 4.0.2
- MongoDB
- JWT (JSON Web Tokens)
- Spring Security
- Maven

---

## Development Phases & Task Order

### **Phase 1: Project Setup & Dependencies**

#### Task 1.1: Configure Maven Dependencies
- [x] Add JWT library (jjwt) to pom.xml
- [x] Add Lombok for boilerplate reduction
- [x] Add validation library (spring-boot-starter-validation)
- [x] Verify all dependencies are correctly defined
- [x] Run `mvn clean install` to download dependencies

#### Task 1.2: Configure MongoDB Connection
- [x] Update `application.properties` with MongoDB URI
  - MongoDB connection string
  - Database name
  - Connection pool settings
- [x] Create MongoDB collections/indexes configuration (`com.example.BlogApp.config.MongoConfig` creates indexes on startup)
- [x] Test MongoDB connection on startup (manual/integration) - `com.example.BlogApp.config.MongoHealthChecker` pings DB on startup and logs the response

#### Task 1.3: Configure Application Properties
- [x] Set server port and servlet context path
- [x] Configure JWT secret key and expiration time (sample values added; use env var `JWT_SECRET` in prod)
- [x] Configure logging levels
- [x] Set up environment-specific profiles (dev, test, prod) - `application-dev.properties`, `application-prod.properties` added

---

### **Phase 2: Database Layer - Models & Repositories**

#### Task 2.1: Create Entity Models
- [x] **User Entity**
  - userId (UUID or String)
  - username (unique)
  - email (unique)
  - password (encrypted)
  - createdAt
  - updatedAt
  - roles/authorities
  - Add @Document annotation for MongoDB

- [x] **Blog Post Entity**
  - postId (UUID or String)
  - title
  - content
  - author (reference to User)
  - createdAt
  - updatedAt
  - tags/categories
  - views count
  - published status

- [x] **Comment Entity**
  - commentId (UUID or String)
  - content
  - author (reference to User)
  - post (reference to Blog Post)
  - createdAt
  - updatedAt

#### Task 2.2: Create MongoDB Repositories
- [x] Create `UserRepository` extending MongoRepository
  - findByUsername()
  - findByEmail()
  - existsByUsername()
  - existsByEmail()

- [x] Create `BlogPostRepository` extending MongoRepository
  - findByAuthor()
  - findByPublishedTrue()
  - findByTitleContaining()
  - Custom pagination queries

- [x] Create `CommentRepository` extending MongoRepository
  - findByPost()
  - findByAuthor()

---

### **Phase 3: Security & Authentication**

#### Task 3.1: Create JWT Utilities
- [ ] Create `JwtTokenProvider` class
  - generateToken(UserDetails) - Create JWT tokens
  - getClaimsFromToken(String token) - Extract claims
  - validateToken(String token) - Validate token signature and expiration
  - getUsernameFromToken(String token) - Extract username
  - getExpirationDateFromToken(String token) - Get expiration date

#### Task 3.2: Create Custom Security Configuration
- [ ] Create `SecurityConfig` class extending WebSecurityConfigurerAdapter
  - Configure HttpSecurity for stateless authentication
  - Disable CSRF (since using JWT)
  - Set session management to STATELESS
  - Configure filter chain order

- [ ] Create JWT Authentication Filter
  - Extract token from Authorization header
  - Validate token
  - Set authentication in SecurityContext

- [ ] Create JWT Authentication Entry Point
  - Handle unauthorized access (401)
  - Return proper error response

#### Task 3.3: Create Custom User Details Service
- [ ] Create `UserDetailsService` implementation
  - loadUserByUsername(String username)
  - Load user from MongoDB
  - Build UserDetails with authorities/roles

---

### **Phase 4: DTO & Request/Response Models**

#### Task 4.1: Create Authentication DTOs
- [ ] `LoginRequest` DTO
  - username
  - password

- [ ] `RegisterRequest` DTO
  - username
  - email
  - password
  - confirmPassword

- [ ] `JwtAuthenticationResponse` DTO
  - token
  - tokenType ("Bearer")
  - expiresIn

- [ ] `AuthResponse` DTO (Generic success/error responses)
  - success (boolean)
  - message (String)
  - data (Object)

#### Task 4.2: Create User DTOs
- [ ] `UserDTO` - For returning user data
  - userId
  - username
  - email
  - createdAt

#### Task 4.3: Create Blog Post DTOs
- [ ] `CreatePostRequest`
  - title
  - content
  - tags

- [ ] `UpdatePostRequest`
  - title
  - content
  - tags

- [ ] `PostDTO` - For returning post data
  - postId
  - title
  - content
  - author (UserDTO)
  - tags
  - createdAt
  - updatedAt
  - views

#### Task 4.4: Create Comment DTOs
- [ ] `CreateCommentRequest`
  - content

- [ ] `CommentDTO`
  - commentId
  - content
  - author (UserDTO)
  - createdAt

---

### **Phase 5: Service Layer**

#### Task 5.1: Create Authentication Service
- [ ] `AuthenticationService` class
  - register(RegisterRequest) - Register new user
    - Validate input (username/email unique)
    - Hash password
    - Save user to MongoDB
    - Return success response
  - login(LoginRequest) - Authenticate user
    - Load user by username
    - Verify password
    - Generate JWT token
    - Return JWT token
  - validateToken(String token) - Validate JWT
  - refreshToken(String token) - Generate new token (optional)

#### Task 5.2: Create User Service
- [ ] `UserService` class
  - getUserById(String userId)
  - getUserByUsername(String username)
  - updateUserProfile(String userId, UpdateUserRequest)
  - getAllUsers() (Admin only)
  - deleteUser(String userId) (Admin only)

#### Task 5.3: Create Blog Post Service
- [ ] `BlogPostService` class
  - createPost(String authorId, CreatePostRequest)
  - getPostById(String postId)
    - Increment view count
  - updatePost(String postId, UpdatePostRequest) - Only by author
  - deletePost(String postId) - Only by author or admin
  - getAllPosts(Pageable) - Public posts only
  - getPostsByAuthor(String authorId, Pageable)
  - searchPosts(String keyword, Pageable)
  - publishPost(String postId)
  - unpublishPost(String postId)

#### Task 5.4: Create Comment Service
- [ ] `CommentService` class
  - addComment(String postId, String authorId, CreateCommentRequest)
  - getCommentsByPost(String postId)
  - deleteComment(String commentId) - By author or admin
  - updateComment(String commentId, CreateCommentRequest) - By author

---

### **Phase 6: REST API Controllers**

#### Task 6.1: Create Authentication Controller
- [ ] `AuthenticationController` class
  - POST /api/auth/register - Register new user
  - POST /api/auth/login - Login user
  - POST /api/auth/refresh - Refresh token (optional)

#### Task 6.2: Create User Controller
- [ ] `UserController` class
  - GET /api/users/{userId} - Get user profile
  - GET /api/users - Get all users (Admin)
  - PUT /api/users/{userId} - Update user profile
  - DELETE /api/users/{userId} - Delete user (Admin)

#### Task 6.3: Create Blog Post Controller
- [ ] `PostController` class
  - POST /api/posts - Create new post (Authenticated)
  - GET /api/posts - Get all published posts (Paginated)
  - GET /api/posts/{postId} - Get single post
  - GET /api/posts/author/{authorId} - Get posts by author
  - PUT /api/posts/{postId} - Update post (Author only)
  - DELETE /api/posts/{postId} - Delete post (Author/Admin)
  - POST /api/posts/{postId}/publish - Publish post
  - POST /api/posts/{postId}/unpublish - Unpublish post
  - GET /api/posts/search?keyword={keyword} - Search posts

#### Task 6.4: Create Comment Controller
- [ ] `CommentController` class
  - POST /api/posts/{postId}/comments - Add comment
  - GET /api/posts/{postId}/comments - Get post comments
  - PUT /api/comments/{commentId} - Update comment (Author only)
  - DELETE /api/comments/{commentId} - Delete comment (Author/Admin)

---

### **Phase 7: Exception Handling & Validation**

#### Task 7.1: Create Custom Exceptions
- [ ] `ResourceNotFoundException` - For missing resources
- [ ] `DuplicateResourceException` - For duplicate entries
- [ ] `UnauthorizedException` - For unauthorized access
- [ ] `ValidationException` - For validation errors
- [ ] `JwtTokenException` - For JWT-related errors

#### Task 7.2: Create Global Exception Handler
- [ ] `GlobalExceptionHandler` class with @ControllerAdvice
  - Handle ResourceNotFoundException
  - Handle ValidationException
  - Handle JwtTokenException
  - Handle general exceptions
  - Return proper HTTP status codes with error messages

#### Task 7.3: Create Custom Validators
- [ ] Validate username format and uniqueness
- [ ] Validate email format and uniqueness
- [ ] Validate password strength
- [ ] Validate post title and content

---

### **Phase 8: Testing**

#### Task 8.1: Unit Tests
- [ ] Test `JwtTokenProvider` methods
- [ ] Test `AuthenticationService` methods
- [ ] Test `UserService` methods
- [ ] Test `BlogPostService` methods
- [ ] Test `CommentService` methods
- [ ] Mock repositories and dependencies

> Note: A basic application context test exists at `src/test/java/com/example/BlogApp/BlogAppApplicationTests.java` and `spring-boot-starter-test` is present in `pom.xml`.

#### Task 8.2: Integration Tests
- [ ] Test authentication flow (register → login → access protected resource)
- [ ] Test blog post operations
- [ ] Test comment operations
- [ ] Test authorization (unauthorized access should be denied)
- [ ] Test edge cases

#### Task 8.3: API Tests
- [ ] Test all REST endpoints
- [ ] Test request validation
- [ ] Test error responses
- [ ] Test pagination
- [ ] Use Postman or similar tools for manual testing

---

### **Phase 9: Security Hardening**

#### Task 9.1: Password Security
- [ ] Implement password hashing (BCryptPasswordEncoder)
- [ ] Add password strength validation
- [ ] Prevent common passwords

#### Task 9.2: JWT Configuration
- [ ] Set appropriate JWT expiration time
- [ ] Use strong secret key (minimum 256 bits)
- [ ] Add token refresh mechanism (optional)
- [ ] Implement token blacklist for logout (optional)

#### Task 9.3: API Security
- [ ] Enable CORS (Cross-Origin Resource Sharing) if needed
- [ ] Add rate limiting
- [ ] Implement request/response validation
- [ ] Add security headers (HSTS, X-Frame-Options, etc.)

#### Task 9.4: Authorization/Access Control
- [ ] Implement role-based access control (RBAC) if needed
- [ ] Ensure users can only modify their own data
- [ ] Ensure admin operations require admin role

---

### **Phase 10: Configuration & Deployment**

#### Task 10.1: Application Configuration
- [ ] Create application.properties for all environments
  - Development
  - Testing
  - Production
- [ ] Configure logging (SLF4J with Logback)
- [ ] Configure database connection pooling

#### Task 10.2: Documentation
- [ ] Create API documentation (Swagger/OpenAPI)
  - Add @ApiOperation and @ApiParam annotations
  - Generate OpenAPI/Swagger UI
- [ ] Create README.md with setup instructions
- [ ] Document API endpoints and usage

#### Task 10.3: Deployment Preparation
- [ ] Create Dockerfile for containerization
- [ ] Create docker-compose.yml for MongoDB
- [ ] Add CI/CD pipeline configuration
- [ ] Create deployment scripts

---

## Task Dependencies & Critical Path

```
Phase 1: Setup & Dependencies
    ↓
Phase 2: Database Layer
    ↓
Phase 3: Security & JWT
    ↓
Phase 4: DTOs & Models
    ↓
Phase 5: Service Layer
    ↓
Phase 6: REST API Controllers
    ↓
Phase 7: Exception Handling
    ↓
Phase 8: Testing
    ↓
Phase 9: Security Hardening
    ↓
Phase 10: Configuration & Deployment
```

---

## Implementation Checklist

Use this checklist to track progress:

- [x] Phase 1 Complete
- [ ] Phase 2 Complete
- [ ] Phase 3 Complete
- [ ] Phase 4 Complete
- [ ] Phase 5 Complete
- [ ] Phase 6 Complete
- [ ] Phase 7 Complete
- [ ] Phase 8 Complete
- [ ] Phase 9 Complete
- [ ] Phase 10 Complete

---

## Key Points to Remember

1. **Stateless Authentication**: JWT tokens contain all user information, no session storage needed
2. **MongoDB**: Ensure proper indexing on frequently queried fields (username, email, authorId)
3. **Security**: Always hash passwords, validate JWT tokens, and implement proper authorization checks
4. **DTOs**: Never expose entity models directly in APIs, use DTOs to control what data is returned
5. **Error Handling**: Return meaningful error messages and proper HTTP status codes
6. **Testing**: Write tests for critical flows like authentication and data operations
7. **Documentation**: Keep API documentation up-to-date for frontend developers

---

## Useful Commands

```bash
# Build project
mvn clean install

# Run application
mvn spring-boot:run

# Run tests
mvn test

# Generate Javadoc
mvn javadoc:javadoc
```

---

## References

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security](https://spring.io/projects/spring-security)
- [MongoDB Spring Data](https://spring.io/projects/spring-data-mongodb)
- [JWT (jjwt)](https://github.com/jwtk/jjwt)
- [REST API Best Practices](https://restfulapi.net/)

---

**Document Version:** 1.0  
**Last Updated:** February 14, 2026  
**Status:** Ready for Development
