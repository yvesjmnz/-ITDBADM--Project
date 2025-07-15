# Neo's Burritos - Online Ordering System

A Java-based burrito ordering system with multi-currency support and admin management.

## Prerequisites

Before you start, make sure you have these installed:

- **Java 17 or higher** - [Download here](https://adoptium.net/)
- **Maven 3.6+** - [Download here](https://maven.apache.org/download.cgi)
- **MySQL 8.0+** - [Download here](https://dev.mysql.com/downloads/mysql/)
- **MySQL Workbench** - [Download here](https://dev.mysql.com/downloads/workbench/)
- **VS Code** - [Download here](https://code.visualstudio.com/)

### VS Code Extensions (Recommended)

Install these extensions in VS Code:
- Extension Pack for Java (Microsoft)

## Database Setup

### 1. Start MySQL Server

Make sure your MySQL server is running on your local machine.

### 2. Create Database


Manually run these files in order in Workbench:
1. `database_Files/schema_simplified.sql`
2. `database_Files/triggers_simplified.sql`
3. 'database_Files/stored_procedures.sql'
4. `database_Files/sample_data_enhanced.sql`


## Project Setup

### 1. Clone/Download Project

Download the project to your local machine.

### 2. Open in VS Code

```bash
cd path/to/your/project
code .
```

### 3. Configure Database Connection

Edit the database connection settings in:
`src/main/java/com/neosburritos/util/DatabaseConnectionManager.java`

Update these values for your local MySQL:
```java
private static final String URL = "jdbc:mysql://localhost:3306/neos_burritos";
private static final String USERNAME = "your_mysql_username";
private static final String PASSWORD = "your_mysql_password";
```

### 4. Run the Application

Press the run button on NeosAppSwing.java

## Testing the Application

### Login Credentials

Use these test accounts:

**Admin:**
- Email: `admin@neosburritos.com`
- Password: `admin123`

**Customer:**
- Email: `john@email.com`
- Password: `customer123`

**Staff:**
- Email: `staff@neosburritos.com`
- Password: `staff123`

### What to Test

1. **Login** with different user roles
2. **Browse products** in different currencies (USD, PHP, KRW)
3. **Add items to cart** with customizations
4. **Place an order** and complete checkout
5. **View order history** as a customer
6. **Manage orders** as an admin

## Project Structure

```
itdbadm_mp/
├── src/main/java/com/neosburritos/
│   ├── dao/           # Database access objects
│   ├── model/         # Data models
│   ├── service/       # Business logic
│   ├── ui/swing/      # User interface
│   └── util/          # Utilities
├── database_Files/    # SQL scripts
└── pom.xml           # Maven configuration
```

## Troubleshooting

### Database Connection Issues

1. **Check MySQL is running**: Look for MySQL in your system services
2. **Verify credentials**: Make sure username/password are correct
3. **Check port**: Default is 3306, make sure it's not blocked
4. **Test connection**: Use MySQL Workbench to connect first

### Application Won't Start

1. **Check Java version**: Run `java -version` (should be 17+)
2. **Check Maven**: Run `mvn -version`
3. **Clean and rebuild**: Run `mvn clean compile`
4. **Check logs**: Look for error messages in the terminal

### UI Issues

1. **Try different look and feel**: The app uses system look and feel
2. **Check screen resolution**: Some UI elements may not fit on small screens
3. **Restart application**: Close and run again

## Features

- **Multi-currency support** (USD, PHP, KRW)
- **User roles** (Admin, Staff, Customer)
- **Product customization** (build your own burrito)
- **Order management** (place, track, update orders)
- **Admin panel** (manage orders and view statistics)
- **Shopping cart** (add, remove, modify items)

## Development Notes

- Built with **Java Swing** for the UI
- Uses **MySQL** for data storage
- **Maven** for dependency management
- **MVC architecture** for clean code organization

## Need Help?

If you run into issues:

1. Check the troubleshooting section above
2. Make sure all prerequisites are installed correctly
3. Verify the database is set up properly
4. Check that MySQL is running and accessible

---