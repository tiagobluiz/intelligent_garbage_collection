USE db_waste_management
GO

CREATE PROC dbo.CreateWashTest
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

		DECLARE @configurationId int
		INSERT INTO dbo.Configuration (configuration_name)
		VALUES('Congif1Test1')
		SET @configurationId = @@IDENTITY

		DECLARE @containerId int
		INSERT INTO dbo.Container(iot_id, latitude, longitude, height, container_type, collect_zone_id, configuration_id)
		VALUES('XXXTESTXXX', 0, 0, 100, 'general', @collectZoneId, @configurationId)
		SET @containerId = @@IDENTITY

		--Execute
		DECLARE @WashDate datetime
		SET @WashDate = '11-11-1997 08:36'
		EXEC dbo.CreateWash @containerId, @WashDate

		--Test
		IF NOT EXISTS(
			SELECT container_id, wash_date FROM dbo.Wash
			WHERE container_id = @containerId AND wash_date = @WashDate
		) THROW 55005, '@WashTests.CreateWashTest() FAILED', 1
		
		ROLLBACK;
		PRINT '@WashTests.CreateWashTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.WashCollectZoneContainersTest
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

		DECLARE @configurationId int
		INSERT INTO dbo.Configuration (configuration_name)
		VALUES('Congif1Test1')
		SET @configurationId = @@IDENTITY

		DECLARE @containerId int
		INSERT INTO dbo.Container(iot_id, latitude, longitude, height, container_type, collect_zone_id, configuration_id)
		VALUES('XXXTESTXXX', 0, 0, 100, 'general', @collectZoneId, @configurationId)
		SET @containerId = @@IDENTITY

		DECLARE @containerId2 int
		INSERT INTO dbo.Container(iot_id, latitude, longitude, height, container_type, collect_zone_id, configuration_id)
		VALUES('XXXTESTXXX2', 0, 0.1, 100, 'paper', @collectZoneId, @configurationId)
		SET @containerId2 = @@IDENTITY

		DECLARE @containerId3 int
		INSERT INTO dbo.Container(iot_id, latitude, longitude, height, container_type, collect_zone_id, configuration_id)
		VALUES('XXXTESTXXX3', 0.1, 0, 100, 'general', @collectZoneId, @configurationId)
		SET @containerId3 = @@IDENTITY

		--Execute
		DECLARE @WashDate datetime
		SET @WashDate = '11-11-1997 08:36'
		EXEC dbo.WashCollectZoneContainers @collectZoneId, @WashDate, 'general'

		--Test
		IF NOT EXISTS(
			SELECT container_id, wash_date FROM dbo.Wash
			WHERE container_id = @containerId AND wash_date = @WashDate
		) THROW 55005, '@WashTests.WashCollectZoneContainersTest() FAILED', 1

		IF EXISTS(
			SELECT container_id, wash_date FROM dbo.Wash
			WHERE container_id = @containerId2 AND wash_date = @WashDate
		) THROW 55005, '@WashTests.WashCollectZoneContainersTest() FAILED', 1
		
		IF NOT EXISTS(
			SELECT container_id, wash_date FROM dbo.Wash
			WHERE container_id = @containerId3 AND wash_date = @WashDate
		) THROW 55005, '@WashTests.WashCollectZoneContainersTest() FAILED', 1
		
		ROLLBACK;
		PRINT '@WashTests.WashCollectZoneContainersTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO



CREATE PROC dbo.UpdateWashTest
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

		DECLARE @configurationId int
		INSERT INTO dbo.Configuration (configuration_name)
		VALUES('Congif1Test1')
		SET @configurationId = @@IDENTITY

		DECLARE @containerId int
		INSERT INTO dbo.Container(iot_id, latitude, longitude, height, container_type, collect_zone_id, configuration_id)
		VALUES('XXXTESTXXX', 0, 0, 100, 'general', @collectZoneId, @configurationId)
		SET @containerId = @@IDENTITY

		DECLARE @washDate datetime
		SET @washDate = '1997-11-11 08:36'
		INSERT INTO dbo.Wash(container_id, wash_date) VALUES(@containerId, @washDate)

		--Execute
		DECLARE @newWashDate datetime
		SET @newWashDate = '1958-08-24 10:00'
		EXEC dbo.UpdateWash @containerId, @WashDate, @newWashDate

		--Test
		IF NOT EXISTS(
			SELECT container_id, wash_date FROM dbo.Wash
			WHERE container_id = @containerId AND wash_date = @newWashDate
		) THROW 55005, '@WashTests.UpdateWashTest() FAILED', 1
		
		ROLLBACK;
		PRINT '@WashTests.UpdateWashTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.GetContainerWashesTest
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

		DECLARE @configurationId int
		INSERT INTO dbo.Configuration (configuration_name)
		VALUES('Congif1Test1')
		SET @configurationId = @@IDENTITY

		DECLARE @containerId int
		INSERT INTO dbo.Container(iot_id, latitude, longitude, height, container_type, collect_zone_id, configuration_id)
		VALUES('XXXTESTXXX', 0, 0, 100, 'general', @collectZoneId, @configurationId)
		SET @containerId = @@IDENTITY

		DECLARE @WashDate1 datetime
		SET @WashDate1 = '1997-11-11 08:36'
		INSERT INTO dbo.Wash(container_id, wash_date) VALUES(@containerId, @WashDate1)

		DECLARE @WashDate2 datetime
		SET @WashDate2 = '1958-08-24 10:00'
		INSERT INTO dbo.Wash(container_id, wash_date) VALUES(@containerId, @WashDate2)
		--Execute
		
		DECLARE @numWashs int
		SELECT @numWashs =  COUNT(*) OVER() FROM dbo.GetContainerWashes (1, 10, @containerId)

		--Test
		IF (@numWashs <> 2) 
			THROW 55005, '@WashTests.GetContainerWashTest() FAILED', 1
		
		ROLLBACK;
		PRINT '@WashTests.GetContainerWashTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.GetWashTest
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

		DECLARE @configurationId int
		INSERT INTO dbo.Configuration (configuration_name)
		VALUES('Congif1Test1')
		SET @configurationId = @@IDENTITY

		DECLARE @containerId int
		INSERT INTO dbo.Container(iot_id, latitude, longitude, height, container_type, collect_zone_id, configuration_id)
		VALUES('XXXTESTXXX', 0, 0, 100, 'general', @collectZoneId, @configurationId)
		SET @containerId = @@IDENTITY

		DECLARE @WashDate1 datetime
		SET @WashDate1 = '1997-11-11 08:36'
		INSERT INTO dbo.Wash(container_id, wash_date) VALUES(@containerId, @WashDate1)

		--Execute & 
		IF NOT EXISTS(
			SELECT container_id, wash_date FROM dbo.GetWash (@containerId, @WashDate1)
		) THROW 55005, '@WashTests.GetWashTest() FAILED', 1
		
		ROLLBACK;
		PRINT '@WashTests.GetWashTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO