USE db_waste_management
GO

CREATE PROC dbo.CreateTruckTest
AS
	SET NOCOUNT ON
	BEGIN TRAN
	BEGIN TRY
		--Prepare
		DECLARE @truckPlate nvarchar(8)
		SET @truckPlate = 'AB-CD-EF'

		--Execute
		EXEC dbo.CreateTruck @truckPlate

		--Test
		IF NOT EXISTS(
			SELECT registration_plate, active FROM dbo.Truck
			WHERE registration_plate = @truckPlate
		)
		THROW 55005, '@TruckTest.CreateTruckTest() FAILED', 1

		ROLLBACK;
		PRINT 'TruckTest.CreateTruckTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.ActivateTruckTest
AS
	SET NOCOUNT ON
	BEGIN TRAN
	BEGIN TRY
		--Prepare
		DECLARE @truckPlate nvarchar(8)
		SET @truckPlate = 'AB-CD-EF'

		INSERT INTO dbo.Truck(registration_plate,active)
		VALUES(@truckPlate, 'F')

		--Execute
		EXEC dbo.ActivateTruck @truckPlate

		--Test
		IF NOT EXISTS(
			SELECT registration_plate, active FROM dbo.Truck
			WHERE registration_plate = @truckPlate AND active = 'T' 
		)
		THROW 55005, '@TruckTest.ActivateTruckTest() FAILED', 1

		ROLLBACK;
		PRINT 'TruckTest.ActivateTruckTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.DeactivateTruckTest
AS
	SET NOCOUNT ON
	BEGIN TRAN
	BEGIN TRY
		--Prepare
		DECLARE @truckPlate nvarchar(8)
		SET @truckPlate = 'AB-CD-EF'

		INSERT INTO dbo.Truck(registration_plate,active)
		VALUES(@truckPlate, 'T')

		--Execute
		EXEC dbo.DeactivateTruck @truckPlate

		--Test
		IF NOT EXISTS(
			SELECT registration_plate, active FROM dbo.Truck
			WHERE registration_plate = @truckPlate AND active = 'F' 
		)
		THROW 55005, '@TruckTest.DeactivateTruckTest() FAILED', 1

		ROLLBACK;
		PRINT 'TruckTest.DeactivateTruckTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.GetAllTrucksTest
AS
	SET NOCOUNT ON
	SET TRAN ISOLATION LEVEL SERIALIZABLE --Stop insertions to Truck table
	BEGIN TRAN
	BEGIN TRY
		--Prepare
		DECLARE @truckPlate nvarchar(8)
		SET @truckPlate = 'AB-CD-EF'

		INSERT INTO dbo.Truck(registration_plate,active)
		VALUES(@truckPlate, 'F')

		--Execute & Test
		DECLARE @totalEntries int
		SELECT @totalEntries = total_entries FROM dbo.GetAllTrucks(1,10)
		IF(@totalEntries < 1)
			THROW 55005, '@TruckTest.GetAllTrucksTest() FAILED', 1

		IF NOT EXISTS(
			SELECT registration_plate, active FROM dbo.GetAllTrucks(1, @totalEntries)
			WHERE registration_plate = @truckPlate
		)
		THROW 55005, '@TruckTest.GetAllTrucksTest() FAILED', 1

		ROLLBACK;
		PRINT 'TruckTest.GetAllTrucksTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.GetAllActiveTrucksTest
AS
	SET NOCOUNT ON
	SET TRAN ISOLATION LEVEL SERIALIZABLE --Stop insertions to Truck table
	BEGIN TRAN
	BEGIN TRY
		--Prepare
		DECLARE @truckPlate nvarchar(8)
		SET @truckPlate = 'AB-CD-EF'

		INSERT INTO dbo.Truck(registration_plate,active)
		VALUES(@truckPlate, 'F')

		DECLARE @truckPlate2 nvarchar(8)
		SET @truckPlate2 = 'AA-BB-CC'
		INSERT INTO dbo.Truck(registration_plate,active)
		VALUES(@truckPlate2, 'T')

		--Execute & Test
		DECLARE @totalEntries int
		SELECT @totalEntries = total_entries FROM dbo.GetAllActiveTrucks(1,10)
		IF(@totalEntries < 1)
			THROW 55005, '@TruckTest.GetAllActiveTrucksTest() FAILED', 1

		IF EXISTS(
			SELECT registration_plate, active FROM dbo.GetAllActiveTrucks(1, @totalEntries)
			WHERE registration_plate = @truckPlate
		)
		THROW 55005, '@TruckTest.GetAllActiveTrucksTest() FAILED', 1

		IF NOT EXISTS(
			SELECT registration_plate, active FROM dbo.GetAllActiveTrucks(1, @totalEntries)
			WHERE registration_plate = @truckPlate2
		)
		THROW 55005, '@TruckTest.GetAllActiveTrucksTest() FAILED', 1

		ROLLBACK;
		PRINT 'TruckTest.GetAllActiveTrucksTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.GetTruckTest
AS
	SET NOCOUNT ON
	BEGIN TRAN
	BEGIN TRY
		--Prepare
		DECLARE @truckPlate nvarchar(8)
		SET @truckPlate = 'AB-CD-EF'

		INSERT INTO dbo.Truck(registration_plate,active)
		VALUES(@truckPlate, 'F')

		--Execute & Test
		IF NOT EXISTS(
			SELECT registration_plate, active FROM dbo.GetTruck(@truckPlate)
		)
		THROW 55005, '@TruckTest.GetTruckTest() FAILED', 1

		ROLLBACK;
		PRINT 'TruckTest.GetTruckTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO