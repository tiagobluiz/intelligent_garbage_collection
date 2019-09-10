USE db_waste_management
GO

CREATE PROC dbo.CreateEmployeeTest
AS
	SET NOCOUNT ON
	BEGIN TRAN
	BEGIN TRY
		--Prepare
		DECLARE @username nvarchar(50)
		SET @username = 'thisisatest'

		--Execute
		DECLARE @pass nvarchar(8)
		EXEC dbo.CreateEmployee @username, 'Username', 'thisiarealemail@wastemanagement.com', 912344454, 'administrator', @pass OUT

		--Test
		IF @pass IS NULL
			THROW 55005, '@EmployeeTest.CreateEmployeeTest() FAILED', 1
		IF NOT EXISTS(
			SELECT * FROM dbo.EmployeeDetailedInfo
			WHERE username = @username AND name = 'Username' AND email = 'thisiarealemail@wastemanagement.com' 
				AND phone_number = 912344454
		) THROW 55005, '@EmployeeTest.CreateEmployeeTest() FAILED', 1

		IF NOT EXISTS(
			SELECT u.name, r.name FROM sys.database_role_members AS m
			INNER JOIN sys.database_principals AS r
			ON m.role_principal_id = r.principal_id
			INNER JOIN sys.database_principals AS u
			ON u.principal_id = m.member_principal_id
			WHERE u.name = @username AND r.name = 'administrator'
		)THROW 55005, '@EmployeeTest.CreateEmployeeTest() FAILED', 1

		ROLLBACK;
		PRINT '@EmployeeTest.CreateEmployeeTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.UpdatePasswordTest
AS
	SET NOCOUNT ON
	BEGIN TRAN
	BEGIN TRY
		--Prepare
		DECLARE @username nvarchar(50)
		SET @username = 'thisisatest'
		DECLARE @pass nvarchar(8)
		EXEC dbo.CreateEmployee @username, 'Username', 'thisiarealemail@wastemanagement.com', 912344454, 'administrator', @pass OUT

		--Execute
		DECLARE @newPass nvarchar(8)
		EXEC dbo.UpdatePassword @username, @newPass OUT

		--Test
		IF @newPass IS NULL
			THROW 55005, '@EmployeeTest.UpdatePasswordTest() FAILED', 1
		IF(@newPass = @pass)
			THROW 55005, '@EmployeeTest.UpdatePasswordTest() FAILED', 1
		
		ROLLBACK;
		PRINT '@EmployeeTest.UpdatePasswordTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO


CREATE PROC dbo.DeleteEmployeeTest
AS
	SET NOCOUNT ON
	BEGIN TRAN
	BEGIN TRY
		--Prepare
		DECLARE @username nvarchar(50)
		SET @username = 'thisisatest'
		DECLARE @pass nvarchar(8)
		EXEC dbo.CreateEmployee @username, 'Username', 'thisiarealemail@wastemanagement.com', 912344454, 'administrator', @pass OUT

		--Execute
		EXEC dbo.DeleteEmployee @username

		--Test
		IF EXISTS(
			SELECT * FROM dbo.EmployeeDetailedInfo
			WHERE username = @username AND name = 'Username' AND email = 'thisiarealemail@wastemanagement.com' 
				AND phone_number = 912344454
		) THROW 55005, '@EmployeeTest.DeleteEmployeeTest() FAILED', 1

		IF EXISTS(
			SELECT u.name, r.name FROM sys.database_role_members AS m
			INNER JOIN sys.database_principals AS r
			ON m.role_principal_id = r.principal_id
			INNER JOIN sys.database_principals AS u
			ON u.principal_id = m.member_principal_id
			WHERE u.name = @username AND r.name = 'administrator'
		)THROW 55005, '@EmployeeTest.DeleteEmployeeTest() FAILED', 1

		ROLLBACK;
		PRINT '@EmployeeTest.DeleteEmployeeTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.ChangeEmployeeJobTest
AS
	SET NOCOUNT ON
	BEGIN TRAN
	BEGIN TRY
		--Prepare
		DECLARE @username nvarchar(50)
		SET @username = 'thisisatest'
		DECLARE @pass nvarchar(8)
		EXEC dbo.CreateEmployee @username, 'Username', 'thisiarealemail@wastemanagement.com', 912344454, 'administrator', @pass OUT

		--Execute
		EXEC dbo.ChangeEmployeeJob @username, 'collector'

		--Test
		IF NOT EXISTS(
			SELECT * FROM dbo.EmployeeDetailedInfo
			WHERE username = @username AND name = 'Username' AND email = 'thisiarealemail@wastemanagement.com' 
				AND phone_number = 912344454 AND job = 'collector'
		) THROW 55005, '@EmployeeTest.ChangeEmployeeJobTest() FAILED', 1

		IF NOT EXISTS(
			SELECT u.name, r.name FROM sys.database_role_members AS m
			INNER JOIN sys.database_principals AS r
			ON m.role_principal_id = r.principal_id
			INNER JOIN sys.database_principals AS u
			ON u.principal_id = m.member_principal_id
			WHERE u.name = @username AND r.name = 'collector'
		)THROW 55005, '@EmployeeTest.DeleteEmployeeTest() FAILED', 1

		ROLLBACK;
		PRINT '@EmployeeTest.ChangeEmployeeJobTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.GetAllEmployeesTest
AS
	SET NOCOUNT ON
	BEGIN TRAN
	BEGIN TRY
		--Prepare
		DECLARE @username nvarchar(50)
		SET @username = 'thisisatest'
		DECLARE @pass nvarchar(8)
		EXEC dbo.CreateEmployee @username, 'Username', 'thisiarealemail@wastemanagement.com', 912344454, 'administrator', @pass OUT

		--Execute & Test
		DECLARE @entries int
		SELECT @entries = total_entries FROM dbo.GetAllEmployees(1,10)

		IF(@entries < 1)
			THROW 55005, '@EmployeeTest.GetAllEmployeesTest() FAILED', 1

		IF NOT EXISTS(
			SELECT * FROM dbo.GetAllEmployees(1,@entries)
			WHERE username = @username AND name = 'Username' AND email = 'thisiarealemail@wastemanagement.com' 
				AND phone_number = 912344454
		) THROW 55005, '@EmployeeTest.GetAllEmployeesTest() FAILED', 1

		ROLLBACK;
		PRINT '@EmployeeTest.GetAllEmployeesTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.GetEmployeeInfoTest
AS
	SET NOCOUNT ON
	BEGIN TRAN
	BEGIN TRY
		--Prepare
		DECLARE @username nvarchar(50)
		SET @username = 'thisisatest'
		DECLARE @pass nvarchar(8)
		EXEC dbo.CreateEmployee @username, 'Username', 'thisiarealemail@wastemanagement.com', 912344454, 'administrator', @pass OUT

		--Execute & Test
		
		IF NOT EXISTS(
			SELECT * FROM dbo.GetEmployeeInfo(@username)
			WHERE username = @username AND name = 'Username' AND email = 'thisiarealemail@wastemanagement.com' 
				AND phone_number = 912344454
		) THROW 55005, '@EmployeeTest.GetEmployeeInfoTest() FAILED', 1

		ROLLBACK;
		PRINT '@EmployeeTest.GetEmployeeInfoTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO



