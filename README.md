# Capstone 3: E-Commerce API - Clothing Store

A RESTful API backend for an e-commerce clothing store built with Java Spring Boot. This project implements product browsing, category management, shopping cart functionality, and user profile management with JWT-based authentication.

## Project Overview

This capstone project serves as the backend API for an online clothing store. The application follows a three-tier architecture with Controllers, DAO interfaces, and MySQL DAO implementations. Users can browse products by category, search with filters, add items to a persistent shopping cart, and manage their profiles. Administrative users have additional privileges for managing products and categories.

**Completed Features:**
- Phase 1: Categories CRUD (Admin only)
- Phase 2: Product search bug fixes
- Optional Phase 3: Shopping Cart
- Optional Phase 4: User Profile Management
- Optional Phase 5 : Checking out

## Technologies Used

- **Language:** Java 17
- **Framework:** Spring Boot 3.x
- **Database:** MySQL 8.0
- **Authentication:** JWT (JSON Web Tokens)
- **Build Tool:** Maven
- **Testing:** JUnit, Postman/Insomnia
- **Architecture:** DAO Pattern with JDBC

## Architecture Overview

```
┌─────────────────┐
│   Controllers   │  ← REST endpoints, request validation
└────────┬────────┘
         │
┌────────▼────────┐
│   DAO Layer     │  ← Business logic interface
└────────┬────────┘
         │
┌────────▼────────┐
│  MySQL DAOs     │  ← Database operations (JDBC)
└────────┬────────┘
         │
┌────────▼────────┐
│  MySQL Database │
└─────────────────┘
```

**Key Components:**
- **Controllers:** Handle HTTP requests, validate input, invoke DAOs
- **DAO Interfaces:** Define data access contracts
- **MySQL DAO Implementations:** Execute SQL queries using JDBC
- **Models:** Plain Java objects (Product, Category, ShoppingCart, Profile, User)
- **Security:** Spring Security + JWT for authentication and authorization

## Database Setup

1. **Install MySQL** (if not already installed):
   ```bash
   # macOS (Homebrew)
   brew install mysql
   brew services start mysql
   ```

2. **Run the database script:**
   ```bash
   # Open MySQL Workbench or command line
   mysql -u root -p
   ```

3. **Execute the schema:**
   ```sql
   source database/create_database_clothingstore.sql;
   ```

4. **Verify the database:**
   ```sql
   USE easyshop;
   SHOW TABLES;
   -- Should show: users, profiles, categories, products, shopping_cart, orders, order_line_items
   ```

**Default Users:**
- Username: `user` | Password: `password` | Role: USER
- Username: `admin` | Password: `password` | Role: ADMIN
- Username: `george` | Password: `password` | Role: USER

## Running the API

1. **Clone the repository:**
   ```bash
   git clone 
   cd capstone-api-starter
   ```

2. **Configure database connection:**

   Edit `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/easyshop
   spring.datasource.username=root
   spring.datasource.password=your_password
   ```

3. **Build and run:**
   ```bash
   ./mvnw clean install
   ./mvnw spring-boot:run
   ```

4. **Verify the server is running:**
    - API: `http://localhost:8080`
    - Test endpoint: `GET http://localhost:8080/products`

5. **Access the frontend (optional):**
    - Navigate to `http://localhost:8080` in your browser
    - The clothing store UI will load

## Authentication

This API uses **JWT (JSON Web Tokens)** for authentication and authorization.

### Registration

**Endpoint:** `POST /register`

**Request Body:**
```json
{
  "username": "newuser",
  "password": "password",
  "confirmPassword": "password",
  "role": "USER"
}
```

**Response:** User object with details

### Login

**Endpoint:** `POST /login`

**Request Body:**
```json
{
  "username": "admin",
  "password": "password"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "id": 2,
    "username": "admin",
    "authorities": [
      { "name": "ROLE_ADMIN" }
    ]
  }
}
```

### Using the Token

For all protected endpoints, include the JWT token in the `Authorization` header:

```
Authorization: Bearer <your-jwt-token>
```

**In Postman/Insomnia:**
1. Go to the **Auth** tab
2. Select **Bearer Token**
3. Paste the token received from `/login`

## How It Works: Mental Models

Understanding how the system processes authenticated requests is crucial for working with this API.

### User's Mental Model (Shopping Cart Flow)

```
1. User logs in 
   → JWT token proves who they are

2. User adds products to cart 
   → Cart persists in database

3. User logs out 
   → Cart stays in database

4. User logs back in 
   → Same cart appears

5. User can view, update quantities, or clear cart
```

**Key Insight:** The shopping cart is NOT stored in memory or session. It lives in the MySQL database, tied to the user's ID. This means carts survive server restarts, logouts, and even browser closures.

### System's Mental Model (Complete Request Flow)

Here's what happens when a user makes an authenticated request (e.g., `GET /cart`):

```
┌─────────────────────────────────────────────────────────────┐
│ 1. HTTP Request with JWT                                    │
│    GET /cart                                                │
│    Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...            │
└────────────────────────┬────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────────┐
│ 2. Spring Security validates token → extracts username     │
│    - JWTFilter intercepts request                           │
│    - TokenProvider validates signature & expiration         │
│    - Username extracted from token payload                  │
└────────────────────────┬────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────────┐
│ 3. Controller receives Principal (contains username)        │
│    @GetMapping("")                                          │
│    public ShoppingCart getCart(Principal principal)         │
│    String username = principal.getName();                   │
└────────────────────────┬────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────────┐
│ 4. Look up userId from username using UserDao               │
│    User user = userDao.getByUserName(username);             │
│    int userId = user.getId();                               │
└────────────────────────┬────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────────┐
│ 5. Use userId to query shopping_cart table via DAO          │
│    ShoppingCart cart = shoppingCartDao.getByUserId(userId); │
│    - Executes SQL: SELECT * FROM shopping_cart              │
│                    WHERE user_id = ?                        │
│    - Joins with products table to get full product details  │
└────────────────────────┬────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────────┐
│ 6. Build ShoppingCart response object                       │
│    - ShoppingCart contains Map<Integer, ShoppingCartItem>   │
│    - Each ShoppingCartItem has Product + quantity           │
│    - Calculate line totals and cart total                   │
└────────────────────────┬────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────────┐
│ 7. Spring converts to JSON automatically                    │
│    - @RestController annotation handles conversion          │
│    - Jackson library serializes objects to JSON             │
└────────────────────────┬────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────────┐
│ 8. Return JSON to client                                    │
│    {                                                        │
│      "items": { ... },                                      │
│      "total": 129.97                                        │
│    }                                                        │
└─────────────────────────────────────────────────────────────┘
```

### Why This Architecture Matters

**Security:** The userId is ALWAYS derived from the JWT token, never from user input. This prevents users from accessing other users' carts by tampering with request parameters.

```java
// ❌ INSECURE - Never trust userId from request body
@GetMapping("/{userId}/cart")
public ShoppingCart getCart(@PathVariable int userId) { ... }

// ✅ SECURE - Always get userId from authenticated Principal
@GetMapping("/cart")
public ShoppingCart getCart(Principal principal) {
    String username = principal.getName();
    User user = userDao.getByUserName(username);
    int userId = user.getId(); // Safe to use
    ...
}
```

**Data Persistence:** Every cart operation writes to the database immediately, ensuring data integrity and enabling features like "abandoned cart recovery."

**Stateless API:** The server doesn't maintain session state. Each request is self-contained with the JWT token providing all necessary authentication information.

## API Endpoints

### Categories

| Method | Endpoint | Auth Required | Role | Description |
|--------|----------|---------------|------|-------------|
| GET | `/categories` | No | - | Get all categories |
| GET | `/categories/{id}` | No | - | Get category by ID |
| POST | `/categories` | Yes | ADMIN | Create new category |
| PUT | `/categories/{id}` | Yes | ADMIN | Update category |
| DELETE | `/categories/{id}` | Yes | ADMIN | Delete category |

**Example: Create Category**
```http
POST /categories
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "name": "Outerwear",
  "description": "Jackets, coats, and hoodies"
}
```

### Products

| Method | Endpoint | Auth Required | Role | Description |
|--------|----------|---------------|------|-------------|
| GET | `/products` | No | - | Get all products (supports filters) |
| GET | `/products/{id}` | No | - | Get product by ID |
| POST | `/products` | Yes | ADMIN | Create new product |
| PUT | `/products/{id}` | Yes | ADMIN | Update product |
| DELETE | `/products/{id}` | Yes | ADMIN | Delete product |

**Search/Filter Parameters:**
- `cat` (int) - Filter by category ID
- `minPrice` (decimal) - Minimum price
- `maxPrice` (decimal) - Maximum price
- `color` (string) - Filter by color (subCategory)

**Example: Search Products**
```http
GET /products?cat=1&minPrice=25&maxPrice=100
```

**Example: Create Product**
```http
POST /products
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "name": "Denim Jacket",
  "price": 89.99,
  "categoryId": 2,
  "description": "Classic blue denim jacket",
  "color": "Blue",
  "stock": 15,
  "imageUrl": "denim-jacket.jpg",
  "featured": true
}
```

### Shopping Cart

| Method | Endpoint | Auth Required | Description |
|--------|----------|---------------|-------------|
| GET | `/cart` | Yes | Get current user's cart |
| POST | `/cart/products/{productId}` | Yes | Add product to cart (qty +1) |
| PUT | `/cart/products/{productId}` | Yes | Update product quantity |
| DELETE | `/cart` | Yes | Clear entire cart |

**Example: Get Cart**
```http
GET /cart
Authorization: Bearer <user-token>
```

**Example Response:**
```json
{
  "items": {
    "1": {
      "product": {
        "productId": 1,
        "name": "White T-Shirt",
        "price": 19.99,
        "categoryId": 1,
        "description": "Classic cotton tee",
        "color": "White",
        "stock": 50,
        "imageUrl": "white-tshirt.jpg",
        "featured": false
      },
      "quantity": 2,
      "lineTotal": 39.98
    }
  },
  "total": 39.98
}
```

**Example: Add to Cart**
```http
POST /cart/products/5
Authorization: Bearer <user-token>
```

**Example: Update Quantity**
```http
PUT /cart/products/5
Authorization: Bearer <user-token>
Content-Type: application/json

{
  "quantity": 3
}
```

### User Profile

| Method | Endpoint | Auth Required | Description |
|--------|----------|---------------|-------------|
| GET | `/profile` | Yes | Get current user's profile |
| PUT | `/profile` | Yes | Update current user's profile |

**Example: Get Profile**
```http
GET /profile
Authorization: Bearer <user-token>
```

**Example: Update Profile**
```http
PUT /profile
Authorization: Bearer <user-token>
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "phone": "555-0123",
  "email": "john.doe@email.com",
  "address": "123 Main St",
  "city": "Seattle",
  "state": "WA",
  "zip": "98101"
}
```

## Testing the API

### Using Postman/Insomnia

1. **Create a new request collection**

2. **Test authentication flow:**
    - Send POST to `/login` with valid credentials
    - Copy the `token` from the response
    - Set up Bearer Token authentication for subsequent requests

3. **Test public endpoints (no auth needed):**
   ```
   GET /categories
   GET /products
   GET /products?cat=1&minPrice=20&maxPrice=50
   ```

4. **Test authenticated endpoints:**
   ```
   GET /cart (USER)
   POST /cart/products/3 (USER)
   GET /profile (USER)
   POST /categories (ADMIN only)
   PUT /products/1 (ADMIN only)
   ```

5. **Verify database changes:**
   ```sql
   SELECT * FROM shopping_cart WHERE user_id = 1;
   SELECT * FROM profiles WHERE user_id = 1;
   ```

### Running Unit Tests

```bash
./mvnw test
```

Tests are located in `src/test/java/org/yearup/data/mysql/`

## Optional Features Implemented

✅ **Phase 3: Shopping Cart**
- Persistent cart storage in MySQL
- Add/update/delete cart items
- Cart survives logout/login
- User-specific cart isolation

✅ **Phase 4: User Profile**
- View profile endpoint
- Update profile endpoint
- Automatic profile creation on registration
- One-to-one relationship with users

## Known Issues / Assumptions

### Assumptions
- Users cannot delete their own accounts
- Product stock is not decremented on cart add (would be implemented in Phase 5 checkout)
- Shopping cart does not enforce stock limits
- All prices are in USD
- Image URLs reference local files in `/images/products/`

### Limitations
- No email verification on registration
- No password reset functionality
- No pagination on product listing (could cause performance issues with large catalogs)
- No order history (requires Phase 5 implementation)

### Security Notes
- Passwords are hashed using BCrypt
- JWT tokens expire after a configured duration
- User IDs are extracted from JWT tokens (not trusted from request body)
- ADMIN role is required for category and product management

## Project Structure

```
src/main/java/org/yearup/
├── EasyshopApplication.java          # Main Spring Boot entry point
├── configurations/
│   └── DatabaseConfig.java           # DataSource configuration
├── controllers/
│   ├── AuthenticationController.java # Login/Register
│   ├── CategoriesController.java     # Category CRUD
│   ├── ProductsController.java       # Product CRUD + Search
│   ├── ShoppingCartController.java   # Cart operations
│   └── ProfileController.java        # User profile management
├── data/
│   ├── CategoryDao.java              # Interface
│   ├── ProductDao.java               # Interface
│   ├── ShoppingCartDao.java          # Interface
│   ├── ProfileDao.java               # Interface
│   ├── UserDao.java                  # Interface
│   └── mysql/
│       ├── MySqlCategoryDao.java     # Implementation
│       ├── MySqlProductDao.java      # Implementation (bug fixes)
│       ├── MySqlShoppingCartDao.java # Implementation
│       ├── MySqlProfileDao.java      # Implementation
│       └── MySqlUserDao.java         # Implementation
├── models/
│   ├── Category.java
│   ├── Product.java
│   ├── ShoppingCart.java
│   ├── ShoppingCartItem.java
│   ├── Profile.java
│   └── User.java
└── security/
    ├── WebSecurityConfig.java        # Security configuration
    └── jwt/
        ├── TokenProvider.java        # JWT generation/validation
        └── JWTFilter.java            # Request filter
```
## UML Diagram
![UML Diagram.png](src/main/resources/screenshots/UML%20Diagram.png)
## Future Improvements

If additional time were available, the following features would enhance the application:

- **Checkout/Orders (Phase 5):** Convert cart to order, generate order confirmation
- **Pagination:** Add page size and page number parameters to product listing
- **Stock Management:** Decrement product stock on order creation
- **Order History:** Allow users to view past orders
- **Email Notifications:** Send confirmation emails on registration and order placement
- **Image Upload:** Allow admins to upload product images via API
- **Discount Codes:** Apply promotional codes at checkout
- **Product Reviews:** Let users rate and review products

---

**Author:** Ermiyas H.
**Course:** LTCA 
**Institution:** Year Up  
**Date:** December 2024