USE db_waste_management
GO

CREATE PROC dbo.CreateRouteCollectionTest
AS
	SET NOCOUNT ON 
	BEGIN TRAN
	BEGIN TRY
		--Prepare
		DECLARE @stationId int
		INSERT INTO dbo.Station(station_name,latitude,longitude,station_type)
		VALUES ('test_station_name', 0, 0, 'base')
		SET @stationId = @@IDENTITY

		DECLARE @routeId1 int
		INSERT INTO dbo.Route(start_point, finish_point)
		VALUES(@stationId, @stationId)
		SET @routeId1 = @@IDENTITY

		DECLARE @truckPlate nvarchar(8)
		SET @truckPlate = 'AB-CD-EF'
		INSERT INTO dbo.Truck (registration_plate) VALUES (@truckPlate)
		
		--Execute
		DECLARE @startDate datetime
		SET @startDate = GETDATE()

		EXEC dbo.CreateRouteCollection @routeId1, @truckPlate, @startDate

		--Test

		IF NOT EXISTS (
			SELECT route_id, truck_plate, start_date FROM dbo.RouteCollection
			WHERE route_id = @routeId1 AND truck_plate = @truckPlate AND start_date = @startDate
		)	THROW 55005, 'RouteCollectionTest.CreateRouteColectionTest() FAILED', 1

		ROLLBACK;
		PRINT 'RouteCollectionTest.CreateRouteColectionTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.CollectRouteTest
AS
	SET NOCOUNT ON 
	BEGIN TRAN
	BEGIN TRY
		--Prepare
		DECLARE @stationId int
		INSERT INTO dbo.Station(station_name,latitude,longitude,station_type)
		VALUES ('test_station_name', 38.706747, -9.375440, 'base')
		SET @stationId = @@IDENTITY

		DECLARE @routeId1 int
		INSERT INTO dbo.Route(start_point, finish_point)
		VALUES(@stationId, @stationId)
		SET @routeId1 = @@IDENTITY

		DECLARE @routeId2 int
		INSERT INTO dbo.Route(start_point, finish_point)
		VALUES(@stationId, @stationId)
		SET @routeId2 = @@IDENTITY

		DECLARE @truckPlate1 nvarchar(8)
		SET @truckPlate1 = 'AB-CD-EF'
		INSERT INTO dbo.Truck (registration_plate) VALUES (@truckPlate1)
		
		DECLARE @truckPlate2 nvarchar(8)
		SET @truckPlate2 = 'AB-CD-GH'
		INSERT INTO dbo.Truck (registration_plate) VALUES (@truckPlate2)

		DECLARE @startDate1 datetime
		SET @startDate1 = GETDATE()
		INSERT INTO dbo.RouteCollection (route_id, truck_plate, start_date)
		VALUES (@routeId1, @truckPlate1, @startDate1)

		DECLARE @collectZoneId int
		INSERT INTO dbo.CollectZone(route_id,pick_order)
		VALUES(@routeId2,1)
		SET @collectZoneId = @@IDENTITY

		DECLARE @configId1 int
		INSERT INTO dbo.Configuration(configuration_name) VALUES ('Config Test 1')
		SET @configId1 = @@IDENTITY

		DECLARE @commId int
		SELECT @commId = communication_id FROM dbo.Communication
		WHERE communication_designation = 'max_threshold'

		INSERT INTO dbo.ConfigurationCommunication (configuration_id, communication_id, value)
		VALUES(@configId1, @commId, 70)

		DECLARE @containerId1 int
		INSERT INTO dbo.Container
			(iot_id, latitude, longitude, height, occupation, container_type, collect_zone_id, configuration_id)
		VALUES
			('THIS IS A TEST', 0, 0, 1, 99, 'general', @collectZoneId, @configId1)
		SET @containerId1 = @@IDENTITY
		
		--Execute
		DECLARE @startDate2 datetime, @latitude decimal(9,6), @longitude decimal(9,6), @collectedRouteId int
		SET @startDate2 = GETDATE()
		SET @latitude = 38.706586
		SET @longitude = -9.375555
		
		EXEC dbo.CollectRoute @latitude, @longitude, @truckPlate2, @startDate2, 'general', @collectedRouteId OUT

		--Test
		/*The route that should be selected to be collected is the second one, since the first
		is already being collected*/
		IF (@collectedRouteId <> @routeId2)
			THROW 55005, 'RouteCollectionTest.CollectRouteTest() FAILED', 1
		
		ROLLBACK;
		PRINT 'RouteCollectionTest.CollectRouteTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO


