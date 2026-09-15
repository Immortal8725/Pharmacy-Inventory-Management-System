# HealthFirst PIMS

Desktop Pharmacy Inventory Management System for HealthFirst Pharmacy (PRO732).

Java Swing GUI, JDBC, role-based Admin/Cashier access, point of sale, billing, and reports.

## How to run (Windows)

1. Open this project folder.
2. Start the app with **one** of these:
   - Double-click **`healthfirst_pims.exe`**
   - Or double-click **`healthfirst_pims.bat`**
   - Or from PowerShell / CMD in this folder:
     ```bat
     java -jar dist\healthfirst_pims.jar
     ```
3. Sign in with one of the accounts below.

### Requirements

- **JDK 17 or newer** (Java must be installed)
- No MySQL install needed for normal use — the app uses the built-in H2 database

### Default logins

| Role | Username | Password |
|------|----------|----------|
| Administrator | `admin` | `admin123` |
| Cashier | `cashier` | `cash123` |

### Tip

Only run **one** copy at a time. If startup fails with a database lock, close any other open HealthFirst window and try again.

---

See `README.txt` for the full user manual, optional MySQL setup, and module guide.
