USE db_waste_management
GO

CREATE PROC dbo.CreateStationTest
AS
	SET NOCOUNT ON
	BEGIN TRAN
	BEGIN TRY
		--Execute
		DECLARE @stationId int
		EXEC dbo.CreateStation 'test_station_name', 0, 0, 'base', @stationId OUT
		
		--Test
		IF NOT EXISTS(
			SELECT station_id FROM dbo.Station
			WHERE station_id = @stationId AND station_name = 'test_station_name' AND station_type = 'base'
		)	THROW 55005, 'StationTest.CreateStationTest() FAILED', 1

		ROLLBACK;
		PRINT 'StationTest.CreateStationTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.UpdateStationTest
AS
	SET NOCOUNT ON
	BEGIN TRAN
	BEGIN TRY
		--Prepare
		DECLARE @stationId int
		INSERT INTO dbo.Station(station_name,latitude,longitude,station_type)
		VALUES ('test_station_name', 0, 0, 'drop_zone')
		SET @stationId = @@IDENTITY

		INSERT INTO dbo.DropZone (drop_zone_id)
		VALUES (@stationId)

		--Execute
		DECLARE @newName nvarchar(100)
		SET @newName ='new_test_station_name'
		EXEC dbo.UpdateStation @stationId, @newName, 0, 0, 'base'

		--Test
		
		/*Entry on Drop Zone should be eliminated*/
		IF EXISTS(
			SELECT drop_zone_id FROM dbo.DropZone
			WHERE drop_zone_id = @stationId
		)	THROW 55005, 'StationTest.UpdateStationTest() FAILED', 1

		IF NOT EXISTS(
			SELECT station_id FROM dbo.Station
			WHERE station_id = @stationId AND station_name = @newName AND station_type = 'base'
		)	THROW 55005, 'StationTest.UpdateStationTest() FAILED', 1


		ROLLBACK
		PRINT 'StationTest.UpdateStationTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.DeleteStationTest
AS
	SET NOCOUNT ON
	BEGIN TRAN
	BEGIN TRY
		--Prepare
		DECLARE @stationId int
		INSERT INTO dbo.Station(station_name,latitude,longitude,station_type)
		VALUES ('test_station_name', 0, 0, 'base')
		SET @stationId = @@IDENTITY

		--Execute
		EXEC dbo.DeleteStation @stationId

		--Test
		IF EXISTS(
			SELECT station_id FROM dbo.Station
			WHERE station_id = @stationId
		)	THROW 55005, 'StationTest.DeleteStationTest() FAILED', 1


		ROLLBACK
		PRINT 'StationTest.DeleteStationTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.DeleteUsedStationTest
AS
	SET NOCOUNT ON
	BEGIN TRAN
	BEGIN TRY
		--Prepare
		DECLARE @stationId int
		INSERT INTO dbo.Station(station_name,latitude,longitude,station_type)
		VALUES ('test_station_name', 0, 0, 'base')
		SET @stationId = @@IDENTITY

		DECLARE @routeId int
		INSERT INTO dbo.Route(start_point, finish_point)
		VALUES(@stationId, @stationId)
		SET @routeId = @@IDENTITY

		--Execute & Test
		EXEC dbo.DeleteStation @stationId;
	
		ROLLBACK
		PRINT 'StationTest.DeleteUsedStationTest() FAILED'
	END TRY
	BEGIN CATCH
		PRINT 'StationTest.DeleteUsedStationTest() SUCCEED';
	END CATCH
GO

CREATE PROC dbo.GetAllStationsTest
AS
	SET NOCOUNT ON
	BEGIN TRAN
	BEGIN TRY
		--Prepare
		DECLARE @stationId1 int
		INSERT INTO dbo.Station(station_name,latitude,longitude,station_type)
		VALUES ('test_station_name', 0, 0, 'base')
		SET @stationId1 = @@IDENTITY

		DECLARE @stationId2 int
		INSERT INTO dbo.Station(station_name,latitude,longitude,station_type)
		VALUES ('test_station_name_2', 0.1, 0.1, 'base')
		SET @stationId2 = @@IDENTITY

		--Execute & Test
		DECLARE @entries int
		SELECT @entries = total_entries FROM dbo.GetAllStations(1,10)
		IF(@entries < 1)
			THROW 55005, 'StationTest.GetAllStationsTest() FAILED', 1

		IF NOT EXISTS(
			SELECT * FROM dbo.GetAllStations(1, @entries)
			WHERE station_id = @stationId1 
		)	THROW 55005, 'StationTest.GetAllStationsTest() FAILED', 1

		IF NOT EXISTS(
			SELECT * FROM dbo.GetAllStations(1, @entries)
			WHERE station_id = @stationId2
		)	THROW 55005, 'StationTest.GetAllStationsTest() FAILED', 1

		ROLLBACK
		PRINT 'StationTest.GetAllStationsTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.GetStationInfoTest
AS
	SET NOCOUNT ON
	BEGIN TRAN
	BEGIN TRY
		--Prepare
		DECLARE @stationId int
		INSERT INTO dbo.Station(station_name,latitude,longitude,station_type)
		VALUES ('test_station_name', 0, 0, 'base')
		SET @stationId = @@IDENTITY

		--Execute & Test

		IF NOT EXISTS(
			SELECT * FROM dbo.GetStationInfo(@stationId)
			WHERE station_id=@stationId AND station_name = 'test_station_name' AND station_type = 'base'
		)	THROW 55005, 'StationTest.GetStationInfoTest() FAILED', 1

		ROLLBACK
		PRINT 'StationTest.GetStationInfoTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO