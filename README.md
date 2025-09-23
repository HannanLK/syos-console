# 🏪 SYOS — Synex Outlet Store (Console)

A clean-architecture, CLI-based retail system for POS and Web sales with batch-aware inventory and reporting. Minimal to run, opinionated to learn.


## Table of Contents
- Overview
- Features
- Tech Stack
- Architecture (Clean Architecture + Patterns + SOLID)
- Installation (Windows/psql)
- Test Users
- How to Run
- Reports
- Assumptions


## Overview
SYOS replaces manual checkout and stock books with a single console application. It supports multi‑role access (Customer/Employee/Admin), POS and Web transaction flows, batch tracking with FIFO and expiry override, separate inventory pools, loyalty points, and PDF bills — all while demonstrating clean code and software design best practices.


## Features
- Item Master and product hierarchy (Brands, Categories, Suppliers)
- Batch tracking (mfg/expiry), FIFO with expiry-date override
- Three inventory pools: WAREHOUSE_STOCK, SHELF_STOCK (POS), WEB_INVENTORY (Web)
- POS flow with cash tender and automatic change, sequential bills (PDF + console)
- Web flow with cart, card simulation (fails only for 0767600730204128), and SYNEX points
- Stock transfers between pools; separate availability per channel
- Low‑stock threshold (50) and basic notifications
- Employee personal purchase mode (basic): no discounts/price edits, LKR 10,000 limit, simple audit log
- Role‑based access control with BCrypt passwords and session management
- Standard reports (daily sales, channel sales, stock, inventory by location, reorder, bill report)


## Tech Stack
- Language/Build: Java 24, Maven 3.8+
- Database/ORM: PostgreSQL 12+, Hibernate ORM (JPA 3.2), HikariCP
- Security/Validation: jBCrypt, Jakarta Validation (Hibernate Validator)
- Logging: SLF4J + Logback
- PDF/Utilities: Apache PDFBox, Apache Commons (Lang, CSV), Lombok
- Testing/Quality: JUnit 5, Mockito, Testcontainers (PostgreSQL), JaCoCo


## Architecture (Clean Architecture + Patterns + SOLID)
- Layers
  - Domain: Entities (User, Product, Batch, Stock, Transaction), Value Objects (Money, ItemCode, Quantity, Email), Specifications, Domain Events
  - Application: Use cases (Auth, POS/Web billing, Inventory, Reporting), Strategies (stock selection FIFO/expiry, payments), Services (Discounts, EventBus), Ports
  - Interface Adapters: CLI controllers/menus, presenters, repository adapters
  - Infrastructure: JPA/Hibernate, PostgreSQL, security, logging, PDF, configuration
- Design Patterns (11)
  - Observer, Strategy, Factory, Command, State, Singleton, Builder, Template Method, Proxy, Specification, Repository
- SOLID and Clean Code
  - SRP/ISP: Use cases and ports are small and specific
  - OCP/DIP: New strategies or persistence adapters plug via interfaces
  - Clean code: meaningful names, small methods, defensive validation


## Installation (Windows/psql)
1) Prerequisites
   - Java 21+ (built with 24), Maven 3.8+, PostgreSQL 12+
2) Clone
   - cd D:\4th_final\sem1\clean_cod\syos\syos-console
3) Create database (psql)
   - psql -U postgres -f "src\main\resources\db\migration\database-setup.sql"
4) Run migrations (in order)
   - PowerShell one‑liner:
     - Get-ChildItem "src\main\resources\db\migration\V*.sql" | Sort-Object Name | ForEach-Object { psql -U postgres -d syosdb -f $_.FullName }
   - Or run key scripts manually (V1 … V11) from the same folder
5) Build
   - mvn clean compile


## Test Users
On first start the app ensures default users exist.
- admin / admin12345 — ADMIN
- employee / employee123 — EMPLOYEE
- customer / customer123 — CUSTOMER


## How to Run
- Option A (from IDE): Run com.syos.Main
- Option B (Maven Exec): mvn exec:java -Dexec.mainClass="com.syos.Main" -DAPP_ENV=development
  - APP_ENV=development enables richer console logs; omit for production‑like output


## Reports (built-in)
- Daily Sales: totals, items, qty, revenue (by day, by channel)
- Channel Sales: POS vs Web comparison
- Stock Report: batch‑wise with mfg/expiry
- Inventory Location: warehouse/shelf/web levels
- Reorder: items below 50
- Bill Report: all bills with channel identification


## Assumptions
- Currency: LKR; taxes not modeled
- Single‑user CLI (no concurrent sessions)
- PDF files stored locally; no external services
- Loyalty: 1% per 100 LKR spent; no partial payments
- Employee personal purchase: LKR 10,000 limit; discounts disabled in that mode
- Soft deletes via status fields where applicable; audit timestamps recorded


—
Built for learning and demonstration purposes; see code and tests for further details.