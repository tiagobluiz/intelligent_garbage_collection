USE db_waste_management
GO

CREATE PROC dbo.CreateConfigurationTest
AS
	SET NOCOUNT ON
	BEGIN TRAN
	BEGIN TRY
		--Prepare
		DECLARE @configName nvarchar(100)
		SET @configName = 'Config Test'

		--Execute
		DECLARE @configId int
		EXEC dbo.CreateConfiguration @configName, @configId OUT

		--Test
		IF NOT EXISTS(
			SELECT configuration_id, configuration_name FROM dbo.Configuration
			WHERE configuration_id = @configId AND configuration_name = @configName
		)	THROW 55005, 'ConfigurationTest.CreateConfiguationTest() FAILED',1

		ROLLBACK;
		PRINT 'ConfigurationTest.CreateConfiguationTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.DeleteConfigurationTest
AS
	SET NOCOUNT ON
	BEGIN TRAN
	BEGIN TRY
		--Prepare
		DECLARE @configId int
		INSERT INTO dbo.Configuration(configuration_name) VALUES ('Config Test')
		SET @configId = @@IDENTITY
		
		--Execute
		EXEC dbo.DeleteConfiguration @configId

		--Test
		IF EXISTS(
			SELECT configuration_id, configuration_name FROM dbo.Configuration
			WHERE configuration_id = @configId
		)	THROW 55005, 'ConfigurationTest.DeleteConfigurationTest() FAILED',1

		ROLLBACK;
		PRINT 'ConfigurationTest.DeleteConfigurationTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.GetAllConfigurationsTest
AS
	SET NOCOUNT ON
	BEGIN TRAN
	BEGIN TRY
		--Prepare
		DECLARE @configId1 int
		INSERT INTO dbo.Configuration(configuration_name) VALUES ('Config Test 1')
		SET @configId1 = @@IDENTITY
		DECLARE @configId2 int
		INSERT INTO dbo.Configuration(configuration_name) VALUES ('Config Test 2')
		SET @configId2 = @@IDENTITY
		
		--Execute
		DECLARE @count int
		SELECT @count = total_entries FROM dbo.GetAllConfigurations (1,10)

		--Test
		IF (@count < 2)
			THROW 55005, 'ConfigurationTest.GetAllConfigurationsTest() FAILED',1

		IF NOT EXISTS(
			SELECT * FROM dbo.GetAllConfigurations(1, @count)
			WHERE configuration_id = @configId1 AND configuration_name = 'Config Test 1'
		)THROW 55005, 'ConfigurationTest.GetAllConfigurationsTest() FAILED',1

		IF NOT EXISTS(
			SELECT * FROM dbo.GetAllConfigurations(1, @count)
			WHERE configuration_id = @configId2 AND configuration_name = 'Config Test 2'
		)THROW 55005, 'ConfigurationTest.GetAllConfigurationsTest() FAILED',1

		ROLLBACK;
		PRINT 'ConfigurationTest.GetAllConfigurationsTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.GetConfigurationTest
AS
	SET NOCOUNT ON
	BEGIN TRAN
	BEGIN TRY
		--Prepare
		DECLARE @configId1 int
		INSERT INTO dbo.Configuration(configuration_name) VALUES ('Config Test 1')
		SET @configId1 = @@IDENTITY
		
		--Execute & Test
		IF NOT EXISTS(
			SELECT configuration_id, configuration_name FROM dbo.GetConfiguration (@configId1)
			WHERE configuration_id = @configId1 AND configuration_name = 'Config Test 1'
		)	THROW 55005, 'ConfigurationTest.GetConfigurationTest() FAILED',1

		ROLLBACK;
		PRINT 'ConfigurationTest.GetConfigurationTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO