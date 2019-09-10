USE db_waste_management
GO

CREATE PROC dbo.CreateCommunicationTest
AS
	SET NOCOUNT ON
	BEGIN TRAN
	BEGIN TRY
		--Prepare
		DECLARE @communicationName nvarchar(100)
		SET @communicationName = 'Com Test'

		--Execute
		DECLARE @commId int
		EXEC dbo.CreateCommunication @communicationName, @commId OUT

		--Test
		IF NOT EXISTS(
			SELECT communication_id, communication_designation FROM dbo.Communication
			WHERE communication_id = @commId AND communication_designation = @communicationName
		) THROW 55005, 'CommunicationTest.CreateCommunicationTest() FAILED', 1

		ROLLBACK;
		PRINT 'CommunicationTest.CreateCommunicationTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.UpdateCommunicationTest
AS
	SET NOCOUNT ON
	BEGIN TRAN
	BEGIN TRY
		--Prepare
		DECLARE @commId int
		INSERT INTO dbo.Communication (communication_designation) VALUES('Com Test')
		SET @commId  = @@IDENTITY

		--Execute
		DECLARE @communicationName nvarchar(100)
		SET @communicationName = 'New Com Test'
		EXEC dbo.UpdateCommunication @commId, @communicationName

		--Test
		IF NOT EXISTS(
			SELECT communication_id, communication_designation FROM dbo.Communication
			WHERE communication_id = @commId AND communication_designation = @communicationName
		) THROW 55005, 'CommunicationTest.UpdateCommunicationTest() FAILED', 1

		ROLLBACK;
		PRINT 'CommunicationTest.UpdateCommunicationTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.DeleteCommunicationTest
AS
	SET NOCOUNT ON
	BEGIN TRAN
	BEGIN TRY
		--Prepare
		DECLARE @communicationName nvarchar(100)
		SET @communicationName = 'Com Test'
		DECLARE @commId int
		INSERT INTO dbo.Communication (communication_designation) VALUES(@communicationName)
		SET @commId  = @@IDENTITY

		--Execute
		EXEC dbo.DeleteCommunication @commId

		--Test
		IF EXISTS(
			SELECT communication_id, communication_designation FROM dbo.Communication
			WHERE communication_id = @commId AND communication_designation = @communicationName
		) THROW 55005, 'CommunicationTest.DeleteCommunicationTest() FAILED', 1

		ROLLBACK;
		PRINT 'CommunicationTest.DeleteCommunicationTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.GetAllCommunicationsTest
AS
	SET NOCOUNT ON
	BEGIN TRAN
	BEGIN TRY
		--Prepare
		DECLARE @commId1 int
		INSERT INTO dbo.Communication (communication_designation) VALUES('Com Test 1')
		SET @commId1  = @@IDENTITY
		DECLARE @commId2 int
		INSERT INTO dbo.Communication (communication_designation) VALUES('Com Test 2')
		SET @commId2  = @@IDENTITY

		--Execute & Test
		DECLARE @count int
		SELECT @count = total_entries FROM dbo.GetAllCommunications(1,10)
		IF (@count < 2) THROW 55005, 'CommunicationTest.GetAllCommunicationsTest() FAILED', 1

		IF NOT EXISTS(
			SELECT * FROM dbo.GetAllCommunications(1, @count)
			WHERE communication_id = @commId1
		) THROW 55005, 'CommunicationTest.GetAllCommunicationsTest() FAILED', 1

		IF NOT EXISTS(
			SELECT * FROM dbo.GetAllCommunications(1, @count)
			WHERE communication_id = @commId2
		) THROW 55005, 'CommunicationTest.GetAllCommunicationsTest() FAILED', 1

		ROLLBACK;
		PRINT 'CommunicationTest.GetAllCommunicationsTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.GetCommunicationTest
AS
	SET NOCOUNT ON
	BEGIN TRAN
	BEGIN TRY
		--Prepare
		DECLARE @commId1 int
		DECLARE @commName nvarchar(100)
		SET @commName = 'Com Test'
		INSERT INTO dbo.Communication (communication_designation) VALUES(@commName)
		SET @commId1  = @@IDENTITY

		--Execute & 
		IF NOT EXISTS(
			SELECT communication_designation FROM dbo.GetCommunication(@commId1)
			WHERE communication_id = @commId1 AND communication_designation = @commName
		) THROW 55005, 'CommunicationTest.GetCommunicationTest() FAILED', 1

		ROLLBACK;
		PRINT 'CommunicationTest.GetCommunicationTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO
