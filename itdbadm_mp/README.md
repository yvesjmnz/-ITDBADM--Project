# Neo's Burritos - Enhanced Online Store

## Overview

This is an enhanced Java AWT application for Neo's Burritos online store, featuring complete ordering and payment functionality with an improved user interface. The application demonstrates advanced database operations using stored procedures and triggers.

## New Features Implemented

### 🛒 Complete Shopping Cart System
- **Add to Cart**: Add products with customizations and quantities
- **Cart Management**: Update quantities, remove items, clear cart
- **Real-time Updates**: Cart count and total displayed in header
- **Persistent Storage**: Cart items saved to database

### 💳 Full Payment Processing
- **Multiple Payment Methods**: 
  - Credit Card
  - Debit Card
  - PayPal
  - Cash on Delivery
- **Payment Validation**: Method-specific validation rules
- **Transaction Simulation**: Realistic payment processing with success/failure scenarios
- **Transaction IDs**: Generated for successful payments

### 📦 Order Management System
- **Order Creation**: Convert cart to order with delivery details
- **Order History**: View past orders with detailed information
- **Order Status Tracking**: Track order progress through different states
- **Order Details**: Complete order information including items and customizations

### 🎨 Enhanced User Interface
- **Modern Design**: Improved color scheme and typography
- **Better Navigation**: Clear panel transitions and user flow
- **Responsive Layout**: Better use of screen space
- **User Feedback**: Informative dialogs and status messages
- **Multi-panel Architecture**: 
  - Login Panel
  - Store Panel (product browsing)
  - Cart Panel (cart management)
  - Checkout Panel (order placement)
  - Order History Panel
  - Admin Panel

### 🔧 Technical Improvements
- **SOLID Principles**: Clean separation of concerns
- **Service Layer**: PaymentService for payment processing
- **Enhanced DAOs**: CartDAO and OrderDAO for data operations
- **Error Handling**: Comprehensive error management
- **Input Validation**: Robust validation for all user inputs

## Architecture

### Model Classes
- **User**: User authentication and roles
- **Product**: Menu items with currency conversion
- **CartItem**: Shopping cart items with customizations
- **Order**: Order information and status
- **OrderItem**: Individual items within orders

### Data Access Objects (DAOs)
- **UserDAO**: User authentication and management
- **ProductDAO**: Product operations and currency conversion
- **CartDAO**: Shopping cart persistence and operations
- **OrderDAO**: Order creation and management

### Services
- **PaymentService**: Payment processing simulation with multiple methods

### Database Integration
- **Stored Procedures**: All database operations use stored procedures
- **Currency Conversion**: Real-time currency conversion for international customers
- **Transaction Management**: Proper transaction handling for order creation

## Usage Instructions

### Running the Application

1. **Using Maven**:
   ```bash
   mvn clean compile exec:java
   ```

2. **Using Batch File** (Windows):
   ```bash
   run.bat
   ```

### Demo Accounts
- **Customer**: alice@email.com / customer123
- **Admin**: admin@neosburritos.com / admin123

### Shopping Flow

1. **Login**: Use demo credentials to access the store
2. **Browse Products**: 
   - Select currency (USD, PHP, KRW)
   - Filter by category (Burrito, Bowl, Drink, Side)
   - View product details
3. **Add to Cart**:
   - Select product
   - Enter quantity
   - Add customizations (optional)
   - Click "Add to Cart"
4. **Manage Cart**:
   - Click "View Cart" to see cart items
   - Update quantities or remove items
   - View running total
5. **Checkout**:
   - Click "Proceed to Checkout"
   - Enter delivery address
   - Add order notes (optional)
   - Select payment method
   - Enter payment details
   - Place order
6. **Order History**:
   - View past orders
   - Check order status
   - See order details

### Admin Features
- Order management interface
- Product management capabilities
- User administration tools

## Key Design Decisions

### 1. **Single Responsibility Principle**
- Each DAO handles one entity type
- PaymentService focuses solely on payment processing
- UI panels have specific purposes

### 2. **Open/Closed Principle**
- Payment methods easily extensible
- Order status system can be extended
- New UI panels can be added without modifying existing code

### 3. **Interface Segregation**
- Specific methods for different operations
- No unnecessary dependencies between components

### 4. **Dependency Inversion**
- Services depend on abstractions
- Database operations abstracted through DAOs

### 5. **Error Handling Strategy**
- Graceful degradation on database errors
- User-friendly error messages
- Comprehensive logging for debugging

## Database Requirements

The application requires the following stored procedures to be implemented in your MySQL database:

### Cart Operations
- `AddToCart(user_id, product_id, quantity, customizations)`
- `GetCartItems(user_id, currency_code)`
- `UpdateCartItemQuantity(cart_id, quantity)`
- `RemoveFromCart(cart_id)`
- `ClearCart(user_id)`
- `GetCartTotal(user_id, currency_code)`
- `GetCartItemCount(user_id)`

### Order Operations
- `CreateOrderFromCart(user_id, currency_code, delivery_address, notes, OUT order_id)`
- `GetOrderById(order_id)`
- `GetOrderItems(order_id)`
- `GetUserOrders(user_id)`
- `UpdateOrderStatus(order_id, status)`
- `GetAllOrders()`

## Future Enhancements

- **Real Payment Integration**: Connect to actual payment gateways
- **Email Notifications**: Order confirmation and status updates
- **Inventory Management**: Real-time stock tracking
- **Promotions System**: Discount codes and special offers
- **Mobile Responsive**: Web-based interface for mobile devices
- **Analytics Dashboard**: Sales reporting and customer insights

## Technologies Used

- **Java 17**: Core application development
- **AWT**: User interface framework
- **MySQL**: Database management
- **Maven**: Build and dependency management
- **JDBC**: Database connectivity

## Development Notes

This enhanced version maintains backward compatibility while adding significant new functionality. The codebase follows clean architecture principles and is designed for maintainability and extensibility.

The application demonstrates practical implementation of:
- Database stored procedures
- Transaction management
- Currency conversion
- Payment processing simulation
- Order lifecycle management
- User session management

All new features are thoroughly integrated with the existing authentication and product management systems.