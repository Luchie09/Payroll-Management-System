
CREATE DATABASE IF NOT EXISTS PayrollDB;
USE PayrollDB;

CREATE TABLE DEPARTMENT (
    DepartmentID INT AUTO_INCREMENT PRIMARY KEY,
    DepartmentName VARCHAR(50) NOT NULL
);

CREATE TABLE POSITION (
    PositionID INT AUTO_INCREMENT PRIMARY KEY,
    PositionName VARCHAR(50) NOT NULL,
    BaseSalary DECIMAL(10,2) NOT NULL,
    DepartmentID INT,
    FOREIGN KEY (DepartmentID) REFERENCES DEPARTMENT(DepartmentID)
);

CREATE TABLE EMPLOYEE (
    EmployeeID INT AUTO_INCREMENT PRIMARY KEY,
    LastName VARCHAR(50) NOT NULL,
    FirstName VARCHAR(50) NOT NULL,
    Age INT,
    DOB DATE,
    Address VARCHAR(100),
    PhoneNumber VARCHAR(20),
    DateOfHire DATE NOT NULL,
    DepartmentID INT,
    PositionID INT,
    MonthlySalary DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (DepartmentID) REFERENCES DEPARTMENT(DepartmentID),
    FOREIGN KEY (PositionID) REFERENCES POSITION (PositionID)
);

CREATE TABLE TIMESHEET (
    TimesheetID INT AUTO_INCREMENT PRIMARY KEY,
    EmployeeID INT NOT NULL,
    WorkDate DATE NOT NULL,
    StandardHours DECIMAL(5,2) NOT NULL DEFAULT 8.00,
    RenderedHours DECIMAL(5,2) NOT NULL,
    OvertimeHours DECIMAL(5,2) GENERATED ALWAYS AS (GREATEST(RenderedHours - StandardHours,0)) STORED,
    FOREIGN KEY (EmployeeID) REFERENCES EMPLOYEE(EmployeeID)
);

CREATE TABLE GROSS_PAY (
    GrossPayID INT AUTO_INCREMENT PRIMARY KEY,
    TimesheetID INT NOT NULL,
    StdHourlyRate DECIMAL(10,2) NOT NULL,
    OTHourlyRate DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (TimesheetID) REFERENCES TIMESHEET(TimesheetID)
);

CREATE VIEW GROSS_PAY_VIEW AS
SELECT g.GrossPayID, g.TimesheetID, g.StdHourlyRate, g.OTHourlyRate,
       (t.StandardHours * g.StdHourlyRate + t.OvertimeHours * g.OTHourlyRate) AS DailyGrossIncome
FROM GROSS_PAY g
JOIN TIMESHEET t ON g.TimesheetID = t.TimesheetID;

CREATE TABLE DEDUCTION (
    DeductionID INT AUTO_INCREMENT PRIMARY KEY,
    Description VARCHAR(50) NOT NULL,
    Default_Amount DECIMAL(10,2) NOT NULL
);

CREATE TABLE PAYROLL (
    PayrollID INT AUTO_INCREMENT PRIMARY KEY,
    EmployeeID INT NOT NULL,
    ReferenceNo VARCHAR(50) UNIQUE NOT NULL,
    Start_Cut_Off DATE NOT NULL,
    End_Cut_Off DATE NOT NULL,
    FOREIGN KEY (EmployeeID) REFERENCES EMPLOYEE(EmployeeID)
);

CREATE TABLE PAYROLL_DEDUCTION (
    PayrollDeductionID INT AUTO_INCREMENT PRIMARY KEY,
    PayrollID INT NOT NULL,
    DeductionID INT NOT NULL,
    Amount DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (PayrollID) REFERENCES PAYROLL(PayrollID),
    FOREIGN KEY (DeductionID) REFERENCES DEDUCTION(DeductionID)
);

CREATE TABLE USER (
    UserID INT AUTO_INCREMENT PRIMARY KEY,
    EmployeeID INT,
    Username VARCHAR(50) UNIQUE NOT NULL,
    Password VARCHAR(255) NOT NULL,
    Role ENUM('Admin','Employee') NOT NULL,
    FOREIGN KEY (EmployeeID) REFERENCES EMPLOYEE(EmployeeID)
);

ALTER TABLE USER
MODIFY Password VARCHAR(20) NOT NULL;

