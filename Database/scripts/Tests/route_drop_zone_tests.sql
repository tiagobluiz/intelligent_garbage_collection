USE db_waste_management
GO

CREATE PROC dbo.CreateRouteDropZoneTest
AS
	SET NOCOUNT ON
	BEGIN TRAN
	BEGIN TRY
		--Prepare
		DECLARE @stationId int
		INSERT INTO dbo.Station(station_name,latitude,longitude,station_type)
		VALUES ('test_station_name', 0, 0, 'drop_zone')
		SET @stationId = @@IDENTITY
		INSERT INTO dbo.DropZone(drop_zone_id) VALUES (@stationId)

		DECLARE @routeId1 int
		INSERT INTO dbo.Route(start_point, finish_point)
		VALUES(@stationId, @stationId)
		SET @routeId1 = @@IDENTITY

		--Execute
		EXEC dbo.CreateRouteDropZone @routeId1, @stationId

		--Test
		IF NOT EXISTS(
			SELECT route_id, drop_zone_id FROM dbo.RouteDropZone
			WHERE route_id = @routeId1 AND drop_zone_id = @stationId
		)	THROW	 55005, 'RouteDropZoneTest.CreateRouteDropZoneTest() FAILED', 1

		ROLLBACK
		PRINT 'RouteDropZoneTest.CreateRouteDropZoneTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.DeleteRouteDropZoneTest
AS
	SET NOCOUNT ON
	BEGIN TRAN
	BEGIN TRY
		--Prepare
		DECLARE @stationId int
		INSERT INTO dbo.Station(station_name,latitude,longitude,station_type)
		VALUES ('test_station_name', 0, 0, 'drop_zone')
		SET @stationId = @@IDENTITY
		INSERT INTO dbo.DropZone(drop_zone_id) VALUES (@stationId)

		DECLARE @routeId1 int
		INSERT INTO dbo.Route(start_point, finish_point)
		VALUES(@stationId, @stationId)
		SET @routeId1 = @@IDENTITY

		INSERT INTO dbo.RouteDropZone (route_id, drop_zone_id)
		VALUES (@routeId1, @stationId)
		
		--Execute
		EXEC dbo.DeleteRouteDropZone @routeId1, @stationId

		--Test
		IF EXISTS(
			SELECT route_id, drop_zone_id FROM dbo.RouteDropZone
			WHERE route_id = @routeId1 AND drop_zone_id = @stationId
		)	THROW	 55005, 'RouteDropZoneTest.DeleteRouteDropZoneTest() FAILED', 1

		ROLLBACK
		PRINT 'RouteDropZoneTest.DeleteRouteDropZoneTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.GetRouteDropZonesTest
AS
	SET NOCOUNT ON
	BEGIN TRAN
	BEGIN TRY
		--Prepare
		DECLARE @stationId int
		INSERT INTO dbo.Station(station_name,latitude,longitude,station_type)
		VALUES ('test_station_name', 0, 0, 'drop_zone')
		SET @stationId = @@IDENTITY
		INSERT INTO dbo.DropZone(drop_zone_id) VALUES (@stationId)

		DECLARE @stationId2 int
		INSERT INTO dbo.Station(station_name,latitude,longitude,station_type)
		VALUES ('test_station_name2', 0, 0.1, 'drop_zone')
		SET @stationId2 = @@IDENTITY
		INSERT INTO dbo.DropZone(drop_zone_id) VALUES (@stationId2)

		DECLARE @routeId1 int
		INSERT INTO dbo.Route(start_point, finish_point)
		VALUES(@stationId, @stationId)
		SET @routeId1 = @@IDENTITY

		INSERT INTO dbo.RouteDropZone (route_id, drop_zone_id)
		VALUES (@routeId1, @stationId)
		INSERT INTO dbo.RouteDropZone (route_id, drop_zone_id)
		VALUES (@routeId1, @stationId2)
		
		--Execute & Test
		IF (
			(SELECT TOP 1 total_entries FROM dbo.GetRouteDropZones(1, 10, @routeId1))
			<> 2
		)	THROW 55005, 'RouteDropZoneTest.GetRouteDropZonesTest() FAILED', 1

		ROLLBACK
		PRINT 'RouteDropZoneTest.GetRouteDropZonesTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.GetRouteDropZoneTest
AS
	SET NOCOUNT ON
	BEGIN TRAN
	BEGIN TRY
		--Prepare
		DECLARE @stationId int
		INSERT INTO dbo.Station(station_name,latitude,longitude,station_type)
		VALUES ('test_station_name', 0, 0, 'drop_zone')
		SET @stationId = @@IDENTITY
		INSERT INTO dbo.DropZone(drop_zone_id) VALUES (@stationId)
		
		DECLARE @routeId1 int
		INSERT INTO dbo.Route(start_point, finish_point)
		VALUES(@stationId, @stationId)
		SET @routeId1 = @@IDENTITY

		INSERT INTO dbo.RouteDropZone (route_id, drop_zone_id)
		VALUES (@routeId1, @stationId)
		
		--Execute & Test
		IF NOT EXISTS (
			SELECT route_id, drop_zone_id FROM dbo.GetRouteDropZone(@routeId1, @stationId)
			WHERE route_id = @routeId1 AND drop_zone_id = @stationId
		)	THROW 55005, 'RouteDropZoneTest.GetRouteDropZoneTest() FAILED', 1

		ROLLBACK
		PRINT 'RouteDropZoneTest.GetRouteDropZoneTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO