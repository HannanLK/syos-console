# Synex Outlet Store (SYOS) - Complete Updated Scenario

## 1. Enhanced Business Requirements

### Core System Overview
The Synex Outlet Store (SYOS) automation system replaces manual processes with a comprehensive CLI-based solution supporting multiple user roles, transaction types, and inventory management with clean architecture principles.

### Primary Functional Requirements

#### 1.1 Item Management & Product Catalog
- **Item Master**: Unique item codes identify products in the system
- **Product Hierarchy**: Categories → Sub-categories → Brands → Products
- **Product Information**: Name, description, unit of measure (each/pack/Kg/L), pack size, cost price, selling price, supplier details etc
- **Status Management**: Active/Inactive products with lifecycle tracking
- **Batch Tracking**: Manufacturing date, expiry date, quantity received/available per batch

#### 1.2 Multi-Channel Transaction Processing

**Point of Sale (POS) Transactions:**
- Employee-operated cash-only transactions
- Item code entry with quantity specification
- Running total display during transaction
- Cash tender input with automatic change calculation
- **Uses SHELF_STOCK inventory pool exclusively**
- Bill generation (console + PDF) with: item name, quantity, unit price, total price, full amount, discount, cash tendered, change, serial number, date/time
- Sequential bill numbering starting from 1
- Transaction type marked as "POS" for reporting

**Web Transactions:**
- Customer-initiated online purchases
- **Uses WEB_INVENTORY pool (separate from shelf stock)**
- Card payment simulation (success: any 16-digit except "0767600730204128" is identified as failure)
- Shopping cart functionality with session management
- Stock reservation during cart (released if not purchased within timeout)
- SYNEX Points integration (1% per 100 LKR spent)
- Transaction type marked as "WEB" for reporting

**Inventory Pool Management:**
- **STORE_STOCK**: Warehouse/backroom inventory (source for both channels)
- **SHELF_STOCK**: Physical store shelves (POS transactions only)
- **WEB_INVENTORY**: Online store allocation (web transactions only)
- **Stock Transfers**: Employees can move stock between pools
- **Separate Availability**: Each channel shows only its allocated inventory

#### 1.3 Advanced Stock Management & Fulfillment Strategy

**Three-Tier Inventory Architecture:**
- **WAREHOUSE_STOCK**: Central inventory pool (source for all channels)
- **SHELF_STOCK**: Physical store display inventory (POS transactions only)
- **WEB_INVENTORY**: Virtual allocation (Web transaction only)

**Fulfillment Strategy:**
- **Web Orders**: Fulfilled from WEB_STOCK 
- **POS Orders**: Fulfilled from SHELF_STOCK (physical items on shelves)
- **Shelf Replenishment**: Employee-triggered based on:
    - Low stock notifications (below reorder point which is 50)
    - Customer foot traffic patterns
    - Seasonal demand analysis
    - Employee judgment and experience

**Smart Restocking Logic:**
- **Automatic Notifications**: Alert employees when shelf stock falls below threshold
- **Batch Selection Strategy**:
    - **Primary Rule**: FIFO (First In, First Out) - sell oldest batch first
    - **Override Rule**: If newer batch expires sooner, prioritize by expiry date
    - **Example**: Batch A (Jan 1st, expires March 31st) vs Batch B (Feb 15th, expires Feb 28th) → Sell Batch B first
- **Demand-Based Suggestions**: System recommends restocking quantities based on:
    - Historical sales patterns
    - Current foot traffic
    - Day of week/seasonal trends
    - Upcoming promotions
- **Employee Override**: Staff can manually restock based on observed customer behavior
- **Consistent Logic**: Both shelf and web fulfillment follow same batch selection rules

**Inventory Flow:**
```
Supplier → WAREHOUSE_STOCK → {
    ├── SHELF_STOCK (Employee-triggered, demand-based)
    └── WEB_INVENTORY (Employee-triggered, )
}
```

#### 1.4 User Management & Authentication
- **Customer Role**: Browse, cart management, purchase, order history, SYNEX Points viewing, account management
- **Employee Role**: POS operations, stock management, report viewing, **personal purchases**
- **Admin Role**: All employee functions + user management + comprehensive analytics
- **Security**: BCrypt password hashing, session management, role-based access control

**Employee Personal Purchase Feature:**
- **Personal Purchase Mode**: Employees can switch POS to personal purchase mode for buying items
- **Automatic Restrictions**: System prevents discount application, price modifications, and transaction voids during personal purchases
- **Simple Workflow**: Employee switches mode → scans items → pays with own money → returns to work mode
- **Basic Audit**: Simple logging of employee purchases (employee ID, items, amount, timestamp)
- **Visual Indicator**: Clear mode indication on POS display to prevent confusion
- **Single Transaction Limit**: Basic limit to prevent excessive purchases (LKR 10,000)

## 2. Enhanced Application Flow

### 2.1 Welcome & Navigation
```
Welcome to Synex Outlet Store, 77 Hortan Pl, Colombo 07
A system by Hannanlk

Main Menu:
1. Browse Products
2. Login
3. Register
4. Exit
```

### 2.2 Product Browsing (Guest & Customer)
- **View All Products**: Dynamically categorized display
- **Category Navigation**: Hierarchical browsing (Beverages → Soft Drinks → Coca-Cola)
- **Featured Section**: Highlighted products
- **Latest Products**: Recently added items
- **Search Functionality**: By name, brand, category
- **Cart Management**: Add to cart (requires login for purchase)
- **Navigation**: "Go Back" and "Go to Main Menu" options
- **Web Simulation**: Introduce deliberate delays to simulate web experience

### 2.3 Role-Based Functionality

**Customer Dashboard:**
- Browse Products & Cart Management
- Purchase & Checkout
- View Order History
- View SYNEX Points Balance
- Account Information Management
- Member Since Date Display

**Employee Dashboard:**
```
Employee Menu:
1. Process Customer Transaction
2. Add Products
3. View Stock
4. Generate Reports
5. Personal Purchase
6. Logout
```

**Personal Purchase Mode Flow:**
```
*** PERSONAL PURCHASE ***
Employee: [Name]
[Discounts and price changes disabled]

1. Scan Items
2. Complete Purchase
3. Cancel & Return to Work
```

**Admin Dashboard:**
- All Employee Functions
- **Personal Purchase Oversight**: View basic employee purchase logs (no complex reporting)
- User Management (Create/Edit/Deactivate)
- Advanced Analytics & Insights
- System Configuration
- Comprehensive Reporting

### 2.4 Registration Process
- Default role: Customer
- Required fields: Name, username (unique), password, email
- Auto-capture: Registration date, initial SYNEX Points (0)
- Validation: Email format, password strength, username availability

## 3. Comprehensive Reporting System

### 3.1 Standard Reports
- **Daily Sales Report**: Total revenue, order numbers/values, items sold with quantities
- **Channel Sales Report**: Separate POS vs Web transaction analysis
- **Stock Report**: Batch-wise details with manufacturing/expiry dates by location
- **Inventory Location Report**: Stock levels across STORE/SHELF/WEB pools
- **Reorder Report**: Items below 50 units threshold (across all locations)
- **Bill Report**: All customer transactions (POS + Web) with channel identification
- **Reshelving Report**: Items requiring shelf restocking from store stock
- **Web Allocation Report**: Items requiring web inventory replenishment

### 3.2 Advanced Retail Insights
- **Inventory Turnover**: Sales velocity analysis per product/category by channel
- **Peak Shopping Hours**: Transaction timestamp analysis (POS vs Web patterns)
- **Product Category Performance**: Sales performance by category and channel
- **Channel Performance Analysis**: Revenue, transaction count, average order value comparison
- **Customer Behavior Analytics**: Purchase patterns, channel preference analysis
- **Cross-Channel Insights**: Customers who use both POS and Web
- **Inventory Efficiency**: Stock allocation effectiveness across channels
- **Seasonal Trends**: Time-based sales pattern identification by channel

### 3.3 Real-Time Notifications
- **Reorder Alerts**: Displayed on employee/admin login
- **Expiry Warnings**: Products approaching expiration
- **Low Stock Notifications**: Real-time inventory alerts
- **User Actions**: View now or later option for all notifications

## 4. Enhanced Database Design

### 4.1 Core Entities
```sql
-- User Management
Users;

-- Product Catalog
Item: ItemID, ItemCode, ItemName, BrandID, CategoryID, Description, UnitOfMeasure, 
      PackSize, CostPrice, SellingPrice, ReOrderPoint, SupplierID, Status, 
      DateAdded, LastUpdated

Brand: BrandID, BrandName, Description, Country, Status
Category: CategoryID, ParentCategory, CategoryName, Level, DisplayOrder
Supplier: SupplierID, SupplierName, ContactInfo, Address, Status

-- Inventory Management
Batch: BatchID, ItemID, BatchNumber, QuantityReceived, QuantityAvailable, 
       ManufactureDate, ExpiryDate, CostPrice, Status

Stock: StockID, ItemID, LocationType, CurrentStock, ReservedStock, LocationID
StockMovement: MovementID, ItemID, FromLocation, ToLocation, Quantity, 
               MovementType, Timestamp, UserId, BatchID

-- Transaction Processing
Transaction: TransactionID, UserID, TransactionType, Date, TotalAmount, 
            Discount, PaymentMethod, SynexPointsAwarded, Status, ChangeAmount,
            IsEmployeePurchase, ProcessedByEmployeeID

TransactionItem: TransactionID, ItemID, Quantity, UnitPrice, TotalPrice, 
                BatchID, DiscountApplied

-- Employee Purchase Audit (Simple)
EmployeePurchaseLog: LogID, EmployeeID, TransactionID, PurchaseTimestamp, TotalAmount, PaymentMethod

-- Promotions & Loyalty
Promotion: PromoID, PromoName, DiscountType, DiscountValue, StartDate, EndDate, Status
PromotionItem: PromoID, ItemID, Conditions
LoyaltyTransaction: TransactionID, PointsEarned, PointsRedeemed, Balance
```