ALTER TABLE EMPLOYEE
MODIFY Email VARCHAR(100) NOT NULL;

ALTER TABLE EMPLOYEE
MODIFY Gender ENUM('Male', 'Female') NOT NULL;

ALTER TABLE EMPLOYEE
ADD CONSTRAINT chk_phone
CHECK (PhoneNumber REGEXP '^[0-9]{11}$');

ALTER TABLE EMPLOYEE
ADD CONSTRAINT chk_age
CHECK (Age BETWEEN 18 AND 65);

CREATE VIEW PAYROLL_CALC AS
SELECT p.PayrollID, p.EmployeeID, p.ReferenceNo, p.Start_Cut_Off, p.End_Cut_Off,
       IFNULL(SUM(t.StandardHours * g.StdHourlyRate + t.OvertimeHours * g.OTHourlyRate),0) AS TotalGrossPay,
       IFNULL(SUM(pd.Amount),0) AS TotalDeduction,
       IFNULL(SUM(t.StandardHours * g.StdHourlyRate + t.OvertimeHours * g.OTHourlyRate),0) - IFNULL(SUM(pd.Amount),0) AS NetPay
FROM PAYROLL p
LEFT JOIN TIMESHEET t ON t.EmployeeID = p.EmployeeID AND t.WorkDate BETWEEN p.Start_Cut_Off AND p.End_Cut_Off
LEFT JOIN GROSS_PAY g ON g.TimesheetID = t.TimesheetID
LEFT JOIN PAYROLL_DEDUCTION pd ON pd.PayrollID = p.PayrollID
GROUP BY p.PayrollID;

-- Departments
INSERT INTO DEPARTMENT (DepartmentName) VALUES ('HR'), ('IT'), ('Finance');

-- Positions
INSERT INTO POSITION (PositionName, BaseSalary, DepartmentID) 
VALUES ('HR Manager', 30000, 1),
       ('Software Engineer', 35000, 2),
       ('Accountant', 28000, 3);

-- Employees
INSERT INTO EMPLOYEE (LastName, FirstName, Age, DOB, Address, PhoneNumber, DateOfHire, DepartmentID, PositionID, MonthlySalary)
VALUES ('Dela Cruz', 'Juan', 25, '2000-05-15', 'Makati City', '09171234567', '2022-01-10', 2, 2, 35000);

-- Users
INSERT INTO USER (EmployeeID, Username, Password, Role)
VALUES (1, 'admin', 'admin_hashed_password', 'Admin');

-- Deductions
INSERT INTO DEDUCTION (Description, Default_Amount) VALUES ('Tax', 500), ('SSS', 300), ('PhilHealth', 200);

-- Sample Timesheet
INSERT INTO TIMESHEET (EmployeeID, WorkDate, StandardHours, RenderedHours)
VALUES (1, '2025-12-01', 8, 9),  -- 1 hour OT
       (1, '2025-12-02', 8, 8),  -- no OT
       (1, '2025-12-03', 8, 10); -- 2 hours OT

-- Sample Gross Pay
INSERT INTO GROSS_PAY (TimesheetID, StdHourlyRate, OTHourlyRate)
VALUES (1, 200, 300),
       (2, 200, 300),
       (3, 200, 300);

-- Sample Payroll
INSERT INTO PAYROLL (EmployeeID, ReferenceNo, Start_Cut_Off, End_Cut_Off)
VALUES (1, 'REF20251201', '2025-12-01', '2025-12-15');

-- Sample Payroll Deductions
INSERT INTO PAYROLL_DEDUCTION (PayrollID, DeductionID, Amount)
VALUES (1, 1, 500),
       (1, 2, 300),
       (1, 3, 200);

ALTER TABLE USER
MODIFY Password VARCHAR(20) NOT NULL;

ALTER TABLE EMPLOYEE
MODIFY Email VARCHAR(100) NOT NULL;

ALTER TABLE EMPLOYEE
MODIFY Gender ENUM('Male', 'Female') NOT NULL;

ALTER TABLE EMPLOYEE
ADD CONSTRAINT chk_phone
CHECK (PhoneNumber REGEXP '^[0-9]{11}$');

ALTER TABLE EMPLOYEE
ADD CONSTRAINT chk_age
CHECK (Age BETWEEN 18 AND 65);




