# Menu Service - Food Delivery App Assignment

A RESTful API service for managing restaurant menus with multi-level caching strategy (L1: Caffeine, L2: Redis, L3: Database) built using Spring Boot.

## 🎯 Assignment Overview

This project implements two core REST APIs for a Food Delivery App's MenuService:

1. **Add Restaurant with Menu Items** - Creates restaurant and associated menu items directly in database
2. **Get Restaurant Menu** - Retrieves menu with multi-level caching (L1 → L2 → Database lookup)

## 🏗️ Architecture

### Multi-Level Caching Strategy
- **L1 Cache (Caffeine)**: In-memory cache for fastest access
- **L2 Cache (Redis)**: Distributed cache for scalability
- **L3 Storage (Database)**: H2 in-memory database with MySQL support

### Cache Flow
```
API Request → L1 Cache (Caffeine) → L2 Cache (Redis) → Database
                    ↓                    ↓              ↓
                Return ←── Update L1 ←── Update L1 & L2
```

## 🗄️ Database Schema

### Restaurant Table
```sql
CREATE TABLE restaurant (
    restaurant_id BINARY(16) PRIMARY KEY,    -- UUID in binary format
    name VARCHAR(100) NOT NULL,              -- Restaurant name
    address VARCHAR(255)                     -- Physical address (nullable)
);
```

### Menu Items Table
```sql
CREATE TABLE menu_items (
    menu_id BINARY(16) PRIMARY KEY,          -- UUID in binary format
    restaurant_id BINARY(16) NOT NULL,       -- Foreign key to restaurant
    name VARCHAR(100) NOT NULL,              -- Menu item name
    price DECIMAL(7,2) NOT NULL,             -- Item price
    availability BOOLEAN NOT NULL DEFAULT TRUE, -- Item availability
    veg BOOLEAN NOT NULL,                    -- Vegetarian status
    FOREIGN KEY (restaurant_id) REFERENCES restaurant(restaurant_id)
);
```

## 📋 API Endpoints

### 1. Add Restaurant with Menu Items
**Creates a new restaurant and its associated menu items**

```http
POST /api/v1/restaurants
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "Pizza Palace",
  "address": "123 Main Street, Downtown",
  "menuItems": [
    {
      "name": "Margherita Pizza",
      "price": 12.99,
      "availability": true,
      "veg": true
    },
    {
      "name": "Pepperoni Pizza", 
      "price": 14.99,
      "availability": true,
      "veg": false
    }
  ]
}
```

**Success Response (201 Created):**
```json
{
  "success": true,
  "message": "Restaurant created successfully",
  "data": {
    "restaurantId": "123e4567-e89b-12d3-a456-426614174000",
    "name": "Pizza Palace",
    "address": "123 Main Street, Downtown",
    "menuItems": [
      {
        "menuId": "987fcdeb-51a2-43d1-b345-567890123456",
        "name": "Margherita Pizza",
        "price": 12.99,
        "availability": true,
        "veg": true
      }
    ]
  },
  "timestamp": "2025-01-15T10:30:00.123"
}
```

### 2. Get Restaurant Menu (Multi-Level Caching)
**Retrieves restaurant menu with L1 → L2 → Database caching strategy**

```http
GET /api/v1/restaurants/{restaurantId}/menu
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Menu retrieved successfully",
  "data": {
    "restaurantId": "123e4567-e89b-12d3-a456-426614174000",
    "name": "Pizza Palace",
    "address": "123 Main Street, Downtown",
    "menuItems": [
      {
        "menuId": "987fcdeb-51a2-43d1-b345-567890123456",
        "name": "Margherita Pizza",
        "price": 12.99,
        "availability": true,
        "veg": true
      }
    ]
  },
  "timestamp": "2025-01-15T10:30:00.123"
}
```

### Additional Endpoints
- **Health Check**: `GET /api/v1/restaurants/health`

## ⚡ Caching Implementation

### Cache Behavior
1. **First API call**: Database hit → Updates both L1 and L2 caches
2. **Subsequent calls**: L1 cache hit (fastest response)
3. **After L1 expiry**: L2 cache hit → Updates L1 cache
4. **After both expiry**: Database hit → Updates both caches

### Cache Configuration
- **L1 (Caffeine)**: 30-minute expiration, 1000 max entries
- **L2 (Redis)**: 1-hour TTL, embedded Redis for development

## 🛠️ Technology Stack

- **Framework**: Spring Boot 3.2.x
- **Language**: Java 17
- **Database**: H2 (development), MySQL-ready for production
- **Caching**: Caffeine (L1), Redis (L2) 
- **Logging**: SLF4J with Logback
- **Validation**: Spring Boot Validation
- **Build Tool**: Maven

## 🚀 Setup and Running

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher

### Steps to Run
1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd menu-service
   ```

2. **Build and run the application**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

3. **Access the application**
   - API Base URL: `http://localhost:8080/api/v1/restaurants`
   - H2 Console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:menudb`, Username: `sa`, Password: `password`)
   - Health Check: `http://localhost:8080/api/v1/restaurants/health`

## 🧪 Testing with Postman

### Test Restaurant Creation
**Method**: POST  
**URL**: `http://localhost:8080/api/v1/restaurants`  
**Headers**: `Content-Type: application/json`  
**Body**: Use the JSON sample from API documentation above

### Test Menu Retrieval  
**Method**: GET  
**URL**: `http://localhost:8080/api/v1/restaurants/{restaurantId}/menu`  
(Use the `restaurantId` from the creation response)

### Expected Cache Logs
**First call** (Database hit):
```
Cache MISS in L1 (Caffeine) for restaurant: {id}
Cache MISS in L2 (Redis) for restaurant: {id}
Cache miss - retrieving from database for restaurant: {id}
L1 Cache updated for restaurant: {id}
L2 Cache updated for restaurant: {id} with TTL: 3600 seconds
```

**Second call** (L1 cache hit):
```
Cache HIT in L1 (Caffeine) for restaurant: {id}
Menu retrieved from L1 cache for restaurant: {id}
```

## 🔍 Error Handling

### HTTP Status Codes
- **201 Created**: Restaurant successfully created
- **200 OK**: Menu retrieved successfully
- **400 Bad Request**: Validation errors, invalid input
- **404 Not Found**: Restaurant not found
- **409 Conflict**: Duplicate restaurant name
- **500 Internal Server Error**: Unexpected errors

### Error Response Format
```json
{
  "success": false,
  "message": "Restaurant not found with ID: 123e4567-e89b-12d3-a456-426614174000",
  "data": null,
  "timestamp": "2025-01-15T10:30:00.123"
}
```

## 📁 Project Structure

```
menu-service/
├── pom.xml
├── logs/
│   └── menu-service.log                    # Application logs
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/fooddelivery/menuservice/
│   │   │       ├── MenuServiceApplication.java
│   │   │       ├── config/
│   │   │       │   ├── CacheConfig.java              # Cache configuration
│   │   │       │   └── EmbeddedRedisConfig.java      # Redis setup
│   │   │       ├── controller/
│   │   │       │   └── MenuController.java           # REST endpoints
│   │   │       ├── dto/
│   │   │       │   ├── ApiResponse.java              # Standard response wrapper
│   │   │       │   ├── CreateRestaurantRequest.java  # Request DTO
│   │   │       │   ├── MenuItemDTO.java              # Menu item DTO
│   │   │       │   └── RestaurantDTO.java            # Restaurant DTO
│   │   │       ├── entity/
│   │   │       │   ├── MenuItem.java                 # Menu item entity
│   │   │       │   └── Restaurant.java               # Restaurant entity
│   │   │       ├── exception/
│   │   │       │   ├── DuplicateResourceException.java
│   │   │       │   ├── GlobalExceptionHandler.java   # Centralized error handling
│   │   │       │   └── ResourceNotFoundException.java
│   │   │       ├── repository/
│   │   │       │   ├── MenuItemRepository.java       # Data access layer
│   │   │       │   └── RestaurantRepository.java
│   │   │       └── service/
│   │   │           ├── CacheService.java             # Multi-level caching logic
│   │   │           └── MenuService.java              # Business logic
│   │   └── resources/
│   │       └── application.properties                # Configuration
```

## 📊 Logging

### Console Output
Real-time logs showing:
- API requests and responses
- Cache hit/miss behavior
- Database operations
- Error scenarios

### File Logging
- **Location**: `logs/menu-service.log`
- **Rotation**: 50MB max size, 10 files history
- **Content**: All application events with timestamps

## 🎯 RESTful Design Features

- **Resource-based URLs**: `/restaurants/{id}/menu`
- **HTTP Methods**: POST (create), GET (retrieve)
- **Status Codes**: 200, 201, 400, 404, 409, 500
- **Content Negotiation**: JSON format
- **Stateless**: No server-side session state
- **Uniform Interface**: Consistent API design
- **Validation**: Request validation with meaningful error messages

## 🔄 Database Migration Support

### Current Setup (Development)
```properties
spring.datasource.url=jdbc:h2:mem:menudb
spring.datasource.driver-class-name=org.h2.Driver
```

### Production Ready (MySQL)
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/menudb
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.username=root
spring.datasource.password=password
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
```

## ✅ Assignment Completion

### Core Requirements Met
- ✅ **API 1**: Add restaurant and menu items (direct database insertion)
- ✅ **API 2**: Get restaurant menu with multi-level caching (L1→L2→DB)
- ✅ **RESTful Design**: Proper HTTP methods, status codes, resource-based URLs
- ✅ **Database Schema**: Exact schema as specified (UUIDs as BINARY(16))
- ✅ **Caching Strategy**: Caffeine (L1) → Redis (L2) → Database (L3)
- ✅ **H2 Support**: In-memory database with MySQL migration path
- ✅ **Professional Code**: Exception handling, validation, logging

### Key Features Implemented
- Multi-level caching with clear logging
- Comprehensive error handling with proper HTTP status codes
- Input validation with meaningful error messages
- UUID-based primary keys stored as BINARY(16)
- Structured logging for debugging and monitoring
- RESTful API design following industry standards

## 📞 API Testing

The application includes sample data and can be tested immediately after startup. Use the Postman examples provided above to test both APIs and observe the caching behavior through console logs.