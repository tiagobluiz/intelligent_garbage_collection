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

		DECLARE @createUserStatement nvarchar(250)
		SET @createUserStatement = 'CREATE USER ' + @username +
			' WITH PASSWORD=''' + @password + ''''
		EXECUTE sp_executesql @createUserStatement

		DECLARE @addRolemember nvarchar(350)
		SET @addRolemember =  'ALTER ROLE ' + @job + ' ADD MEMBER ' + @username
		EXEC sp_executesql @addRolemember

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
		SET @changeLoginStatement  = 'ALTER USER ' + @username + 
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
	BEGIN TRAN
	BEGIN TRY
		DECLARE @statement nvarchar(250)
		SET @statement = 'DROP USER IF EXISTS [' + @username + '];'
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

		DECLARE @dropRolemember nvarchar(350)
		SET @dropRolemember =  'ALTER ROLE ' + @previousRole + ' DROP MEMBER ' + @username
		EXEC sp_executesql @dropRolemember

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