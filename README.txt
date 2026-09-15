HealthFirst Pharmacy Inventory Management System (PIMS)
=======================================================
PRO732 desktop application for HealthFirst Pharmacy.

The system replaces paper stock books with a multi-user Swing application.
Cashiers process sales at the till. Administrators manage medicines,
suppliers, user accounts, and business reports.


1. Default login credentials
----------------------------
Role          Username     Password
Administrator admin        admin123
Cashier       cashier      cash123
Administrator manager      manager123
Cashier       cashier2     cash123

Passwords are stored as SHA-256 hashes, not plain text.


2. How to run (bundled executable)
----------------------------------
The packaged application uses an embedded H2 database in MySQL
compatibility mode. You do not need to install MySQL to test the system.
A sample catalogue, users, and sales history are created on first launch.

Windows:
    Double-click healthfirst_pims.exe
    (Requires JDK 17+ installed. The .exe launches dist\healthfirst_pims.jar
    and the embedded database. For submission, rename the file to
    YourName_pims.exe as required by the brief.)

    Alternative: double-click healthfirst_pims.bat

Linux / macOS:
    ./healthfirst_pims.sh
    or: ./healthfirst_pims

From source (any OS with Java 17+):
    java -jar dist/healthfirst_pims.jar

Optional checks:
    java -jar dist/healthfirst_pims.jar --self-test



3. Using a MySQL server (optional)
----------------------------------
1. Install MySQL 8.
2. Run database.sql in MySQL Workbench or:
       mysql -u root -p < database.sql
3. Copy config.properties next to the executable (or into the working
   directory) and set:
       db.mode=mysql
       mysql.user=root
       mysql.password=YOUR_PASSWORD
4. Start the application again.


4. Building from source
-----------------------
Requirements: JDK 17 or newer.

    ./build.sh

This compiles every file under src/ and writes dist/healthfirst_pims.jar.


5. User guide
-------------
Sign in
    Failed logins stay on the sign-in screen with an error message.
    A successful Admin login opens the Administrator dashboard.
    A successful Cashier login opens the till.

Cashier — Point of Sale
    Search the catalogue, set a quantity, and click Add to cart.
    Use Remove or Clear cart to change the current sale.
    Checkout deducts stock, stores the sale, and opens a bill window.
    The bill can be saved as a text file or sent to a printer.

Cashier — Stock Check
    Look up price, quantity, and expiry without making a sale.
    Cashiers cannot add, edit, or delete medicines.

Administrator — Overview
    Shows SKU count, low stock, near-expiry items, and month-to-date sales.

Administrator — Manage Medicines
    Full create, read, update, and delete for inventory.
    Rows tint amber when a product expires within 30 days and red when
    stock is at or below the reorder level.

Administrator — Manage Suppliers
    Maintain supplier names, contacts, and addresses.

Administrator — Manage Users
    Create cashier (or extra admin) accounts, reset passwords, and
    delete unused logins. You cannot delete the account you are using.

Administrator — Reports
    Sales Report      transactions in a date range
    Item-Wise Report  units sold and revenue by medicine
    Low Stock Report  items at or below reorder level
    Expiry Report     medicines expiring in the next 30 days


6. Project layout
-----------------
src/            Java source (Swing GUI, JDBC DAOs, models)
lib/            H2, MySQL Connector/J, FlatLaf
database.sql    MySQL schema and sample data
screenshots/    Required interface captures
dist/           Runnable JAR
data/           Created at runtime for the bundled H2 database


7. Notes for marking
--------------------
Core technologies: Java Swing, JDBC.
Database tables match the specification: users, suppliers, medicines,
sales, sale_items, with primary keys, foreign keys, DECIMAL(10,2)
prices, and DATE expiry values.