CREATE PROC dbo.CollectNearestRouteTest
AS
	SET NOCOUNT ON 
	BEGIN TRAN
	BEGIN TRY
		--Prepare
		DECLARE @stationId int
		INSERT INTO dbo.Station(station_name,latitude,longitude,station_type)
		VALUES ('test_station_name', 38.706747, -9.375440, 'base')
		SET @stationId = @@IDENTITY

		DECLARE @stationId2 int
		INSERT INTO dbo.Station(station_name,latitude,longitude,station_type)
		VALUES ('test_station_name_2', 38.707175, -9.376202, 'base')
		SET @stationId2 = @@IDENTITY

		DECLARE @routeId1 int
		INSERT INTO dbo.Route(start_point, finish_point)
		VALUES(@stationId, @stationId)
		SET @routeId1 = @@IDENTITY

		DECLARE @routeId2 int
		INSERT INTO dbo.Route(start_point, finish_point)
		VALUES(@stationId, @stationId2)
		SET @routeId2 = @@IDENTITY

		DECLARE @truckPlate1 nvarchar(8)
		SET @truckPlate1 = 'AB-CD-EF'
		INSERT INTO dbo.Truck (registration_plate) VALUES (@truckPlate1)

		DECLARE @collectZoneId int
		INSERT INTO dbo.CollectZone(route_id,pick_order)
		VALUES(@routeId1,1)
		SET @collectZoneId = @@IDENTITY

		DECLARE @configId1 int
		INSERT INTO dbo.Configuration(configuration_name) VALUES ('Config Test 1')
		SET @configId1 = @@IDENTITY

		DECLARE @commId int
		SELECT @commId = communication_id FROM dbo.Communication
		WHERE communication_designation = 'max_threshold'

		INSERT INTO dbo.ConfigurationCommunication (configuration_id, communication_id, value)
		VALUES(@configId1, @commId, 70)

		DECLARE @containerId1 int
		INSERT INTO dbo.Container
			(iot_id, latitude, longitude, height, occupation, container_type, collect_zone_id, configuration_id)
		VALUES
			('THIS IS A TEST', 0, 0, 1, 99, 'general', @collectZoneId, @configId1)
		SET @containerId1 = @@IDENTITY
		
		--Execute
		DECLARE @startDate2 datetime, @latitude decimal(9,6), @longitude decimal(9,6), @collectedRouteId int
		SET @startDate2 = GETDATE()
		SET @latitude = 38.706586
		SET @longitude = -9.375555
		
		EXEC dbo.CollectRoute @latitude, @longitude, @truckPlate1, @startDate2, 'general', @collectedRouteId OUT

		--Test
		/*The route that should be selected to be collected is the first one, since only ~20m of the current
		user location, while the second is ~100m*/
		IF (@collectedRouteId <> @routeId1)
			THROW 55005, 'RouteCollectionTest.CollectNearestRouteTest() FAILED', 1
		
		ROLLBACK;
		PRINT 'RouteCollectionTest.CollectNearestRouteTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.CollectWithOccupiedTruck
AS
	SET NOCOUNT ON 
	BEGIN TRAN
	BEGIN TRY
		--Prepare
		DECLARE @stationId int
		INSERT INTO dbo.Station(station_name,latitude,longitude,station_type)
		VALUES ('test_station_name', 38.706747, -9.375440, 'base')
		SET @stationId = @@IDENTITY

		DECLARE @routeId1 int
		INSERT INTO dbo.Route(start_point, finish_point)
		VALUES(@stationId, @stationId)
		SET @routeId1 = @@IDENTITY

		DECLARE @routeId2 int
		INSERT INTO dbo.Route(start_point, finish_point)
		VALUES(@stationId, @stationId)
		SET @routeId2 = @@IDENTITY

		DECLARE @truckPlate1 nvarchar(8)
		SET @truckPlate1 = 'AB-CD-EF'
		INSERT INTO dbo.Truck (registration_plate) VALUES (@truckPlate1)
		
		DECLARE @truckPlate2 nvarchar(8)
		SET @truckPlate2 = 'AB-CD-GH'
		INSERT INTO dbo.Truck (registration_plate) VALUES (@truckPlate2)


		DECLARE @startDate1 datetime
		SET @startDate1 = GETDATE()
		INSERT INTO dbo.RouteCollection (route_id, truck_plate, start_date)
		VALUES (@routeId1, @truckPlate1, @startDate1)
		
		DECLARE @collectZoneId int
		INSERT INTO dbo.CollectZone(route_id,pick_order)
		VALUES(@routeId2,1)
		SET @collectZoneId = @@IDENTITY

		DECLARE @configId1 int
		INSERT INTO dbo.Configuration(configuration_name) VALUES ('Config Test 1')
		SET @configId1 = @@IDENTITY

		DECLARE @commId int
		SELECT @commId = communication_id FROM dbo.Communication
		WHERE communication_designation = 'max_threshold'

		INSERT INTO dbo.ConfigurationCommunication (configuration_id, communication_id, value)
		VALUES(@configId1, @commId, 70)

		DECLARE @containerId1 int
		INSERT INTO dbo.Container
			(iot_id, latitude, longitude, height, occupation, container_type, collect_zone_id, configuration_id)
		VALUES
			('THIS IS A TEST', 0, 0, 1, 99, 'general', @collectZoneId, @configId1)
		SET @containerId1 = @@IDENTITY
		
		--Execute
		DECLARE @startDate2 datetime, @latitude decimal(9,6), @longitude decimal(9,6), @collectedRouteId int
		SET @startDate2 = GETDATE()
		SET @latitude = 38.706586
		SET @longitude = -9.375555
		
		--Execute & Test
		/*Since the truck with registration plate @truckPlate1, is already collecting a route,
		this operation should throw an error*/
		EXEC dbo.CollectRoute @latitude, @longitude, @truckPlate1, @startDate2, 'general', @collectedRouteId OUT;
		
		ROLLBACK;
		PRINT 'RouteCollectionTest.CollectWithOccupiedTruck() FAILED'
	END TRY
	BEGIN CATCH
		PRINT 'RouteCollectionTest.CollectRouteTest() SUCCEED'
	END CATCH
GO

CREATE PROC dbo.UpdateRouteCollectionTest
AS
	SET NOCOUNT ON 
	BEGIN TRAN
	BEGIN TRY
		--Prepare
		DECLARE @stationId int
		INSERT INTO dbo.Station(station_name,latitude,longitude,station_type)
		VALUES ('test_station_name', 0, 0, 'base')
		SET @stationId = @@IDENTITY

		DECLARE @routeId1 int
		INSERT INTO dbo.Route(start_point, finish_point)
		VALUES(@stationId, @stationId)
		SET @routeId1 = @@IDENTITY

		DECLARE @truckPlate nvarchar(8)
		SET @truckPlate = 'AB-CD-EF'
		INSERT INTO dbo.Truck (registration_plate) VALUES (@truckPlate)

		DECLARE @startDate datetime
		SET @startDate = GETDATE()
		INSERT INTO dbo.RouteCollection (route_id, truck_plate, start_date)
		VALUES (@routeId1, @truckPlate, @startDate)
		
		--Execute
		DECLARE @finishDate datetime
		SET @startDate = GETDATE()

		EXEC dbo.UpdateRouteCollection @routeId1, @startDate, @finishDate, @truckPlate

		--Test
		IF NOT EXISTS (
			SELECT route_id, truck_plate, start_date FROM dbo.RouteCollection
			WHERE route_id = @routeId1 AND truck_plate = @truckPlate AND start_date = @startDate
		)	THROW 55005, 'RouteCollectionTest.UpdateRouteCollectionTest() FAILED', 1

		ROLLBACK;
		PRINT 'RouteCollectionTest.UpdateRouteCollectionTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.GetRouteCollectionsTest
AS
	SET NOCOUNT ON 
	BEGIN TRAN
	BEGIN TRY
		--Prepare
		DECLARE @stationId int
		INSERT INTO dbo.Station(station_name,latitude,longitude,station_type)
		VALUES ('test_station_name', 0, 0, 'base')
		SET @stationId = @@IDENTITY

		DECLARE @routeId1 int
		INSERT INTO dbo.Route(start_point, finish_point)
		VALUES(@stationId, @stationId)
		SET @routeId1 = @@IDENTITY

		DECLARE @truckPlate nvarchar(8)
		SET @truckPlate = 'AB-CD-EF'
		INSERT INTO dbo.Truck (registration_plate) VALUES (@truckPlate)

		DECLARE @startDate datetime
		SET @startDate = GETDATE()
		INSERT INTO dbo.RouteCollection (route_id, truck_plate, start_date)
		VALUES (@routeId1, @truckPlate, @startDate)
		
		--Execute & Test
		IF NOT EXISTS(
			SELECT total_entries, route_id, start_date,finish_date,truck_plate 
			FROM dbo.GetRouteCollections(1, 10, @routeId1)
		)	THROW 55005, 'RouteCollectionTest.GetRouteCollectionsTest() FAILED', 1

		IF ((SELECT total_entries FROM dbo.GetRouteCollections(1, 10, @routeId1)) <> 1)	
			THROW 55005, 'RouteCollectionTest.GetRouteCollectionsTest() FAILED', 1

		IF ((SELECT route_id FROM dbo.GetRouteCollections(1, 10, @routeId1)) <> @routeId1)	
			THROW 55005, 'RouteCollectionTest.GetRouteCollectionsTest() FAILED', 1

		ROLLBACK;
		PRINT 'RouteCollectionTest.GetRouteCollectionsTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.GetRouteCollectionTest
AS
	SET NOCOUNT ON 
	BEGIN TRAN
	BEGIN TRY
		--Prepare
		DECLARE @stationId int
		INSERT INTO dbo.Station(station_name,latitude,longitude,station_type)
		VALUES ('test_station_name', 0, 0, 'base')
		SET @stationId = @@IDENTITY

		DECLARE @routeId1 int
		INSERT INTO dbo.Route(start_point, finish_point)
		VALUES(@stationId, @stationId)
		SET @routeId1 = @@IDENTITY

		DECLARE @truckPlate nvarchar(8)
		SET @truckPlate = 'AB-CD-EF'
		INSERT INTO dbo.Truck (registration_plate) VALUES (@truckPlate)

		DECLARE @startDate datetime
		SET @startDate = GETDATE()
		INSERT INTO dbo.RouteCollection (route_id, truck_plate, start_date)
		VALUES (@routeId1, @truckPlate, @startDate)
		
		--Execute & Test
		IF NOT EXISTS(
			SELECT route_id, start_date,finish_date,truck_plate 
			FROM dbo.GetRouteCollection(@routeId1, @startDate)
		)	THROW 55005, 'RouteCollectionTest.GetRouteCollectionTest() FAILED', 1

		ROLLBACK;
		PRINT 'RouteCollectionTest.GetRouteCollectionTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.GetTruckCollectionsTest
AS
	SET NOCOUNT ON 
	BEGIN TRAN
	BEGIN TRY
		--Prepare
		DECLARE @stationId int
		INSERT INTO dbo.Station(station_name,latitude,longitude,station_type)
		VALUES ('test_station_name', 0, 0, 'base')
		SET @stationId = @@IDENTITY

		DECLARE @routeId1 int
		INSERT INTO dbo.Route(start_point, finish_point)
		VALUES(@stationId, @stationId)
		SET @routeId1 = @@IDENTITY

		DECLARE @truckPlate nvarchar(8)
		SET @truckPlate = 'AB-CD-EF'
		INSERT INTO dbo.Truck (registration_plate) VALUES (@truckPlate)

		DECLARE @startDate datetime
		SET @startDate = GETDATE()
		INSERT INTO dbo.RouteCollection (route_id, truck_plate, start_date)
		VALUES (@routeId1, @truckPlate, @startDate)
		
		--Execute & Test
		IF NOT EXISTS(
			SELECT total_entries, route_id, start_date,finish_date,truck_plate 
			FROM dbo.GetTruckCollections(1, 10, @truckPlate)
		)	THROW 55005, 'RouteCollectionTest.GetTruckCollectionsTest() FAILED', 1

		IF ((SELECT total_entries FROM dbo.GetTruckCollections(1, 10, @truckPlate)) <> 1)	
			THROW 55005, 'RouteCollectionTest.GetTruckCollectionsTest() FAILED', 1

		ROLLBACK;
		PRINT 'RouteCollectionTest.GetTruckCollectionsTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO