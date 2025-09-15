# SYOS (Synex Outlet Store) Console Application

This is a clean architecture implementation of the SYOS retail management system with complete user authentication, built following SOLID principles and design patterns.

## Project Structure

The project follows Clean Architecture principles with clear layer separation:

```
src/main/java/com/syos/
├── domain/              # Business logic and entities
│   ├── entities/        # Domain entities (User)
│   ├── valueobjects/    # Value objects (Username, Password, Email, etc.)
│   ├── events/          # Domain events
│   └── exceptions/      # Domain-specific exceptions
├── application/         # Use cases and application services
│   ├── usecases/        # Application use cases
│   ├── ports/           # Interfaces (in/out)
│   └── dto/             # Data transfer objects
├── adapter/             # Interface adapters
│   ├── in/cli/          # Console interface adapters
│   └── out/persistence/ # Repository implementations
├── infrastructure/     # Frameworks and drivers
│   ├── config/          # Database and app configuration
│   ├── persistence/     # JPA entities
│   └── security/        # Security implementations
└── shared/              # Shared utilities and enums
```

## Recent Improvements (Latest Update)

### ✅ Enhanced User Experience Flow
- **Seamless Registration**: Users are automatically logged in after successful registration
- **Enhanced User Profile**: Professional profile display with time-based greetings
- **Streamlined Navigation**: Simplified customer menu focused on core functions
- **Improved Visual Design**: Better formatting and professional appearance

### ✅ User Interface Enhancements
- **Time-Based Greetings**: Dynamic greetings based on time of day (Morning/Afternoon/Evening)
- **Professional Profile Display**: Formatted user information with member since date
- **Clean Menu Structure**: Removed redundant options, focused on navigation
- **Enhanced Login Flow**: Improved login header and user feedback

## Features Implemented

### ✅ Authentication System
- **User Registration**: Complete customer registration with validation
- **User Login**: Role-based authentication (Customer, Employee, Admin)
- **Password Security**: BCrypt hashing with salt
- **Input Validation**: Comprehensive validation for all user inputs

### ✅ Repository Implementations
- **JPA Repository**: PostgreSQL database persistence
- **In-Memory Repository**: For testing and development
- **Automatic Fallback**: Falls back to in-memory if database unavailable

### ✅ Clean Architecture
- **Domain Layer**: Pure business logic, no external dependencies
- **Application Layer**: Use cases and application services
- **Adapter Layer**: Interface adapters for CLI and persistence
- **Infrastructure Layer**: Framework-specific implementations

### ✅ Design Patterns (8+ implemented)
1. **Repository Pattern**: Data access abstraction
2. **Factory Pattern**: Object creation (MenuFactory, UserFactory)
3. **Command Pattern**: CLI command handling
4. **State Pattern**: Application state management
5. **Strategy Pattern**: Payment and stock selection strategies
6. **Singleton Pattern**: Session management
7. **Observer Pattern**: Domain events
8. **Specification Pattern**: Business rules validation

### ✅ Testing
- **Unit Tests**: Comprehensive test coverage for all layers
- **Integration Tests**: End-to-end authentication flow testing
- **Value Object Tests**: Complete validation testing
- **Repository Tests**: Both JPA and in-memory implementations

### ✅ Logging
- **SLF4J + Logback**: Structured logging configuration
- **File Logging**: Separate log files for different components
- **Authentication Logging**: Dedicated auth.log for security events
- **Log Rotation**: Automatic log rotation and cleanup

## Prerequisites

- Java 24
- PostgreSQL 13+ (optional - will fallback to in-memory)
- Maven 3.8+

## Database Setup (Optional)

If you want to use PostgreSQL persistence:

1. Install PostgreSQL
2. Create database:
```sql
CREATE DATABASE syosdb;
CREATE USER postgres WITH PASSWORD 'apiit-LV6';
GRANT ALL PRIVILEGES ON DATABASE syosdb TO postgres;
```

3. Update connection details in:
   - `src/main/resources/META-INF/persistence.xml`
   - `src/main/java/com/syos/infrastructure/config/DatabaseConfig.java`

## Running the Application

### 1. Compile the project
```bash
mvn clean compile
```

### 2. Run tests
```bash
mvn test
```

### 3. Run the application
```bash
mvn exec:java -Dexec.mainClass="com.syos.Main"
```

Or compile and run directly:
```bash
mvn clean compile exec:java -Dexec.mainClass="com.syos.Main"
```

## Default Users

The system comes with pre-configured test users:

| Username | Password    | Role     | Email                |
|----------|-------------|----------|----------------------|
| admin    | admin12345  | ADMIN    | admin@syos.com       |
| employee | employee123 | EMPLOYEE | employee@syos.com    |
| customer | customer123 | CUSTOMER | customer@example.com |

## Application Flow

1. **Welcome Screen**: Shows welcome banner and main menu
2. **Main Menu Options**:
   - Browse Products (guest access)
   - Login (displays user profile, redirects based on role)
   - Register (creates account + auto-login + user profile + customer dashboard)
   - Exit

3. **Enhanced Registration Flow**:
   - User provides registration details
   - Account created successfully
   - **Automatic login** (no manual login required)
   - **User profile display** with time-based greeting
   - **Direct navigation** to customer dashboard

4. **Enhanced Login Flow**:
   - Professional login interface
   - **User profile display** after successful login
   - Time-based personalized greeting
   - Role-based dashboard access

5. **Role-Based Access**:
   - **Customer**: Browse products, view cart, order history (streamlined 4-option menu)
   - **Employee**: POS operations, inventory management
   - **Admin**: All operations plus user management (debug info removed)

6. **User Profile Information** (displayed automatically after login/registration):
   - Time-based greeting (Good Morning/Afternoon/Evening)
   - Username and email
   - Current Synex Points balance
   - Member since date (formatted professionally)

## Testing

### Run all tests:
```bash
mvn test
```

### Run specific test categories:
```bash
# Unit tests only
mvn test -Dtest="*Test"

# Integration tests only
mvn test -Dtest="*IntegrationTest"

# Value object tests
mvn test -Dtest="com.syos.domain.valueobjects.*"
```

### Test Coverage:
```bash
mvn test jacoco:report
```
View coverage report at: `target/site/jacoco/index.html`

## Key Design Decisions

### 1. **Clean Architecture Compliance**
- **Dependency Rule**: Inner layers don't depend on outer layers
- **Interface Segregation**: Small, focused interfaces
- **Dependency Inversion**: Depend on abstractions, not concretions

### 2. **SOLID Principles**
- **S**ingle Responsibility: Each class has one reason to change
- **O**pen/Closed: Open for extension, closed for modification
- **L**iskov Substitution: Subtypes are substitutable for base types
- **I**nterface Segregation: Clients don't depend on unused interfaces
- **D**ependency Inversion: Depend on abstractions

### 3. **Design Pattern Mitigation**
See `DESIGN_PATTERNS_SOLID_MITIGATION.md` for detailed analysis of how design patterns and SOLID principles coexist.

### 4. **Security**
- **Password Hashing**: BCrypt with configurable strength
- **Input Validation**: Domain-level validation for all inputs
- **SQL Injection Prevention**: JPA parameterized queries
- **Authentication Logging**: All auth attempts logged

### 5. **Error Handling**
- **Domain Exceptions**: Specific exceptions for business rule violations
- **Application Exceptions**: Use case specific error handling
- **Infrastructure Exceptions**: Technical error handling
- **No Raw Try-Catch**: Business logic doesn't handle technical exceptions

## Logs

Application logs are written to:
- `logs/syos-application.log` - General application logs
- `logs/syos-auth.log` - Authentication specific logs

Log configuration: `src/main/resources/logback.xml`

## Extensibility

The clean architecture design makes the application highly extensible:

### Adding New Features:
1. **New Domain Entity**: Add to `domain/entities/`
2. **New Use Case**: Add to `application/usecases/`
3. **New Repository**: Implement interface in `adapter/out/persistence/`
4. **New CLI Command**: Add to `adapter/in/cli/commands/`

### Adding New Storage:
1. Implement `UserRepository` interface
2. Update `Main.java` to use new implementation
3. No changes needed in business logic

### Adding New Interface:
1. Create new adapter in `adapter/in/`
2. Reuse existing use cases
3. No changes needed in business logic

## Common Issues & Solutions

### 1. **Database Connection Failed**
- Application automatically falls back to in-memory storage
- Check PostgreSQL service is running
- Verify connection details in configuration

### 2. **Compilation Errors**
- Ensure Java 24 is being used
- Run `mvn clean compile` to fresh compile
- Check all dependencies are downloaded

### 3. **Test Failures**
- Ensure no conflicting processes using test resources
- Run tests individually to isolate issues
- Check log files for detailed error information

## Development Guidelines

### 1. **Adding New Tests**
- Follow existing test structure
- Use descriptive test names with `@DisplayName`
- Test both happy path and error cases
- Maintain test coverage above 80%

### 2. **Code Style**
- Follow Clean Code principles
- Use meaningful variable and method names
- Keep methods small and focused
- Document complex business logic

### 3. **Logging**
- Use appropriate log levels (DEBUG, INFO, WARN, ERROR)
- Include context in log messages
- Don't log sensitive information (passwords, personal data)
- Use structured logging for important events

## Future Enhancements

The current implementation provides a solid foundation for:
- **Product Management**: Inventory, categories, pricing
- **Transaction Processing**: POS, web orders, payments
- **Reporting**: Sales, inventory, analytics
- **Advanced Features**: Promotions, loyalty points, returns

Each enhancement can be added following the same clean architecture patterns established in the authentication system.

## Documentation

- `DESIGN_PATTERNS_SOLID_MITIGATION.md` - Design patterns analysis
- `src/main/resources/META-INF/persistence.xml` - JPA configuration
- `src/main/resources/logback.xml` - Logging configuration
- JavaDoc comments throughout codebase

## Contact

For questions or issues, please refer to the comprehensive test suite and documentation provided in the codebase.
