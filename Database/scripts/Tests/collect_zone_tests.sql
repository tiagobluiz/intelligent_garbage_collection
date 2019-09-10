USE db_waste_management
GO

CREATE PROC dbo.CreateCollectZoneTest
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
		DECLARE @collectZoneId int
		EXEC dbo.CreateCollectZone @routeId, @collectZoneId OUT

		--Test
		IF NOT EXISTS(
			SELECT * FROM dbo.ActiveCollectZones --it is, by default, active
			WHERE collect_zone_id = @collectZoneId AND route_id = @routeId
		)THROW 55005, '@CollectZoneTest.CreateCollectZoneTest() FAILED', 1;
		
		ROLLBACK;
		PRINT '@CollectZoneTest.CreateCollectZoneTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.UpdateCollectZoneTest
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

		DECLARE @collectZoneId int
		INSERT INTO dbo.CollectZone(route_id,pick_order)
		VALUES(@routeId1,32767)
		SET @collectZoneId = @@IDENTITY

		--Execute
		EXEC dbo.UpdateCollectZone @collectZoneId,  @routeId2

		--Test
		IF NOT EXISTS(
			SELECT * FROM dbo.ActiveCollectZones --it is, by default, active
			WHERE collect_zone_id = @collectZoneId AND route_id = @routeId2
		)THROW 55005, '@CollectZoneTest.UpdateCollectZoneTest() FAILED', 1;

		ROLLBACK;
		PRINT '@CollectZoneTest.UpdateCollectZoneTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.DeactivateCollectZoneTest
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
		VALUES(@routeId1,32767)
		SET @collectZoneId = @@IDENTITY

		--Execute
		EXEC dbo.DeactivateCollectZone @collectZoneId

		--Test
		DECLARE @state char
		SELECT @state = active FROM dbo.CollectZone
		WHERE collect_zone_id = @collectZoneId
		
		IF(@state <> 'F')
		BEGIN
			PRINT 'DeactivateCollectZone did not put the state as F';
			THROW 55005, '@CollectZoneTest.DeactivateCollectZoneTest() FAILED', 1;
		END
		IF EXISTS(
			SELECT collect_zone_id FROM dbo.ActiveCollectZones
			WHERE collect_zone_id = @collectZoneId
		) BEGIN
			PRINT 'The deactivated Collect Zone still in ActiveCollectZones views';
			THROW 55005, '@CollectZoneTest.DeactivateCollectZoneTest() FAILED', 1;
		END
		ROLLBACK;
		PRINT '@CollectZoneTest.DeactivateCollectZoneTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.ActivateCollectZoneTest
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
		INSERT INTO dbo.CollectZone(route_id,pick_order, active)
		VALUES(@routeId1,32767, 'F')
		SET @collectZoneId = @@IDENTITY

		--Execute
		EXEC dbo.ActivateCollectZone @collectZoneId

		--Test
		DECLARE @state char
		SELECT @state = active FROM dbo.CollectZone
		WHERE collect_zone_id = @collectZoneId
		
		IF(@state <> 'T')
		BEGIN
			PRINT 'ActivateCollectZone did not put the state as T';
			THROW 55005, '@CollectZoneTest.ActivateCollectZoneTest() FAILED', 1;
		END
		IF NOT EXISTS(
			SELECT collect_zone_id FROM dbo.ActiveCollectZones
			WHERE collect_zone_id = @collectZoneId
		) BEGIN
			PRINT 'The activated Collect Zone is not in ActiveCollectZones views';
			THROW 55005, '@CollectZoneTest.ActivateCollectZoneTest() FAILED', 1;
		END
		ROLLBACK;
		PRINT '@CollectZoneTest.ActivateCollectZoneTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.GetRouteCollectZonesTest
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

		DECLARE @collectZoneId1 int
		INSERT INTO dbo.CollectZone(route_id,pick_order, active)
		VALUES(@routeId1,32767, 'F')
		SET @collectZoneId1 = @@IDENTITY

		DECLARE @collectZoneId2 int
		INSERT INTO dbo.CollectZone(route_id,pick_order)
		VALUES(@routeId1,32767)
		SET @collectZoneId2 = @@IDENTITY

		--Execute
		DECLARE @totalEntries int
		SELECT @totalEntries = total_entries
		FROM dbo.GetRouteCollectZones (1, 20, @routeId1)

		--Test		
		IF(@totalEntries < 2) 
			THROW 55005, '@CollectZoneTest.GetRouteCollectZonesTest() FAILED', 1;

		IF NOT EXISTS(
			SELECT * FROM dbo.GetRouteCollectZones(1,@totalEntries,@routeId1)
			WHERE collect_zone_id = @collectZoneId1
		)THROW 55005, '@CollectZoneTest.GetRouteCollectZonesTest() FAILED', 1;

		IF NOT EXISTS(
			SELECT * FROM dbo.GetRouteCollectZones(1,@totalEntries,@routeId1)
			WHERE collect_zone_id = @collectZoneId2
		)THROW 55005, '@CollectZoneTest.GetRouteCollectZonesTest() FAILED', 1;

		ROLLBACK;
		PRINT '@CollectZoneTest.GetRouteCollectZonesTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.GetRouteActiveCollectZonesTest
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

		DECLARE @collectZoneId1 int
		INSERT INTO dbo.CollectZone(route_id,pick_order, active)
		VALUES(@routeId1,32767, 'F')
		SET @collectZoneId1 = @@IDENTITY

		DECLARE @collectZoneId2 int
		INSERT INTO dbo.CollectZone(route_id,pick_order)
		VALUES(@routeId1,32767)
		SET @collectZoneId2 = @@IDENTITY

		--Execute
		DECLARE @totalEntries int
		SELECT @totalEntries = total_entries
		FROM dbo.GetRouteActiveCollectZones (1, 20, @routeId1)

		--Test		
		IF(@totalEntries < 1
		) THROW 55005, '@CollectZoneTest.GetRouteActiveCollectZonesTest() FAILED', 1;

		IF EXISTS(
			SELECT * FROM dbo.GetRouteActiveCollectZones(1,@totalEntries,@routeId1)
			WHERE collect_zone_id = @collectZoneId1
		)THROW 55005, '@CollectZoneTest.GetRouteActiveCollectZonesTest() FAILED', 1;

		IF NOT EXISTS(
			SELECT * FROM dbo.GetRouteActiveCollectZones(1,@totalEntries,@routeId1)
			WHERE collect_zone_id = @collectZoneId2
		)THROW 55005, '@CollectZoneTest.GetRouteActiveCollectZonesTest() FAILED', 1;

		ROLLBACK;
		PRINT '@CollectZoneTest.GetRouteActiveCollectZonesTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.GetCollectZoneInfoTest
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

		DECLARE @collectZoneId1 int
			INSERT INTO dbo.CollectZone(route_id,pick_order)
		VALUES(@routeId1,32767)
		SET @collectZoneId1 = @@IDENTITY

		DECLARE @configurationId int
		INSERT INTO dbo.Configuration(configuration_name) VALUES('configuration_test')
		SET @configurationId = @@IDENTITY

		DECLARE @containerId int
		INSERT INTO dbo.Container
			(iot_id, latitude, longitude, height, container_type, occupation, collect_zone_id, configuration_id)
		VALUES
			('THIS IS A TEST', 0, 0, 1, 'general', 80, @collectZoneId1, @configurationId)

		--Execute & Test
		IF NOT EXISTS(
			SELECT * FROM dbo.GetCollectZoneInfo (@collectZoneId1)
			WHERE collect_zone_id = @collectZoneId1 AND route_id = @routeId1 AND general_occupation = 80
				AND paper_occupation IS NULL AND glass_occupation IS NULL AND plastic_occupation IS NULL
		)THROW 55005, '@CollectZoneTest.GetCollectZoneInfoTest() FAILED', 1;

		ROLLBACK;
		PRINT '@CollectZoneTest.GetCollectZoneInfoTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.GetCollectZoneStatisticsTest
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

		DECLARE @collectZoneId1 int
			INSERT INTO dbo.CollectZone(route_id,pick_order)
		VALUES(@routeId1,32767)
		SET @collectZoneId1 = @@IDENTITY

		DECLARE @configurationId int
		INSERT INTO dbo.Configuration(configuration_name) VALUES('configuration_test')
		SET @configurationId = @@IDENTITY

		DECLARE @containerId1 int
		INSERT INTO dbo.Container
			(iot_id, latitude, longitude, height, container_type, occupation, collect_zone_id, configuration_id)
		VALUES
			('THIS IS A TEST', 0, 0, 1, 'general', 80, @collectZoneId1, @configurationId)
		SET @containerId1 = @@IDENTITY

		--Create a second Container and Collect Zone to infer if the results don't conflict
		DECLARE @collectZoneId2 int
			INSERT INTO dbo.CollectZone(route_id,pick_order)
		VALUES(@routeId1,32767)
		SET @collectZoneId2 = @@IDENTITY

		DECLARE @containerId2 int
		INSERT INTO dbo.Container
			(iot_id, latitude, longitude, height, container_type, occupation, collect_zone_id, configuration_id)
		VALUES
			('THIS IS A TEST 2', -1, -1, 1, 'general', 80, @collectZoneId2, @configurationId)
		SET @containerId2 = @@IDENTITY

		--Execute & Test
		IF NOT EXISTS (
			SELECT * FROM dbo.GetCollectZoneStatistics (@collectZoneId1)
			WHERE collect_zone_id = @collectZoneId1 AND num_containers = 1
		)THROW 55005, '@CollectZoneTest.GetCollectZoneStatisticsTest() FAILED', 1;

		ROLLBACK;
		PRINT '@CollectZoneTest.GetCollectZoneStatisticsTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO