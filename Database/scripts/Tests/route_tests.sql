USE db_waste_management
GO

CREATE PROC dbo.CreateRouteTest
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
		DECLARE @routeId int
		EXEC dbo.CreateRoute @stationId, @stationId, @routeId OUT

		--Test
		/*A route, when its created, should always be active*/
		IF NOT EXISTS(
			SELECT route_id FROM dbo.ActiveRoutes
			WHERE route_id = @routeId AND start_point = @stationId AND finish_point = @stationId
		)	THROW 55005, 'RouteTest.CreateRouteTest() FAILED', 1


		ROLLBACK
		PRINT 'RouteTest.CreateRouteTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.UpdateRouteTest
AS
	SET NOCOUNT ON
	BEGIN TRAN
	BEGIN TRY
		--Prepare
		DECLARE @stationId int
		INSERT INTO dbo.Station(station_name,latitude,longitude,station_type)
		VALUES ('test_station_name', 0, 0, 'base')
		SET @stationId = @@IDENTITY

		DECLARE @stationId2 int
		INSERT INTO dbo.Station(station_name,latitude,longitude,station_type)
		VALUES ('test_station_name_2', 0.1, 0.1, 'base')
		SET @stationId2 = @@IDENTITY

		DECLARE @routeId int
		INSERT INTO dbo.Route(start_point, finish_point)
		VALUES(@stationId, @stationId)
		SET @routeId = @@IDENTITY

		--Execute
		
		EXEC dbo.UpdateRoute @routeId, @stationId, @stationId2

		--Test
		IF NOT EXISTS(
			SELECT route_id FROM dbo.Route
			WHERE route_id = @routeId AND finish_point = @stationId2
		)	THROW 55005, 'RouteTest.UpdateRouteTest() FAILED', 1


		ROLLBACK
		PRINT 'RouteTest.UpdateRouteTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.ActivateRouteTest
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
		INSERT INTO dbo.Route(active, start_point, finish_point)
		VALUES('F', @stationId, @stationId)
		SET @routeId = @@IDENTITY

		--Execute
		
		EXEC dbo.ActivateRoute @routeId

		--Test
		
		IF NOT EXISTS(
			SELECT route_id FROM dbo.ActiveRoutes
			WHERE route_id = @routeId
		)	THROW 55005, 'RouteTest.ActivateRouteTest() FAILED', 1


		ROLLBACK
		PRINT 'RouteTest.ActivateRouteTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.DeactivateRouteTest
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

		--Execute
		
		EXEC dbo.DeactivateRoute @routeId

		--Test
		
		IF EXISTS(
			SELECT route_id FROM dbo.ActiveRoutes
			WHERE route_id = @routeId
		)	THROW 55005, 'RouteTest.DeactivateRouteTest() FAILED', 1

		IF NOT EXISTS(
			SELECT route_id FROM dbo.Route
			WHERE route_id = @routeId
		)	THROW 55005, 'RouteTest.DeactivateRouteTest() FAILED', 1

		ROLLBACK
		PRINT 'RouteTest.DeactivateRouteTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.GetAllRoutesTest
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

		DECLARE @routeId2 int
		INSERT INTO dbo.Route(start_point, finish_point)
		VALUES(@stationId, @stationId)
		SET @routeId2 = @@IDENTITY

		--Execute & Test
		DECLARE @entries int
		SELECT @entries =  total_entries FROM dbo.GetAllRoutes(1,10)
		IF (@entries < 2)
			THROW 55005, 'RouteTest.GetAllRoutesTest() FAILED', 1

		IF NOT EXISTS(
			SELECT * FROM dbo.GetAllRoutes(1,@entries)
			WHERE route_id = @routeId
		)THROW 55005, 'RouteTest.GetAllRoutesTest() FAILED', 1

		IF NOT EXISTS(
			SELECT * FROM dbo.GetAllRoutes(1,@entries)
			WHERE route_id = @routeId2
		)THROW 55005, 'RouteTest.GetAllRoutesTest() FAILED', 1

		ROLLBACK
		PRINT 'RouteTest.GetAllRoutesTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.GetAllActiveRoutesTest
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

		DECLARE @routeId2 int
		INSERT INTO dbo.Route(active, start_point, finish_point)
		VALUES('F', @stationId, @stationId)
		SET @routeId2 = @@IDENTITY

		--Execute & Test
		DECLARE @entries int
		SELECT @entries =  total_entries FROM dbo.GetAllActiveRoutes(1,10)
		IF (@entries < 2)
			THROW 55005, 'RouteTest.GetAllActiveRoutesTest() FAILED', 1

		IF NOT EXISTS(
			SELECT * FROM dbo.GetAllActiveRoutes(1,@entries)
			WHERE route_id = @routeId
		)THROW 55005, 'RouteTest.GetAllActiveRoutesTest() FAILED', 1

		IF EXISTS(
			SELECT * FROM dbo.GetAllActiveRoutes(1,@entries)
			WHERE route_id = @routeId2
		)THROW 55005, 'RouteTest.GetAllActiveRoutesTest() FAILED', 1

		ROLLBACK
		PRINT 'RouteTest.GetAllActiveRoutesTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.GetRouteStatisticsTest
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

		DECLARE @collectZoneId int
		INSERT INTO dbo.CollectZone(route_id,pick_order)
		VALUES(@routeId1,1)
		SET @collectZoneId = @@IDENTITY

		DECLARE @configId1 int
		INSERT INTO dbo.Configuration(configuration_name) VALUES ('Config Test 1')
		SET @configId1 = @@IDENTITY

		DECLARE @containerId1 int
		INSERT INTO dbo.Container
			(iot_id, latitude, longitude, height, container_type, collect_zone_id, configuration_id)
		VALUES
			('THIS IS A TEST', 0, 0, 1, 'general', @collectZoneId, @configId1)
		SET @containerId1 = @@IDENTITY

		DECLARE @containerId2 int
		INSERT INTO dbo.Container
			(iot_id, latitude, longitude, height, container_type, collect_zone_id, configuration_id)
		VALUES
			('THIS IS A TEST 2', -1, -1, 1, 'general', @collectZoneId, @configId1)
		SET @containerId2 = @@IDENTITY
		
		--Execute
		DECLARE @numCollectZones int, @numCollects int, @numContainers int, @routeIdSelected int
		SELECT 
			@routeIdSelected =  route_id, @numCollectZones = num_collect_zones, 
			@numContainers =  num_containers, @numCollects = num_collects 
		FROM dbo.GetRouteStatistics(@routeId1)

		--Test
		IF (@routeIdSelected <> @routeId1)
			THROW 55005, 'RouteTest.GetRouteStatisticsTest() FAILED', 1

		IF (@numCollectZones <> 1)
			THROW 55005, 'RouteTest.GetRouteStatisticsTest() FAILED', 1

		IF (@numContainers <> 2)
			THROW 55005, 'RouteTest.GetRouteStatisticsTest() FAILED', 1

		IF (@numCollects <> 0)
			THROW 55005, 'RouteTest.GetRouteStatisticsTest() FAILED', 1

		ROLLBACK
		PRINT 'RouteTest.GetRouteStatisticsTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH	
GO

CREATE PROC dbo.GetRouteInfoTest
AS
	SET NOCOUNT ON
	BEGIN TRAN
	BEGIN TRY
		--Prepare
		DECLARE @stationId int, @stationName nvarchar(100)
		SET @stationName = 'test_station_name'
		INSERT INTO dbo.Station(station_name,latitude,longitude,station_type)
		VALUES (@stationName, 0, 0, 'base')
		SET @stationId = @@IDENTITY

		DECLARE @routeId int
		INSERT INTO dbo.Route(start_point, finish_point)
		VALUES(@stationId, @stationId)
		SET @routeId = @@IDENTITY

		--Execute & Test
		
		IF (
			(SELECT start_point_station_name FROM dbo.GetRouteInfo (@routeId))
			<> @stationName
		)	THROW 55005, 'RouteTest.GetRouteInfoTest() FAILED', 1

		IF (
			(SELECT finish_point_station_name FROM dbo.GetRouteInfo (@routeId))
			<> @stationName
		)	THROW 55005, 'RouteTest.GetRouteInfoTest() FAILED', 1

		IF (
			(SELECT start_point_latitude FROM dbo.GetRouteInfo (@routeId))
			<> 0
		)	THROW 55005, 'RouteTest.GetRouteInfoTest() FAILED', 1

		IF (
			(SELECT finish_point_longitude FROM dbo.GetRouteInfo (@routeId))
			<> 0
		)	THROW 55005, 'RouteTest.GetRouteInfoTest() FAILED', 1

		ROLLBACK
		PRINT 'RouteTest.GetRouteInfoTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.GetCollectableRoutesTest
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

		DECLARE @routeId2 int
		INSERT INTO dbo.Route(start_point, finish_point)
		VALUES(@stationId, @stationId)
		SET @routeId2 = @@IDENTITY

		DECLARE @truckPlate1 nvarchar(8)
		SET @truckPlate1 = 'AB-CD-EF'
		INSERT INTO dbo.Truck (registration_plate) VALUES (@truckPlate1)

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

		INSERT INTO dbo.ConfigurationCommunication(configuration_id, communication_id, value)
		SELECT @configId1, communication_id, 70 FROM dbo.Communication
		WHERE communication_designation = 'max_threshold'

		DECLARE @containerId1 int
		INSERT INTO dbo.Container
			(iot_id, latitude, longitude, height, occupation, container_type, collect_zone_id, configuration_id)
		VALUES
			('THIS IS A TEST', 0, 0, 1, 85, 'general', @collectZoneId, @configId1)
		SET @containerId1 = @@IDENTITY
		
		--Execute & Test
		DECLARE @entries int
		SELECT @entries =  total_entries FROM dbo.GetCollectableRoutes(1,10,'general')
		IF (@entries < 1)
			THROW 55005, 'RouteTest.GetCollectableRoutesTest() FAILED', 1

		IF NOT EXISTS(
			SELECT * FROM dbo.GetCollectableRoutes(1,@entries,'general')
			WHERE route_id = @routeId2
		) THROW 55005, 'RouteTest.GetCollectableRoutesTest() FAILED', 1
		
		ROLLBACK;
		PRINT 'RouteTest.GetCollectableRoutesTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO