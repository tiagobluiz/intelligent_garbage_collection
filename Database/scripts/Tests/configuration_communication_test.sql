USE db_waste_management
GO

CREATE PROC dbo.AssociateCommunicationToTheConfigurationTest
AS
	SET NOCOUNT ON
	BEGIN TRAN
	BEGIN TRY
		--Prepare
		DECLARE @configId int
		INSERT INTO dbo.Configuration(configuration_name) VALUES ('Config Test')
		SET @configId = @@IDENTITY

		DECLARE @commId int
		INSERT INTO dbo.Communication (communication_designation) VALUES('Com Test')
		SET @commId  = @@IDENTITY
		
		--Execute
		EXEC dbo.AssociateCommunicationToTheConfiguration @configId, @commId, 50

		--Test
		IF NOT EXISTS(
			SELECT configuration_id, communication_id FROM dbo.ConfigurationCommunication
			WHERE configuration_id = @configId AND communication_id = @commId AND value = 50
		)	THROW 55005,'ConfigurationCommunicationTest.AssociateCommunicationToTheConfigurationTest FAILED', 1
		
		ROLLBACK;
		PRINT 'ConfigurationCommunicationTest.AssociateCommunicationToTheConfigurationTest SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.DisassociateCommunicationToConfigurationTest
AS
	SET NOCOUNT ON
	BEGIN TRAN
	BEGIN TRY
		--Prepare
		DECLARE @configId int
		INSERT INTO dbo.Configuration(configuration_name) VALUES ('Config Test')
		SET @configId = @@IDENTITY

		DECLARE @commId int
		INSERT INTO dbo.Communication (communication_designation) VALUES('Com Test')
		SET @commId  = @@IDENTITY
		
		INSERT INTO dbo.ConfigurationCommunication (configuration_id, communication_id, value)
		VALUES (@configId, @commId, 50)

		--Execute
		EXEC dbo.DisassociateCommunicationToConfiguration @configId, @commId

		--Test
		IF EXISTS(
			SELECT configuration_id, communication_id FROM dbo.ConfigurationCommunication
			WHERE configuration_id = @configId AND communication_id = @commId
		)	THROW 55005,'ConfigurationCommunicationTest.DisassociateCommunicationToConfigurationTest FAILED', 1
		
		ROLLBACK;
		PRINT 'ConfigurationCommunicationTest.DisassociateCommunicationToConfigurationTest SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.GetConfigurationCommunicationsTest
AS
	SET NOCOUNT ON
	BEGIN TRAN
	BEGIN TRY
		--Prepare
		DECLARE @configId int
		INSERT INTO dbo.Configuration(configuration_name) VALUES ('Config Test')
		SET @configId = @@IDENTITY

		DECLARE @commId int
		INSERT INTO dbo.Communication (communication_designation) VALUES('Com Test')
		SET @commId  = @@IDENTITY
		
		INSERT INTO dbo.ConfigurationCommunication (configuration_id, communication_id, value)
		VALUES (@configId, @commId, 50)

		--Execute & Test
		DECLARE @entries int
		SELECT @entries = total_entries FROM dbo.GetConfigurationCommunications(1, 10, @configId)
		IF(@entries < 1)
			THROW 55005,'ConfigurationCommunicationTest.GetConfigurationCommunicationsTest FAILED', 1

		IF NOT EXISTS(
			SELECT configuration_id, communication_id, value FROM dbo.GetConfigurationCommunications(1, @entries, @configId)
			WHERE configuration_id = @configId AND communication_id = @commId AND value = 50
		)	THROW 55005,'ConfigurationCommunicationTest.GetConfigurationCommunicationsTest FAILED', 1
		
		ROLLBACK;
		PRINT 'ConfigurationCommunicationTest.GetConfigurationCommunicationsTest SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.GetConfigurationCommunicationTest
AS
	SET NOCOUNT ON
	BEGIN TRAN
	BEGIN TRY
		--Prepare
		DECLARE @configId int
		INSERT INTO dbo.Configuration(configuration_name) VALUES ('Config Test')
		SET @configId = @@IDENTITY

		DECLARE @commId int
		INSERT INTO dbo.Communication (communication_designation) VALUES('Com Test')
		SET @commId  = @@IDENTITY
		
		INSERT INTO dbo.ConfigurationCommunication (configuration_id, communication_id, value)
		VALUES (@configId, @commId, 50)

		--Execute & Test
		IF NOT EXISTS(
			SELECT configuration_id, communication_id, value FROM dbo.GetConfigurationCommunication(@configId, @commId)
			WHERE configuration_id = @configId AND communication_id = @commId AND value = 50
		)	THROW 55005,'ConfigurationCommunicationTest.GetConfigurationCommunicationTest FAILED', 1
		
		ROLLBACK;
		PRINT 'ConfigurationCommunicationTest.GetConfigurationCommunicationTest SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.GetCommunicationValueForConfigurationTest
AS
	SET NOCOUNT ON
	BEGIN TRAN
	BEGIN TRY
		--Prepare
		DECLARE @configId int
		INSERT INTO dbo.Configuration(configuration_name) VALUES ('Config Test')
		SET @configId = @@IDENTITY

		DECLARE @commName nvarchar(100), @commId int
		SET @commName = 'Com Test'
		INSERT INTO dbo.Communication (communication_designation) VALUES(@commName)
		SET @commId  = @@IDENTITY
		
		INSERT INTO dbo.ConfigurationCommunication (configuration_id, communication_id, value)
		VALUES (@configId, @commId, 50)

		--Execute & Test
		IF ((SELECT dbo.GetCommunicationValueForConfiguration(@configId,@commName)) <> 50)
			THROW 55005,'ConfigurationCommunicationTest.GetCommunicationValueForConfigurationTest FAILED', 1
		
		ROLLBACK;
		PRINT 'ConfigurationCommunicationTest.GetCommunicationValueForConfigurationTest SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.GetConfigurationCommunicationByNameTest
AS
	SET NOCOUNT ON
	BEGIN TRAN
	BEGIN TRY
		--Prepare
		DECLARE @configId int
		INSERT INTO dbo.Configuration(configuration_name) VALUES ('Config Test')
		SET @configId = @@IDENTITY

		DECLARE @commId int, @commName nvarchar(50)
		SET @commName = 'Com Test'
		INSERT INTO dbo.Communication (communication_designation) VALUES(@commName)
		SET @commId  = @@IDENTITY
		
		INSERT INTO dbo.ConfigurationCommunication (configuration_id, communication_id, value)
		VALUES (@configId, @commId, 50)

		--Execute & Test
		IF NOT EXISTS(
			SELECT configuration_id, communication_id, value FROM dbo.GetConfigurationCommunicationByName(@configId, @commName)
			WHERE configuration_id = @configId AND communication_id = @commId AND value = 50
		)	THROW 55005,'ConfigurationCommunicationTest.GetConfigurationCommunicationByNameTest FAILED', 1
		
		ROLLBACK;
		PRINT 'ConfigurationCommunicationTest.GetConfigurationCommunicationByNameTest SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO