#Enterprise Business Layer (Domain)
src/main/java/com/syos/domain/
├── entities/
│   ├── User.java
│   ├── ItemMasterFile.java
│   ├── Bill.java
│   ├── Transaction.java
│   ├── TransactionItem.java
│   ├── Batch.java
│   ├── Stock.java (Abstract)
│   ├── WarehouseStock.java
│   ├── ShelfStock.java
│   ├── WebInventory.java
│   ├── StockTransfer.java
│   ├── Shelf.java
│   ├── ShelfLocation.java
│   ├── Category.java
│   ├── Brand.java
│   ├── Supplier.java
│   ├── Cart.java
│   ├── CartItem.java
│   ├── Promotion.java
│   ├── Notification.java
│   └── LoyaltyAccount.java
│
├── valueobjects/
│   ├── Money.java
│   ├── ItemCode.java
│   ├── Quantity.java
│   ├── DateRange.java
│   ├── Percentage.java
│   ├── SynexPoints.java
│   ├── BillNumber.java
│   ├── BatchNumber.java
│   ├── ExpiryDate.java
│   ├── ShelfCode.java
│   ├── Location.java
│   ├── Capacity.java
│   ├── TransferCode.java
│   ├── WarehouseStockCode.java
│   ├── QualityStatus.java
│   ├── Address.java
│   ├── Email.java
│   └── PhoneNumber.java
│
├── aggregates/
│   ├── InventoryAggregate.java
│   ├── WarehouseAggregate.java
│   ├── ShelfAggregate.java
│   ├── BillingAggregate.java
│   ├── UserAggregate.java
│   └── TransferAggregate.java
│
├── events/ [Observer Pattern - Pattern #1]
│   ├── DomainEvent.java (Interface)
│   ├── StockDepletedEvent.java
│   ├── ProductExpiredEvent.java
│   ├── BillGeneratedEvent.java
│   ├── LoyaltyPointsEarnedEvent.java
│   ├── ReorderRequiredEvent.java
│   ├── ShelfRestockRequiredEvent.java
│   ├── StockTransferInitiatedEvent.java
│   ├── StockTransferCompletedEvent.java
│   ├── WarehouseStockReceivedEvent.java
│   └── EventPublisher.java (Interface)
│
├── specifications/ [Specification Pattern - Pattern #2]
│   ├── Specification.java (Interface)
│   ├── AndSpecification.java
│   ├── OrSpecification.java
│   ├── NotSpecification.java
│   ├── ProductActiveSpecification.java
│   ├── BatchExpiredSpecification.java
│   ├── StockBelowThresholdSpecification.java
│   ├── ShelfCapacitySpecification.java
│   ├── UserAuthenticatedSpecification.java
│   ├── TransferApprovedSpecification.java
│   └── WarehouseStockAvailableSpecification.java
│
├── services/ (Domain Services)
│   ├── InventoryDomainService.java
│   ├── PricingDomainService.java
│   └── StockTransferDomainService.java
│
└── exceptions/
├── DomainException.java (Base)
├── InsufficientStockException.java
├── InvalidQuantityException.java
├── ExpiredProductException.java
├── ShelfCapacityExceededException.java
├── BusinessRuleViolationException.java
├── TransferNotAllowedException.java
└── WarehouseCapacityExceededException.java

#Application Business Layer (Use Case)
src/main/java/com/syos/application/
├── usecases/
│   ├── auth/
│   │   ├── LoginUseCase.java
│   │   ├── RegisterCustomerUseCase.java
│   │   ├── CreateEmployeeUseCase.java
│   │   ├── CreateAdminUseCase.java
│   │   ├── LogoutUseCase.java
│   │   └── ValidateSessionUseCase.java
│   │
│   ├── billing/
│   │   ├── ProcessPOSTransactionUseCase.java
│   │   ├── ProcessWebTransactionUseCase.java
│   │   ├── CalculateChangeUseCase.java
│   │   ├── GenerateBillUseCase.java
│   │   ├── ProcessReturnUseCase.java
│   │   ├── VoidTransactionUseCase.java
│   │   └── ApplyDiscountUseCase.java
│   │
│   ├── inventory/
│   │   ├── AddProductUseCase.java
│   │   ├── ReceiveWarehouseStockUseCase.java
│   │   ├── InitiateStockTransferUseCase.java
│   │   ├── ApproveStockTransferUseCase.java
│   │   ├── CompleteStockTransferUseCase.java
│   │   ├── GetFIFOStockUseCase.java
│   │   ├── RestockShelfUseCase.java
│   │   ├── TransferToWebInventoryUseCase.java
│   │   ├── CheckReorderLevelsUseCase.java
│   │   ├── ViewShelfStockUseCase.java
│   │   ├── ViewShelfBrandsUseCase.java
│   │   ├── SearchProductByShelfUseCase.java
│   │   ├── GetShelfInventoryDetailsUseCase.java
│   │   └── CheckExpiryDatesUseCase.java
│   │
│   ├── browsing/
│   │   ├── BrowseProductsUseCase.java
│   │   ├── ViewByCategoryUseCase.java
│   │   ├── ViewFeaturedProductsUseCase.java
│   │   ├── ViewLatestProductsUseCase.java
│   │   ├── SearchProductsUseCase.java
│   │   ├── AddToCartUseCase.java
│   │   ├── RemoveFromCartUseCase.java
│   │   ├── UpdateCartQuantityUseCase.java
│   │   ├── ViewCartUseCase.java
│   │   └── CheckoutCartUseCase.java
│   │
│   ├── reporting/
│   │   ├── GenerateDailySalesReportUseCase.java
│   │   ├── GenerateStockReportUseCase.java
│   │   ├── GenerateReorderReportUseCase.java
│   │   ├── GenerateBillReportUseCase.java
│   │   ├── GenerateReshelvingReportUseCase.java
│   │   ├── GenerateShelfAnalyticsUseCase.java
│   │   ├── GenerateRetailInsightsUseCase.java
│   │   ├── GenerateInventoryTurnoverReportUseCase.java
│   │   ├── GeneratePeakHoursReportUseCase.java
│   │   ├── GenerateCategoryPerformanceReportUseCase.java
│   │   └── GenerateTransferHistoryReportUseCase.java
│   │
│   ├── loyalty/
│   │   ├── CalculateLoyaltyPointsUseCase.java
│   │   ├── ViewLoyaltyBalanceUseCase.java
│   │   ├── RedeemPointsUseCase.java
│   │   └── TransferPointsUseCase.java
│   │
│   └── notifications/
│       ├── ViewNotificationsUseCase.java
│       ├── MarkNotificationReadUseCase.java
│       ├── DismissNotificationUseCase.java
│       └── CreateReorderNotificationUseCase.java
│
├── ports/
│   ├── in/ (Use Case Interfaces)
│   │   ├── AuthenticationPort.java
│   │   ├── BillingPort.java
│   │   ├── InventoryPort.java
│   │   ├── WarehouseManagementPort.java
│   │   ├── ShelfManagementPort.java
│   │   ├── ReportingPort.java
│   │   ├── LoyaltyPort.java
│   │   └── NotificationPort.java
│   │
│   └── out/ (Repository Interfaces)
│       ├── UserRepository.java
│       ├── ItemMasterFileRepository.java
│       ├── BillRepository.java
│       ├── TransactionRepository.java
│       ├── StockRepository.java
│       ├── WarehouseStockRepository.java
│       ├── StockTransferRepository.java
│       ├── ShelfRepository.java
│       ├── ShelfStockRepository.java
│       ├── BatchRepository.java
│       ├── CategoryRepository.java
│       ├── BrandRepository.java
│       ├── SupplierRepository.java
│       ├── CartRepository.java
│       ├── NotificationRepository.java
│       ├── PromotionRepository.java
│       └── EventStore.java
│
├── dto/
│   ├── requests/
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   ├── BillRequest.java
│   │   ├── ProductRequest.java
│   │   ├── ReportRequest.java
│   │   ├── ShelfStockRequest.java
│   │   ├── ShelfBrandsRequest.java
│   │   ├── ShelfSearchRequest.java
│   │   ├── WarehouseStockRequest.java
│   │   ├── StockTransferRequest.java
│   │   ├── CartRequest.java
│   │   └── NotificationRequest.java
│   │
│   └── responses/
│       ├── AuthResponse.java
│       ├── BillResponse.java
│       ├── ProductResponse.java
│       ├── ReportResponse.java
│       ├── LoyaltyResponse.java
│       ├── ShelfStockResponse.java
│       ├── ShelfBrandsResponse.java
│       ├── ShelfAnalyticsResponse.java
│       ├── WarehouseStockResponse.java
│       ├── StockTransferResponse.java
│       ├── CartResponse.java
│       └── NotificationResponse.java
│
├── services/ (Application Services)
│   ├── StockAllocationService.java
│   ├── FIFOStockSelectionService.java
│   ├── StockTransferService.java
│   ├── PricingService.java
│   ├── DiscountCalculationService.java
│   ├── NotificationService.java
│   ├── ShelfManagementService.java
│   ├── ShelfOptimizationService.java
│   ├── BrandAnalysisService.java
│   └── InventoryValidationService.java
│
├── strategies/ [Strategy Pattern - Pattern #3]
│   ├── payment/
│   │   ├── PaymentStrategy.java (Interface)
│   │   ├── CashPaymentStrategy.java
│   │   └── CardPaymentStrategy.java
│   │
│   ├── stock/
│   │   ├── StockSelectionStrategy.java (Interface)
│   │   ├── FIFOWithExpiryStrategy.java
│   │   ├── LIFOStrategy.java
│   │   └── RandomSelectionStrategy.java
│   │
│   └── discount/
│       ├── DiscountStrategy.java (Interface)
│       ├── NoDiscountStrategy.java
│       ├── LoyaltyDiscountStrategy.java
│       ├── PromotionalDiscountStrategy.java
│       └── BulkDiscountStrategy.java
│
└── factories/ [Factory Pattern - Pattern #4]
├── UserFactory.java
├── BillFactory.java
├── ReportFactory.java
├── ShelfFactory.java
├── TransactionFactory.java
├── StockTransferFactory.java
└── NotificationFactory.java

#Interface Adapters
src/main/java/com/syos/adapter/
├── in/ (Controllers/Presenters)
│   ├── cli/
│   │   ├── controllers/
│   │   │   ├── MainController.java
│   │   │   ├── AuthenticationController.java
│   │   │   ├── CustomerController.java
│   │   │   ├── EmployeeController.java
│   │   │   ├── AdminController.java
│   │   │   ├── GuestController.java
│   │   │   ├── ShelfController.java
│   │   │   ├── WarehouseController.java
│   │   │   └── ReportController.java
│   │   │
│   │   ├── presenters/
│   │   │   ├── Presenter.java (Interface)
│   │   │   ├── MenuPresenter.java
│   │   │   ├── ProductPresenter.java
│   │   │   ├── BillPresenter.java
│   │   │   ├── ReportPresenter.java
│   │   │   ├── ErrorPresenter.java
│   │   │   ├── ShelfPresenter.java
│   │   │   ├── BrandPresenter.java
│   │   │   ├── WarehousePresenter.java
│   │   │   ├── TransferPresenter.java
│   │   │   └── NotificationPresenter.java
│   │   │
│   │   ├── commands/ [Command Pattern - Pattern #5]
│   │   │   ├── Command.java (Interface)
│   │   │   ├── LoginCommand.java
│   │   │   ├── LogoutCommand.java
│   │   │   ├── BrowseCommand.java
│   │   │   ├── AddToCartCommand.java
│   │   │   ├── CheckoutCommand.java
│   │   │   ├── ViewShelfStockCommand.java
│   │   │   ├── ViewShelfBrandsCommand.java
│   │   │   ├── GenerateReportCommand.java
│   │   │   ├── ReceiveStockCommand.java
│   │   │   ├── TransferStockCommand.java
│   │   │   ├── ProcessReturnCommand.java
│   │   │   ├── ViewNotificationsCommand.java
│   │   │   └── CommandInvoker.java
│   │   │
│   │   ├── state/ [State Pattern - Pattern #6]
│   │   │   ├── ApplicationState.java (Interface)
│   │   │   ├── WelcomeState.java
│   │   │   ├── GuestState.java
│   │   │   ├── BrowsingState.java
│   │   │   ├── CartState.java
│   │   │   ├── AuthenticatedCustomerState.java
│   │   │   ├── AuthenticatedEmployeeState.java
│   │   │   ├── AuthenticatedAdminState.java
│   │   │   ├── POSTransactionState.java
│   │   │   ├── ReportGenerationState.java
│   │   │   └── StateContext.java
│   │   │
│   │   ├── views/
│   │   │   ├── View.java (Interface)
│   │   │   ├── WelcomeView.java
│   │   │   ├── LoginView.java
│   │   │   ├── RegisterView.java
│   │   │   ├── ProductBrowserView.java
│   │   │   ├── CategoryView.java
│   │   │   ├── CartView.java
│   │   │   ├── BillingView.java
│   │   │   ├── PaymentView.java
│   │   │   ├── ReportView.java
│   │   │   ├── NotificationView.java
│   │   │   ├── ShelfManagementView.java
│   │   │   ├── BrandManagementView.java
│   │   │   ├── WarehouseManagementView.java
│   │   │   ├── TransferManagementView.java
│   │   │   ├── UserManagementView.java
│   │   │   └── OrderHistoryView.java
│   │   │
│   │   ├── session/
│   │   │   ├── SessionManager.java [Singleton - Pattern #7]
│   │   │   ├── UserSession.java
│   │   │   ├── SessionContext.java
│   │   │   └── SessionValidator.java
│   │   │
│   │   └── navigation/
│   │       ├── NavigationManager.java
│   │       ├── MenuBuilder.java
│   │       └── BreadcrumbTracker.java
│   │
│   └── web/ (Future REST implementation)
│       ├── RestController.java (Placeholder)
│       └── WebSocketHandler.java (Placeholder)
│
├── out/ (Gateways/Repositories)
│   ├── persistence/
│   │   ├── JpaUserRepository.java
│   │   ├── JpaItemMasterFileRepository.java
│   │   ├── JpaBillRepository.java
│   │   ├── JpaTransactionRepository.java
│   │   ├── JpaStockRepository.java
│   │   ├── JpaWarehouseStockRepository.java
│   │   ├── JpaStockTransferRepository.java
│   │   ├── JpaShelfRepository.java
│   │   ├── JpaShelfStockRepository.java
│   │   ├── JpaBatchRepository.java
│   │   ├── JpaCategoryRepository.java
│   │   ├── JpaBrandRepository.java
│   │   ├── JpaSupplierRepository.java
│   │   ├── JpaCartRepository.java
│   │   ├── JpaNotificationRepository.java
│   │   └── JpaPromotionRepository.java
│   │
│   ├── notification/
│   │   ├── NotificationGateway.java
│   │   ├── ConsoleNotificationAdapter.java
│   │   ├── EmailNotificationAdapter.java (Future)
│   │   └── SMSNotificationAdapter.java (Future)
│   │
│   ├── pdf/
│   │   ├── PDFGateway.java
│   │   ├── builders/ [Builder Pattern - Pattern #8]
│   │   │   ├── PDFBuilder.java (Interface)
│   │   │   ├── BillPDFBuilder.java
│   │   │   ├── ReportPDFBuilder.java
│   │   │   └── InvoicePDFBuilder.java
│   │   │
│   │   └── templates/ [Template Method - Pattern #9]
│   │       ├── PDFTemplate.java (Abstract)
│   │       ├── BillPDFTemplate.java
│   │       ├── ReportPDFTemplate.java
│   │       └── InventoryPDFTemplate.java
│   │
│   └── payment/
│       ├── PaymentGateway.java
│       ├── SimulatedCardProcessor.java
│       └── CashDrawerManager.java
│
├── mappers/ (Data Transfer)
│   ├── EntityMapper.java (Interface)
│   ├── UserMapper.java
│   ├── ItemMasterFileMapper.java
│   ├── BillMapper.java
│   ├── TransactionMapper.java
│   ├── ShelfMapper.java
│   ├── ShelfStockMapper.java
│   ├── BrandMapper.java
│   ├── WarehouseStockMapper.java
│   ├── StockTransferMapper.java
│   └── NotificationMapper.java
│
└── validators/
├── InputValidator.java
├── CardNumberValidator.java
├── DateValidator.java
├── EmailValidator.java
├── PhoneValidator.java
├── ItemCodeValidator.java
└── QuantityValidator.java

#Infrastructure Layers
src/main/java/com/syos/infrastructure/
├── persistence/
│   ├── entities/ (JPA Entities)
│   │   ├── UserEntity.java
│   │   ├── ItemMasterFileEntity.java
│   │   ├── BillEntity.java
│   │   ├── TransactionEntity.java
│   │   ├── TransactionItemEntity.java
│   │   ├── StockEntity.java
│   │   ├── WarehouseStockEntity.java
│   │   ├── StockTransferEntity.java
│   │   ├── ShelfEntity.java
│   │   ├── ShelfStockEntity.java
│   │   ├── BatchEntity.java
│   │   ├── CategoryEntity.java
│   │   ├── BrandEntity.java
│   │   ├── SupplierEntity.java
│   │   ├── BrandSupplierEntity.java
│   │   ├── LocationEntity.java
│   │   ├── CartEntity.java
│   │   ├── CartItemEntity.java
│   │   ├── PromotionEntity.java
│   │   ├── PromotionItemEntity.java
│   │   ├── ReturnEntity.java
│   │   ├── ReturnItemEntity.java
│   │   ├── NotificationEntity.java
│   │   ├── AuditLogEntity.java
│   │   ├── SystemSettingEntity.java
│   │   └── LoyaltyTransactionEntity.java
│   │
│   ├── config/
│   │   ├── HibernateConfig.java
│   │   ├── DatabaseConnection.java
│   │   ├── ConnectionPool.java
│   │   └── FlywayConfig.java
│   │
│   └── migrations/
│       ├── V001__Create_Extensions_And_Types.sql
│       ├── V002__Create_Core_Tables.sql
│       ├── V003__Create_Item_Master_And_Locations.sql
│       ├── V004__Create_Promotions_And_Transactions.sql
│       ├── V005__Create_Returns_And_Movements.sql
│       ├── V006__Create_System_Tables.sql
│       ├── V007__Create_Indexes.sql
│       ├── V008__Create_Functions_And_Triggers.sql
│       ├── V009__Create_Views.sql
│       ├── V010__Seed_Data.sql
│       ├── V011__Drop_Legacy_Stock_Tables.sql
│       ├── V012__Create_Warehouse_Stock_Management.sql
│       └── V013__Create_Stock_Transfer_Views.sql
│
├── security/
│   ├── BCryptPasswordEncoder.java
│   ├── SecurityContext.java
│   ├── AuthenticationManager.java
│   ├── SessionManager.java
│   └── AccessControlManager.java
│
├── logging/
│   ├── LoggerFactory.java [Factory Pattern - Pattern #10]
│   ├── FileLogger.java
│   ├── ConsoleLogger.java
│   ├── AuditLogger.java
│   └── LoggerConfiguration.java
│
├── pdf/
│   ├── PDFBoxAdapter.java
│   ├── PDFGenerator.java
│   ├── PDFConfiguration.java
│   └── FontManager.java
│
├── cache/ [Proxy Pattern - Pattern #11]
│   ├── CacheProxy.java
│   ├── InMemoryCache.java
│   ├── EhCacheAdapter.java
│   └── CacheConfiguration.java
│
├── config/
│   ├── ApplicationConfig.java
│   ├── DatabaseConfig.java
│   ├── SystemConfig.java
│   └── EnvironmentConfig.java
│
└── Main.java (Application Entry Point)

#Shared Kernels (Cross Cutting Concerns)
src/main/java/com/syos/shared/
├── constants/
│   ├── ApplicationConstants.java
│   ├── BusinessConstants.java
│   ├── ShelfConstants.java
│   ├── WarehouseConstants.java
│   ├── ErrorMessages.java
│   └── ValidationMessages.java
│
├── enums/
│   ├── UserRole.java
│   ├── TransactionType.java
│   ├── TransactionStatus.java
│   ├── PaymentMethod.java
│   ├── ProductStatus.java
│   ├── UnitOfMeasure.java
│   ├── ShelfStatus.java
│   ├── ReportType.java
│   ├── NotificationType.java
│   ├── TransferType.java
│   ├── TransferStatus.java
│   └── QualityStatus.java
│
├── utils/
│   ├── DateUtils.java
│   ├── MoneyUtils.java
│   ├── StringUtils.java
│   ├── ShelfUtils.java
│   ├── ValidationUtils.java
│   ├── BarcodeUtils.java
│   ├── CalculationUtils.java
│   └── FormatUtils.java
│
└── exceptions/
├── ApplicationException.java
├── InfrastructureException.java
├── PresentationException.java
└── ValidationException.java