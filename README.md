# ✨ SYOS — Synex Outlet Store (Console)

Clean-architecture, CLI retail system for POS + Web with smart inventory, batch FIFO/expiry logic, loyalty, and rich reporting — optimized for learning and demos.

<p align="center">
  <img alt="Java" src="https://img.shields.io/badge/Java-24-007396?logo=java&logoColor=white" />
  <img alt="Maven" src="https://img.shields.io/badge/Maven-3.8+-C71A36?logo=apachemaven&logoColor=white" />
  <img alt="PostgreSQL" src="https://img.shields.io/badge/PostgreSQL-12+-336791?logo=postgresql&logoColor=white" />
  <img alt="JUnit" src="https://img.shields.io/badge/Tests-JUnit%205-25A162?logo=junit5&logoColor=white" />
  <img alt="License" src="https://img.shields.io/badge/Architecture-Clean-blue" />
</p>

---

## 🔗 Table of Contents
- [Overview](#-overview)
- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [Installation](#-installation)
- [How to Run](#-how-to-run)
- [Test Users](#-test-users)
- [Reports](#-reports)
- [Assumptions](#-assumptions)
- [Credits](#-credits)

> Tip: Use the links above to jump to any section. [Back to top](#-table-of-contents)

---

## 📌 Overview
SYOS replaces manual checkout and stock books with a single console app. It supports role-based access (Customer/Employee/Admin), separate POS and Web flows, PDF bills, and inventory pools with smart batch selection (FIFO with expiry override). It also demonstrates SOLID, Clean Architecture, and common design patterns.

---

## 🚀 Features
- Item Master with hierarchy (Brands, Categories, Suppliers)
- Batch tracking (manufacture/expiry), FIFO with expiry-date override
- Three inventory pools: WAREHOUSE_STOCK, SHELF_STOCK (POS), WEB_INVENTORY (Web)
- POS: cash tender, automatic change, sequential bill numbers, PDF + console bill
- Web: cart + session, card simulation (any 16 digits except 0767600730204128 fails), SYNEX points
- Stock transfers between pools; channel-specific availability
- Low‑stock threshold (50) notifications and basic alerts
- Employee Personal Purchase mode: no discounts/price edits, LKR 10,000 limit, simple audit log
- Role-based access, BCrypt passwords, sessions
- Built-in reports: daily sales, channel sales, stock, inventory by location, reorder, bill report

[Back to top](#-table-of-contents)

---

## 🛠 Tech Stack
- Language/Build: Java 24, Maven 3.8+
- Database/ORM: PostgreSQL 12+, Hibernate ORM (JPA 3.2), HikariCP
- Security/Validation: jBCrypt, Jakarta Validation (Hibernate Validator)
- Logging: SLF4J + Logback
- PDF/Utilities: Apache PDFBox; Apache Commons; Lombok
- Testing/Quality: JUnit 5, Mockito, Testcontainers (PostgreSQL), JaCoCo

[Back to top](#-table-of-contents)

---

## 🧭 Architecture
- Clean Architecture layers:
  - Domain: Entities (User, Product, Batch, Stock, Transaction), Value Objects (Money, ItemCode, Quantity, Email), Specifications, Domain Events
  - Application: Use cases (Auth, POS/Web, Inventory, Reporting), Strategies (FIFO/Expiry stock select, Payment), Services, Ports
  - Interface Adapters: CLI controllers/menus, presenters, repository adapters
  - Infrastructure: PostgreSQL + Hibernate, Security, Logging, PDF, Config
- Design Patterns in use (8–11): Observer, Strategy, Factory, Command, State, Singleton, Builder, Template Method, Proxy, Specification, Repository
- SOLID: SRP/ISP via focused use cases/ports, OCP/DIP via abstractions, clean naming and small methods

Inventory flow:
```
Supplier → WAREHOUSE_STOCK → {
    ├── SHELF_STOCK (POS)
    └── WEB_INVENTORY (Web)
}
```
Batch selection: FIFO unless a newer batch expires sooner (expiry override)

[Back to top](#-table-of-contents)

---

## 🧩 Installation (Windows + PostgreSQL)
1. Prerequisites
   - Java 21+ (tested with 24), Maven 3.8+, PostgreSQL 12+
2. Open a terminal and change directory:
   - cd D:\\4th_final\\sem1\\clean_cod\\syos\\syos-console
3. Create database and roles (psql):
   - psql -U postgres -f "src\\main\\resources\\db\\migration\\database-setup.sql"
4. Run migrations in order (PowerShell):
   - Get-ChildItem "src\\main\\resources\\db\\migration\\V*.sql" | Sort-Object Name | ForEach-Object { psql -U postgres -d syosdb -f $_.FullName }
5. Build
   - mvn clean compile

[Back to top](#-table-of-contents)

---

## ▶️ How to Run
- From IDE: run main class com.syos.Main
- Via Maven Exec:
  - mvn exec:java -Dexec.mainClass="com.syos.Main" -DAPP_ENV=development
  - APP_ENV=development enables richer console logs

[Back to top](#-table-of-contents)

---

## 👥 Test Users
Default users are created/ensured on startup (see DatabaseInitializer).

| Role     | Username | Password  | Notes |
|----------|----------|-----------|-------|
| Admin    | 1000     | 1qaz!QAZ  | System Admin |
| Employee | 3033     | 1qaz!QAZ  | Test Employee |
| Customer | 2303     | 1qaz!QAZ  | Test Customer |

[Back to top](#-table-of-contents)

---

## 📊 Reports
- Daily Sales: totals, items, quantity, revenue (by day, by channel)
- Channel Sales: POS vs Web
- Stock Report: batch‑wise with manufacture/expiry
- Inventory Location: warehouse/shelf/web levels
- Reorder: items below threshold (50)
- Bill Report: all bills with channel identification

[Back to top](#-table-of-contents)

---

## 📎 Assumptions
- Currency: LKR; no tax modeled
- Single‑user CLI (no concurrent sessions)
- Local PDF output; no external services
- Loyalty: 1% per 100 LKR; full payment only
- Employee personal purchase: LKR 10,000 limit; discounts disabled in that mode
- Soft deletes via status fields; timestamps and basic audits

[Back to top](#-table-of-contents)

---

## ❤️ Credits
Made with love by Hannan Munas