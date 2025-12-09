-- MySQL dump 10.13  Distrib 8.0.43, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: payrolldb
-- ------------------------------------------------------
-- Server version	5.5.5-10.4.32-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Temporary view structure for view `gross_pay_view`
--

DROP TABLE IF EXISTS `gross_pay_view`;
/*!50001 DROP VIEW IF EXISTS `gross_pay_view`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `gross_pay_view` AS SELECT 
 1 AS `GrossPayID`,
 1 AS `TimesheetID`,
 1 AS `StdHourlyRate`,
 1 AS `OTHourlyRate`,
 1 AS `DailyGrossIncome`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `payroll_calc`
--

DROP TABLE IF EXISTS `payroll_calc`;
/*!50001 DROP VIEW IF EXISTS `payroll_calc`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `payroll_calc` AS SELECT 
 1 AS `PayrollID`,
 1 AS `EmployeeID`,
 1 AS `ReferenceNo`,
 1 AS `Start_Cut_Off`,
 1 AS `End_Cut_Off`,
 1 AS `TotalGrossPay`,
 1 AS `TotalDeduction`,
 1 AS `NetPay`*/;
SET character_set_client = @saved_cs_client;

--
-- Final view structure for view `gross_pay_view`
--

/*!50001 DROP VIEW IF EXISTS `gross_pay_view`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `gross_pay_view` AS select `g`.`GrossPayID` AS `GrossPayID`,`g`.`TimesheetID` AS `TimesheetID`,`g`.`StdHourlyRate` AS `StdHourlyRate`,`g`.`OTHourlyRate` AS `OTHourlyRate`,`t`.`StandardHours` * `g`.`StdHourlyRate` + `t`.`OvertimeHours` * `g`.`OTHourlyRate` AS `DailyGrossIncome` from (`gross_pay` `g` join `timesheet` `t` on(`g`.`TimesheetID` = `t`.`TimesheetID`)) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `payroll_calc`
--

/*!50001 DROP VIEW IF EXISTS `payroll_calc`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `payroll_calc` AS select `p`.`PayrollID` AS `PayrollID`,`p`.`EmployeeID` AS `EmployeeID`,`p`.`ReferenceNo` AS `ReferenceNo`,`p`.`Start_Cut_Off` AS `Start_Cut_Off`,`p`.`End_Cut_Off` AS `End_Cut_Off`,ifnull(sum(`t`.`StandardHours` * `g`.`StdHourlyRate` + `t`.`OvertimeHours` * `g`.`OTHourlyRate`),0) AS `TotalGrossPay`,ifnull(sum(`pd`.`Amount`),0) AS `TotalDeduction`,ifnull(sum(`t`.`StandardHours` * `g`.`StdHourlyRate` + `t`.`OvertimeHours` * `g`.`OTHourlyRate`),0) - ifnull(sum(`pd`.`Amount`),0) AS `NetPay` from (((`payroll` `p` left join `timesheet` `t` on(`t`.`EmployeeID` = `p`.`EmployeeID` and `t`.`WorkDate` between `p`.`Start_Cut_Off` and `p`.`End_Cut_Off`)) left join `gross_pay` `g` on(`g`.`TimesheetID` = `t`.`TimesheetID`)) left join `payroll_deduction` `pd` on(`pd`.`PayrollID` = `p`.`PayrollID`)) group by `p`.`PayrollID` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-12-10  2:08:09
