This project is an e-commerce platform named "mall-shop". It includes two main modules: `tigshop-adminapi` and `tigshop-api`, which serve as the backend management system and frontend user system respectively.

---

## 📦 Project Structure

The project is divided into multiple modules:

### 1. **tigshop-adminapi**
This module serves as the **admin backend API** system, providing management capabilities for products, orders, users, and other system settings.

- **Controllers**: 
  - `ProductController`, `OrderController`, `UserController`, `BrandController`, `CategoryController`, etc., handle admin-side operations.
  - Security is managed via Spring Security and custom permission checks (`@PreAuthorize`).
- **Features**:
  - Product management (create, update, delete, batch import/export).
  - Order management (status updates, refunds, logistics).
  - User and role management.
  - Marketing tools (coupons, promotions, flash sales).
  - Store and merchant management.
  - Financial tools (balance logs, recharge, withdrawal).
  - Content management (articles, banners, decoration).
  - Multi-language and internationalization support.
  - Settings and configurations (payment, shipping, store, etc.).

### 2. **tigshop-api**
This module serves as the **frontend API** for end users and mobile clients.

- **Controllers**:
  - `ProductController`, `CartController`, `OrderController`, `UserController`, `SalesmanController`, etc.
  - Handles user-facing operations like browsing, purchasing, and account management.
- **Features**:
  - Product browsing and search.
  - Shopping cart and checkout.
  - Order placement and tracking.
  - User account management (login, registration, profile).
  - Marketing features (coupons, flash sales,积分兑换).
  - Salesman/distribution system.
  - Storefront decoration and navigation.
  - Multi-language and translation support.

### 3. **tigshop-bean**
This module contains **shared data models and DTOs** used across both admin and user-facing APIs.

- Includes:
  - Data Transfer Objects (DTOs).
  - Business Objects (BOs).
  - Constants and utility classes.
  - Permission and configuration annotations.

### 4. **Database**
- Contains SQL scripts for database upgrades and initialization.
- Supports versioned migrations (e.g., `2.0.5-2.0.6update.sql`).

### 5. **Docker**
- Dockerfiles for both `tigshop-adminapi` and `tigshop-api`.
- Uses JDK 21 and RabbitMQ plugin for message queue support.

---

## 🚀 Key Features

### Admin Features
- **Product Management**: Full CRUD operations, batch import/export, inventory logs.
- **Order Management**: Order status updates, refunds, logistics tracking.
- **User Management**: Admin and customer accounts, roles, permissions.
- **Marketing Tools**: Coupons, flash sales,积分兑换, promotions.
- **Store Management**: Shop setup, store locations, merchant applications.
- **Financial Tools**: Recharge, withdrawal, balance logs.
- **Content Management**: Articles, banners, decoration modules.
- **Multi-language Support**: Translation management, locale settings.
- **Settings**: Payment, shipping, store, and system-wide configurations.

### User Features
- **Product Browsing**: Search, filtering, and recommendations.
- **Shopping Cart**: Add, update, remove items, and checkout.
- **Order System**: Place orders, track status, apply for refunds.
- **User Account**: Login, registration, profile management, address book.
- **Salesman/Distribution**: Referral system, commission tracking, marketing materials.
- **Multi-language**: Locale selection and translation support.
- **Mobile Optimization**: Designed for mobile clients with responsive UI.

---

## 🛠️ Technologies Used

- **Backend**: Java (Spring Boot 21)
- **Database**: MySQL (with MyBatis for ORM)
- **Message Queue**: RabbitMQ
- **Search**: ElasticSearch
- **Caching**: Redis
- **Authentication**: Spring Security + JWT
- **Deployment**: Docker
- **Build Tool**: Maven (`pom.xml`)

---

## 📁 Configuration Files

- `application.yaml`, `application-dev.yaml`, `application-docker.yaml`: Environment-specific configurations.
- `logback.xml`: Logging configuration.
- `pom.xml`: Maven project configuration.
- `build-and-run.sh` / `build-and-run.bat`: Build and run scripts for Linux/Windows.

---

## 📌 License

The project uses standard open-source licensing. Please refer to the repository for the exact license file.

---

## 📝 Summary

This is a full-featured e-commerce platform with a robust admin backend and a user-facing frontend. It supports multi-language, multi-store, and distribution systems, making it suitable for medium to large-scale online retail operations.

If you're looking to deploy or extend this platform, it's recommended to start with the Docker setup and review the configuration files for customization.