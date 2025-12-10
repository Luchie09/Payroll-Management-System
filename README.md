# Payroll-Management-System
A Payroll Management System that provides complete CRUD functionality for managing employee records and payroll information.
This system allows stakeholders to store, update, and maintain important employee and payroll data accurately and efficiently. 
By automating essential processes such as employee registration, salary computation, and record tracking, the system helps ensure 
consistency, reduce manual errors, and streamline overall payroll management operations.

To fully access and run this program. You must follow these requirements and steps-by-step procedures:

Preliminaries:
You must install or have the following tools and technologies;
- Visual Studio Code: https://code.visualstudio.com/docs
- Java Compiler: https://code.visualstudio.com/docs/java/java-tutorial (follow the steps provided in the link)
- Add Extension Pack for Java in your VS Code extensions
- MySQL WorkBench: https://dev.mysql.com/downloads/installer/
- XAMPP: https://www.apachefriends.org/index.html

Step-by-Step Procedure for Installing and Setting Up MySQL
Part I: Installation
1. Download MySQL Installer
   - Go to: [https://dev.mysql.com/downloads/installer/](https://dev.mysql.com/downloads/installer/)
   - Download the MySQL Installer for Windows.
2. Run MySQL Installer
   - Double-click the downloaded file.
   - Wait for Windows to configure the installer.
3. Choose Setup Type
   - In the installer, if “Developer Default” is not available, select "Custom".
   - Click Next.
4. Select Products to Install
   - Choose the latest versions of:
     - MySQL Server
     - MySQL Workbench
     - MySQL Shell
   - Click Next and proceed through the download and installation steps.
5. Configure MySQL Server
   - Click Next on the Product Configuration screen.
   - For Config Type, select "Development Computer".
   - Leave other networking settings as default.
   - Click Next.
6. Set Authentication Method
   - Select Use Strong Password Encryption for Authentication (Recommended).
   - Click Next.
7. Set Root Password
   - Enter a password for the root account.
   - Re-enter the password to confirm.
   - Optionally, add MySQL user accounts (skip if not needed).
   - Click Next.
8. Configure Windows Service
   - Enable Configure MySQL Server as a Windows Service.
   - Set the service name as MySQL80.
   - Choose Start the MySQL Server at System Startup.
   - Select Standard System Account for the service to run under.
   - Click Next.
9. Set Server File Permissions
   - Choose Yes, grant full access to the user running the Windows Service….
   - Click Next.
10. Apply Configuration
    - Click Execute to apply all settings.
    - Wait for the process to complete (may take several minutes).
    - Click Finish.
Part II: Launch and Connect to MySQL Workbench**
1. Open MySQL Workbench
   - Launch it from the Start Menu.
2. Connect to Local Instance
   - Under MySQL Connections, click Local Instance MySQL80.
   - Enter the root password you set earlier.
   - Click OK to connect.
3. Verify Connection
   - Check the status in the Output Panel to confirm a successful connection.

Step-by-Step XAMPP Installation & Setup
1. Download XAMPP
   - Go to https://www.apachefriends.org/index.html and download the installer for your OS.
2. Run Installer as Administrator
   - Right-click the downloaded file → Run as administrator.
3. Select Components
   - Check all components (Apache, MySQL, PHP, phpMyAdmin, etc.) → Click Next.
4. Choose Installation Folder
   - Select destination (default is C:\xampp) → Click Next.
5. Choose Language
   - Select English from the dropdown → Click Next.
6. Complete Installation
   - Click Next and wait for setup to finish.
7. Open XAMPP Control Panel
   - Launch from Start Menu or desktop shortcut.
8. Start Required Services
   - In the Control Panel, click Start for:
     - Apache
     - MySQL
10. Verify Services Are Running
    - Both should show a green status or “Running”.
11. Access phpMyAdmin
    - Click the Admin button next to MySQL to open phpMyAdmin in your browser.
   
Once all prerequisites are prepared, you may proceed with the installation and execution of the system:
1. Download the Payroll project folder and open it in Visual Studio Code.
2. Create the database by executing the SQL files provided inside the PayrollDB folder.
   - Alternatively, you may execute all the queries in the "payrolldbscript" file to generate all required tables and records likewise.
3. After setting up the database, run the application by launching the Login.java file.

Default Admin Credentials
Use the following credentials to access the Admin Dashboard:
Username: admi
Password: 1234





