USE db_waste_management
GO

CREATE PROC dbo.CreateCollectTest
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
		DECLARE @collectDate datetime
		SET @collectDate = '11-11-1997 08:36'
		EXEC dbo.CreateCollect @containerId, @collectDate

		--Test
		IF NOT EXISTS(
			SELECT container_id, collect_date FROM dbo.Collect
			WHERE container_id = @containerId AND collect_date = @collectDate
		) THROW 55005, '@CollectTests.CreateCollectTest() FAILED', 1
		
		ROLLBACK;
		PRINT '@CollectTests.CreateCollectTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.CollectCollectZoneContainersTest
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
		VALUES('XXXTESTXXX2', 0.00001, 0.0001, 100, 'paper', @collectZoneId, @configurationId)
		SET @containerId2 = @@IDENTITY

		DECLARE @containerId3 int
		INSERT INTO dbo.Container(iot_id, latitude, longitude, height, container_type, collect_zone_id, configuration_id)
		VALUES('XXXTESTXXX3', 0.000012, 0.00012, 100, 'general', @collectZoneId, @configurationId)
		SET @containerId3 = @@IDENTITY

		DECLARE @collectDate datetime
		SET @collectDate = '1997-11-11 08:36'
		
		--Execute
		EXEC dbo.CollectCollectZoneContainers @collectZoneId, @collectDate, 'general'

		--Test
		IF NOT EXISTS(
			SELECT container_id, collect_date FROM dbo.Collect
			WHERE container_id = @containerId AND collect_date = @collectDate
		) THROW 55005, '@CollectTests.CollectCollectZoneContainersTest() FAILED', 1

		IF NOT EXISTS(
			SELECT container_id, collect_date FROM dbo.Collect
			WHERE container_id = @containerId3 AND collect_date = @collectDate
		) THROW 55005, '@CollectTests.CollectCollectZoneContainersTest() FAILED', 1

		IF EXISTS(
			SELECT container_id, collect_date FROM dbo.Collect
			WHERE container_id = @containerId2 AND collect_date = @collectDate
		) THROW 55005, '@CollectTests.CollectCollectZoneContainersTest() FAILED', 1
		
		ROLLBACK;
		PRINT '@CollectTests.CollectCollectZoneContainersTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO


CREATE PROC dbo.UpdateCollectTest
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

		DECLARE @collectDate datetime
		SET @collectDate = '1997-11-11 08:36'
		INSERT INTO dbo.Collect(container_id, collect_date) VALUES(@containerId, @collectDate)

		--Execute
		DECLARE @newCollectDate datetime
		SET @newCollectDate = '1958-08-24 10:00'
		EXEC dbo.UpdateCollect @containerId, @collectDate, @newCollectDate

		--Test
		IF NOT EXISTS(
			SELECT container_id, collect_date FROM dbo.Collect
			WHERE container_id = @containerId AND collect_date = @newCollectDate
		) THROW 55005, '@CollectTests.UpdateCollectTest() FAILED', 1
		
		ROLLBACK;
		PRINT '@CollectTests.UpdateCollectTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.GetContainerCollectsTest
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

		DECLARE @collectDate1 datetime
		SET @collectDate1 = '1997-11-11 08:36'
		INSERT INTO dbo.Collect(container_id, collect_date) VALUES(@containerId, @collectDate1)

		DECLARE @collectDate2 datetime
		SET @collectDate2 = '1958-08-24 10:00'
		INSERT INTO dbo.Collect(container_id, collect_date) VALUES(@containerId, @collectDate2)
		--Execute
		
		DECLARE @numCollects int
		SELECT @numCollects =  COUNT(*) OVER() FROM dbo.GetContainerCollects (1,10, @containerId)

		--Test
		IF (@numCollects < 2) 
			THROW 55005, '@CollectTests.GetContainerCollectTest() FAILED', 1

		IF NOT EXISTS(
			SELECT container_id, collect_date FROM dbo.Collect
			WHERE container_id = @containerId AND collect_date = @collectDate1
		)THROW 55005, '@CollectTests.GetContainerCollectTest() FAILED', 1

		IF NOT EXISTS(
			SELECT container_id, collect_date FROM dbo.Collect
			WHERE container_id = @containerId AND collect_date = @collectDate2
		)THROW 55005, '@CollectTests.GetContainerCollectTest() FAILED', 1
		
		ROLLBACK;
		PRINT '@CollectTests.GetContainerCollectTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.GetCollectTest
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

		DECLARE @collectDate1 datetime
		SET @collectDate1 = '1997-11-11 08:36'
		INSERT INTO dbo.Collect(container_id, collect_date) VALUES(@containerId, @collectDate1)

		--Execute & 
		IF NOT EXISTS(
			SELECT container_id, collect_date FROM dbo.GetCollect (@containerId, @collectDate1)
		) THROW 55005, '@CollectTests.GetCollectTest() FAILED', 1
		
		ROLLBACK;
		PRINT '@CollectTests.GetCollectTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO