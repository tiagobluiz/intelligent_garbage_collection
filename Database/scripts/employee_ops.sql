USE db_waste_management
GO

CREATE PROC dbo.CreateEmployee @username nvarchar(50), @name nvarchar(100), @email nvarchar(100), 
	@phone_number numeric(9), @job nvarchar(13), @password nvarchar(8) OUT
AS
	BEGIN TRAN
	BEGIN TRY
		DECLARE @Length int, @CharPool nvarchar(max), @PoolLength int, @LoopCount int

		SET @Length = 8
		SET @CharPool = 
			'abcdefghijkmnopqrstuvwxyzABCDEFGHIJKLMNPQRSTUVWXYZ23456789!"#$%&/()=?*+.:,;_-'
		SET @PoolLength = Len(@CharPool)

		SET @LoopCount = 0
		SET @password = ''

		WHILE (@LoopCount < @Length) BEGIN
			SELECT @password = @password + 
				SUBSTRING(@Charpool, CONVERT(int, RAND() * @PoolLength) + 1, 1)
			SELECT @LoopCount = @LoopCount + 1
		END

		DECLARE @createLoginStatement nvarchar(190)
		SET @createLoginStatement = 'CREATE LOGIN ' + @username +
			' WITH PASSWORD = ''' + @password + ''', DEFAULT_DATABASE = db_waste_management'
		EXECUTE sp_executesql @createLoginStatement

		DECLARE @createUserStatement nvarchar(250)
		SET @createUserStatement = 'CREATE USER ' + @username +
			' FOR LOGIN ' + @username + ' WITH DEFAULT_SCHEMA = dbo'
		EXECUTE sp_executesql @createUserStatement

		DECLARE @grantConnectStatement nvarchar(190)
		SET @grantConnectStatement  = 'GRANT CONNECT TO ' + @username
		EXECUTE sp_executesql @grantConnectStatement

		DECLARE @addRolemember nvarchar(350)
		SET @addRolemember =  'ALTER ROLE ' + @job + ' ADD MEMBER ' + @username
		EXEC sp_executesql @addRolemember
		IF(@job = 'administrator')
		BEGIN
			SET @addRolemember =  'ALTER SERVER ROLE securityadmin ADD MEMBER ' + @username
			EXEC sp_executesql @addRolemember
			SET @addRolemember =  'ALTER SERVER ROLE processadmin ADD MEMBER ' + @username
			EXEC sp_executesql @addRolemember
			SET @addRolemember =  'ALTER ROLE db_accessadmin ADD MEMBER ' + @username
			EXEC sp_executesql @addRolemember
		END

		INSERT INTO dbo.EmployeeDetailedInfo(username,name,email,phone_number,job)
		VALUES (@username,@name,@email,@phone_number,@job)

		COMMIT
	END TRY
	BEGIN CATCH
		ROLLBACK;	
		THROW;
	END CATCH
GO

CREATE PROC dbo.UpdatePassword @username nvarchar(50), @password nvarchar(100) OUT
AS
	BEGIN TRAN
	BEGIN TRY
		DECLARE @Length int, @CharPool nvarchar(max), @PoolLength int, @LoopCount int

		SET @Length = 8
		SET @CharPool = 
			'abcdefghijkmnopqrstuvwxyzABCDEFGHIJKLMNPQRSTUVWXYZ23456789!"#$%&/()=?*+.:,;_-'
		SET @PoolLength = Len(@CharPool)

		SET @LoopCount = 0
		SET @password = ''

		WHILE (@LoopCount < @Length) BEGIN
			SELECT @password = @password + 
				SUBSTRING(@Charpool, CONVERT(int, RAND() * @PoolLength) + 1, 1)
			SELECT @LoopCount = @LoopCount + 1
		END
		DECLARE @changeLoginStatement nvarchar(190)
		SET @changeLoginStatement  = 'ALTER LOGIN ' + @username + 
			' WITH PASSWORD = ''' + @password + ''''
		EXECUTE sp_executesql @changeLoginStatement
		
		COMMIT
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.DeleteEmployee @username nvarchar(50)
AS
	BEGIN TRY
		DECLARE @loginNameToDrop sysname
		SET @loginNameToDrop = @username;

		DECLARE sessionsToKill CURSOR FAST_FORWARD FOR
			SELECT session_id
			FROM sys.dm_exec_sessions
			WHERE login_name = @username
		OPEN sessionsToKill

		DECLARE @sessionId INT
		DECLARE @statement NVARCHAR(200)

		FETCH NEXT FROM sessionsToKill INTO @sessionId

		WHILE @@FETCH_STATUS = 0
		BEGIN
			SET @statement = 'KILL ' + CAST(@sessionId AS NVARCHAR(20))
			EXEC sp_executesql @statement

			FETCH NEXT FROM sessionsToKill INTO @sessionId
		END

		CLOSE sessionsToKill
		DEALLOCATE sessionsToKill
		
		BEGIN TRAN
		SET @statement = 'DROP LOGIN IF EXISTS [' + @username + ']'
		EXEC sp_executesql @statement

		SET @statement = 'DROP USER IF EXISTS [' + @username + ']'
		EXEC sp_executesql @statement

		DELETE FROM dbo.EmployeeDetailedInfo
		WHERE username = @username;

		IF(@@ROWCOUNT = 0)
			THROW 55003,'The employee passed as parameter does not exists',1

		COMMIT
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.ChangeEmployeeJob @username nvarchar(50), @newJob nvarchar(13)
AS
	SET TRANSACTION ISOLATION LEVEL REPEATABLE READ
	BEGIN TRAN
	BEGIN TRY
		DECLARE @previousRole nvarchar(13)
		SELECT @previousRole = job FROM dbo.EmployeeDetailedInfo
		WHERE username = @username
		
		DECLARE @addRolemember nvarchar(350)
		SET @addRolemember =  'ALTER ROLE ' + @newJob + ' ADD MEMBER ' + @username
		EXEC sp_executesql @addRolemember
		IF(@newJob = 'administrator') --Grant permissions to create/update/delete logins
		BEGIN
			SET @addRolemember =  'ALTER SERVER ROLE securityadmin ADD MEMBER ' + @username
			EXEC sp_executesql @addRolemember
			SET @addRolemember =  'ALTER ROLE db_accessadmin ADD MEMBER ' + @username
			EXEC sp_executesql @addRolemember
		END

		DECLARE @dropRolemember nvarchar(350)
		SET @dropRolemember =  'ALTER ROLE ' + @previousRole + ' DROP MEMBER ' + @username
		EXEC sp_executesql @dropRolemember
		IF(@previousRole = 'administrator') --Grant permissions to create/update/delete logins
		BEGIN
			SET @addRolemember =  'ALTER SERVER ROLE securityadmin DROP MEMBER ' + @username
			EXEC sp_executesql @addRolemember
			SET @addRolemember =  'ALTER SERVER ROLE processadmin DROP MEMBER ' + @username
			EXEC sp_executesql @addRolemember
			SET @addRolemember =  'ALTER ROLE db_accessadmin DROP MEMBER ' + @username
			EXEC sp_executesql @addRolemember
		END

		UPDATE dbo.EmployeeDetailedInfo
		SET job = @newJob
		WHERE username = @username

		COMMIT
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE FUNCTION dbo.GetEmployeeInfo(@username nvarchar(50)) RETURNS TABLE AS 
RETURN(
	SELECT username, name, email, phone_number, job FROM dbo.EmployeeDetailedInfo
	WHERE username = @username
)
GO

CREATE FUNCTION dbo.GetCurrentEmployeeInfo() RETURNS TABLE AS 
RETURN(
	SELECT username, name, email, phone_number, job FROM dbo.EmployeeDetailedInfo
	WHERE username = SYSTEM_USER
)
GO

CREATE FUNCTION dbo.GetAllEmployees(@PageNumber int, @Rows int) RETURNS TABLE AS 
RETURN(
	SELECT COUNT(*) OVER() AS 'total_entries', username, name, email, phone_number, job FROM dbo.EmployeeDetailedInfo
	ORDER BY name
	OFFSET ((@PageNumber - 1) * @Rows) ROWS
	FETCH NEXT @Rows ROWS ONLY
)
GO