### 4.2 Advanced Features
- **Audit Trail**: All data changes tracked with timestamp and user
- **Soft Deletes**: Maintain data integrity with status-based deletion
- **Indexing Strategy**: Optimized queries for reporting and search
- **Data Validation**: Database-level constraints and application-level validation

## 5. Technical Architecture Enhancements

### 5.1 Clean Architecture Implementation
```
Domain Layer (Core Business Logic)
├── Entities: User, Product, Transaction, Batch, Stock
├── Value Objects: Money, ItemCode, Quantity, SynexPoints
├── Aggregates: InventoryAggregate, BillingAggregate, UserAggregate
├── Domain Events: StockDepletedEvent, BillGeneratedEvent
└── Specifications: Business rule validation

Application Layer (Use Cases)
├── Authentication: Login, Register, Logout
├── Billing: ProcessPOS, ProcessWeb, GenerateBill
├── Inventory: StockManagement, Reorder, Transfer
├── Reporting: Sales, Stock, Analytics, Insights
└── User Management: Customer, Employee, Admin operations

Interface Adapters (Controllers & Gateways)
├── CLI Controllers: Role-based menu systems
├── Presenters: Console output formatting
├── Repositories: Data persistence abstractions
└── External Services: PDF generation, notifications

Infrastructure (Technical Implementation)
├── Database: PostgreSQL with Hibernate
├── Security: BCrypt, session management
├── Logging: SLF4J with structured logging
└── PDF Generation: Apache PDFBox
```

### 5.2 Design Patterns (8-11 Required)
1. **Observer Pattern**: Event-driven notifications (stock alerts, reorders)
2. **Strategy Pattern**: Payment processing, stock selection (FIFO/Expiry), POS mode handling
3. **Factory Pattern**: User, transaction, report object creation
4. **Command Pattern**: CLI command processing and undo operations
5. **State Pattern**: Application state management per user role, POS mode switching
6. **Singleton Pattern**: Session management, database connections
7. **Builder Pattern**: Complex PDF report construction
8. **Template Method Pattern**: Report generation templates
9. **Proxy Pattern**: Caching layer for frequently accessed data
10. **Specification Pattern**: Business rule validation, employee purchase restrictions
11. **Repository Pattern**: Data access abstraction

## 6. Quality Assurance & Testing

### 6.1 Clean Testing Strategy
- **Unit Tests**: Domain logic testing with 80%+ coverage
- **Integration Tests**: Database and external service integration
- **End-to-End Tests**: Complete user workflow validation
- **Test Data Builders**: Consistent test data creation
- **Mock Objects**: External dependency isolation

### 6.2 Code Quality Standards
- **SOLID Principles**: Single responsibility, open/closed, dependency inversion
- **Clean Code**: Meaningful names, small methods, clear structure
- **Documentation**: Comprehensive JavaDoc and architectural documentation
- **Code Coverage**: JaCoCo integration with 80% minimum threshold

## 7. Assumptions & Clarifications

### 7.1 Business Assumptions
- **Operating Hours**: 24/7 system availability with audit logging
- **Currency**: All monetary values in LKR (Sri Lankan Rupees)
- **Tax**: No tax calculations required (can be added as enhancement)
- **Partial Payments**: Not supported (full payment required)
- **Cash Management**: Employees responsible for cash drawer accuracy
- **Employee Purchases**: Single transaction limit of LKR 10,000 for personal purchases
- **Personal Purchase Items**: Gift cards restricted from employee purchase

### 7.2 Technical Assumptions
- **Database**: PostgreSQL running on localhost with provided credentials
- **Concurrency**: Single-user CLI application (no concurrent access)
- **File Storage**: PDFs stored in local filesystem with organized structure
- **Session Management**: In-memory session storage for CLI application
- **Error Handling**: Comprehensive exception hierarchy with user-friendly messages

### 7.3 Implementation Priorities
1. **Phase 1**: Core POS functionality with basic reporting
2. **Phase 2**: Web transaction simulation and enhanced inventory
3. **Phase 3**: Advanced analytics and comprehensive testing
4. **Phase 4**: Performance optimization and documentation

## 8. Success Criteria & Validation

### 8.1 Functional Validation
- All user roles can perform designated functions
- **Employee personal purchase mode** functions correctly with proper restrictions
- Transaction processing accurate with proper change calculation
- Stock management follows FIFO with expiry override
- Reports generate correctly with accurate data
- PDF bills and reports created successfully
- **POS mode switching** works seamlessly between work and personal purchase modes

### 8.2 Technical Validation
- Clean architecture principles demonstrated
- 8-11 design patterns contextually applied
- SOLID principles implementation
- 80%+ test coverage achieved
- Exception handling without raw try-catch in business logic

### 8.3 Quality Metrics
- Code maintainability and extensibility
- Performance benchmarks for common operations
- Error handling and recovery procedures
- Documentation completeness and accuracy
- Demonstration readiness and confidence

## 9. Future Enhancement Opportunities

### 9.1 Technical Enhancements
- **Multi-threading**: Concurrent transaction processing
- **REST API**: Web service endpoints for future integrations
- **Real-time Analytics**: Dashboard with live metrics

### 9.2 Business Enhancements
- **Advanced Promotions**: Complex discount rules and combinations
- **Supplier Integration**: Automated reordering and purchase orders
- **Customer Analytics**: Advanced behavior analysis and recommendations
- **Multi-location Support**: Chain store management capabilities

This comprehensive scenario addresses all rubric requirements while maintaining clean architecture principles and providing clear implementation guidance for achieving the 70-100% grade band across all assessment criteria.





==================================================================================================================
BELOW IS HOW THE RUBIC EXPLAINED THE SCENARIO : ABOVE IT'S ENHANCED WITH USERS REQUIREMENT. BELOW IS NEGLETABLE

Synex Outlet Store (SYOS) is an up-and-coming grocery store in Colombo. SYOS is a large store where customers can choose their household needs. After selecting items, customers check out the items via the single point of sales managed by a SYOS employee. At the point of sale, the employee writes down the items to be purchased in their quantities and calculates the amount owed by the customer to SYOS. The employee prepares and issues the final bill with the necessary change upon payment.
The management of SYOS has observed that during rush hour, the customers spend a lot of time queuing at the point of sale, resulting in store congestion and customer dissatisfaction. Also, the manual methods are prone to calculation errors, are time-consuming, and are tedious. To alleviate the problem, the management has decided to automate the billing system at SYOS and integrate it with the stock system to manage stocks and reorder levels efficiently.
Following are the functional requirements of the system that need to be developed.
1. The items to be purchased by the customer must be entered into the system via a code. The codes can be defined in suitable forms if they uniquely identify the item. Once the thing has been keyed into the system, the quantity is inserted, and the following item is keyed. Upon completing the insertion of the items to be purchased into the system, the system shall calculate the dues and display them to the employee.
   a. SYOS only accepts cash payments. Hence, when cash is tendered, the amount so tendered is inserted into the system, and the change to be returned is calculated and displayed.
   b. The bill is created with the item name (not code), the quantity, the total price for the item, full price, discount, cash tendered, and the change amount. The bill should have the serial number (running number starting from 1) and the bill date. Bill should be saved separately with the bill serial number for each customer transaction performed.
2. Upon the customer checking out (the bill has been generated and saved), the number of items on shelves should be reduced by the amount the customer has purchased.
   a. The items are first bought at the SYOS store and are stocked according to the code, date of purchase, amount of quantity received, and date of expiry of items.
   b. Items are moved to the shelf from the store. The stock should be reduced from the oldest batch of items and put on the shelf. However, when the expiry date of another set is closer than the one in the oldest batch of items, the newer batch is chosen to stock the SYOS shelves.
3. Apart from the over-the-counter sales, SYOS provides an interface where sales are made through the Internet. The user needs to be registered with the system first and, upon completing the registration process, can purchase items from SYOS's website.
   a. The website inventory must also be maintained separately from the store's shelf.
   b. The transactions that have happened online and over the counter should be identified separately.
4. The following reports are needed to be displayed on the screen. The reports should be generated combinedly and severally for each transaction type and store type.
   a. The total sale for a given day. This report should display the items sold (with name and code), the total quantity, and the total revenue for the given day.
   b. The total number of items with code, name, and quantity must be reshelved at the end of the day.
   c. Reorder levels of stock. If any stock for a given item falls below 50 items, that item should appear on the report.
   d. Stock report. Provides details of the current stock batch-wise with the same information as mentioned in 2(a) above.
   e. Bill report. This would contain all the customer transactions that have taken place in the SYOS system.