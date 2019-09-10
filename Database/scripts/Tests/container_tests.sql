USE db_waste_management
GO

CREATE PROC dbo.CreateContainerTest
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

		DECLARE @configId1 int
		INSERT INTO dbo.Configuration(configuration_name) VALUES ('Config Test 1')
		SET @configId1 = @@IDENTITY

		DECLARE @iotId nvarchar(250), @latitude decimal(9,6), @longitude decimal(9,6), @height int, 
			@type nvarchar(7), @containerId int
		SET @iotId = 'Test IOT'
		SET @latitude = 0
		SET @longitude = 0
		SET @height = 100
		SET @type = 'general'

		--Execute
		EXEC dbo.CreateContainer @iotId, @latitude, @longitude, @height, @type, @collectZoneId, @configId1, @containerId OUT

		--Test
		IF NOT EXISTS(
			SELECT container_id FROM dbo.Container
			WHERE container_id = container_id AND iot_id = @iotId AND latitude = @latitude AND longitude = @longitude
				AND height = @height AND container_type = @type AND collect_zone_id = @collectZoneId 
				AND configuration_id = @configId1
		)	THROW 55005, 'ContainerTest.CreateContainerTest() FAILED', 1
		
		ROLLBACK;
		PRINT 'ContainerTest.CreateContainerTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.UpdateContainerConfigurationTest
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

		DECLARE @configId1 int
		INSERT INTO dbo.Configuration(configuration_name) VALUES ('Config Test 1')
		SET @configId1 = @@IDENTITY

		DECLARE @iotId nvarchar(250), @latitude decimal(9,6), @longitude decimal(9,6), @height int, 
			@type nvarchar(7), @containerId int
		SET @iotId = 'Test IOT'
		SET @latitude = 0
		SET @longitude = 0
		SET @height = 100
		SET @type = 'general'

		INSERT INTO dbo.Container 
		(iot_id, latitude, longitude, height, container_type, collect_zone_id, configuration_id)
		VALUES
		(@iotId, @latitude, @longitude, @height, @type, @collectZoneId, @configId1)
		SET @containerId = @@IDENTITY

		--Execute
		DECLARE @newIotId nvarchar(250), @newType nvarchar(7), @newHeight int
		SET @newIotId = 'This is a new IOT ID Test'
		SET @newType = 'plastic'
		SET @newHeight = 150
		EXEC dbo.UpdateContainerConfiguration @containerId, @newIotId, @newHeight, @newType, @configId1

		--Test
		IF NOT EXISTS(
			SELECT container_id FROM dbo.Container
			WHERE container_id = container_id AND iot_id = @newIotId AND height = @newHeight 
				AND container_type = @newType AND configuration_id = @configId1
		)	THROW 55005, 'ContainerTest.UpdateContainerConfigurationTest() FAILED', 1
		
		ROLLBACK;
		PRINT 'ContainerTest.UpdateContainerConfigurationTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.UpdateContainerLocalizationTest
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

		/*
			Put 2 containers on a collect zone. Since this collect zone has more then 1 container,
			the procedure should create a new one for the container
		*/
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
		DECLARE @newLatitude decimal(9,6), @newLongitude decimal(9,6)
		SET @newLatitude = 0.0002
		SET @newLongitude = 0.0002
		EXEC dbo.UpdateContainerLocalization @containerId2, @newLatitude, @newLongitude, -1

		--Test
		IF((SELECT collect_zone_id FROM dbo.Container WHERE container_id = @containerId2) = @collectZoneId)
			THROW 55005, 'ContainerTest.UpdateContainerLocalizationTest() FAILED', 1

		IF NOT EXISTS(
			SELECT container_id FROM dbo.Container
			WHERE container_id = @containerId2 AND latitude = @newLatitude AND longitude = @newLongitude
		)	THROW 55005, 'ContainerTest.UpdateContainerLocalizationTest() FAILED', 1
		
		ROLLBACK;
		PRINT 'ContainerTest.UpdateContainerLocalizationTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.UpdateContainerReadsTest
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

		DECLARE @iotId nvarchar(250)
		SET @iotId = 'This is a IOT ID Test'
		DECLARE @containerId1 int
		INSERT INTO dbo.Container
			(iot_id, latitude, longitude, height, container_type, collect_zone_id, configuration_id)
		VALUES
			(@iotId, 0, 0, 1, 'general', @collectZoneId, @configId1)
		SET @containerId1 = @@IDENTITY
		
		--Execute
		DECLARE @battery tinyint, @occupation tinyint, @temperature tinyint
		SET @battery = 99
		SET @occupation = 70
		SET @temperature = 40
		
		EXEC dbo.UpdateContainerReads @iotId, @battery, @occupation, @temperature

		--Test
		IF NOT EXISTS(
			SELECT container_id FROM dbo.Container
			WHERE container_id = @containerId1 AND iot_id = @iotId AND battery = @battery 
				AND occupation = @occupation AND temperature = @temperature
		)	THROW 55005, 'ContainerTest.UpdateContainerReadsTest() FAILED', 1
		
		ROLLBACK;
		PRINT 'ContainerTest.UpdateContainerReadsTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.DeactivateContainerTest
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

		DECLARE @iotId nvarchar(250)
		SET @iotId = 'This is a IOT ID Test'
		DECLARE @containerId1 int
		INSERT INTO dbo.Container
			(iot_id, latitude, longitude, height, container_type, collect_zone_id, configuration_id)
		VALUES
			(@iotId, 0, 0, 1, 'general', @collectZoneId, @configId1)
		SET @containerId1 = @@IDENTITY
		
		--Execute
		EXEC dbo.DeactivateContainer @containerId1

		--Test
		IF EXISTS(
			SELECT container_id FROM dbo.ActiveContainers
			WHERE container_id = @containerId1
		)	THROW 55005, 'ContainerTest.DeactivateContainerTest() FAILED', 1

		IF ((SELECT active FROM dbo.Container WHERE container_id = @containerId1) <> 'F')
			THROW 55005, 'ContainerTest.DeactivateContainerTest() FAILED', 1
		
		ROLLBACK;
		PRINT 'ContainerTest.DeactivateContainerTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.ActivateContainerTest
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

		DECLARE @iotId nvarchar(250)
		SET @iotId = 'This is a IOT ID Test'
		DECLARE @containerId1 int
		INSERT INTO dbo.Container
			(iot_id, active, latitude, longitude, height, container_type, collect_zone_id, configuration_id)
		VALUES
			(@iotId, 'F', 0, 0, 1, 'general', @collectZoneId, @configId1)
		SET @containerId1 = @@IDENTITY
		
		--Execute
		EXEC dbo.ActivateContainer @containerId1

		--Test
		IF NOT EXISTS(
			SELECT container_id FROM dbo.ActiveContainers
			WHERE container_id = @containerId1
		)	THROW 55005, 'ContainerTest.ActivateContainerTest() FAILED', 1

		IF ((SELECT active FROM dbo.Container WHERE container_id = @containerId1) <> 'T')
			THROW 55005, 'ContainerTest.ActivateContainerTest() FAILED', 1
		
		ROLLBACK;
		PRINT 'ContainerTest.ActivateContainerTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.GetCollectZoneContainersTest
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
			(iot_id, active, latitude, longitude, height, container_type, collect_zone_id, configuration_id)
		VALUES
			('THIS IS A TEST 2', 'F', -1, -1, 1, 'general', @collectZoneId, @configId1)
		SET @containerId2 = @@IDENTITY

		--Execute
		DECLARE @count int
		SELECT @count = total_entries FROM dbo.GetCollectZoneContainers (1,10, @collectZoneId)

		--Test
		IF(@count <> 2)
			THROW 55005, 'ContainerTest.GetCollectZoneContainersTest() FAILED', 1

		IF NOT EXISTS(
			SELECT container_id FROM dbo.GetCollectZoneContainers (1, 10, @collectZoneId)
			WHERE container_id = @containerId1
		)	THROW 55005, 'ContainerTest.GetCollectZoneContainersTest() FAILED', 1

		IF NOT EXISTS(
			SELECT container_id FROM dbo.GetCollectZoneContainers (1, 10, @collectZoneId)
			WHERE container_id = @containerId2
		)	THROW 55005, 'ContainerTest.GetCollectZoneContainersTest() FAILED', 1

		IF EXISTS (
			SELECT container_id FROM dbo.GetCollectZoneActiveContainers (1, 10, @collectZoneId)
			WHERE container_id = @containerId2
		)	THROW 55005, 'ContainerTest.GetCollectZoneContainersTest() FAILED', 1
		
		IF NOT EXISTS(
			SELECT container_id FROM dbo.GetCollectZoneActiveContainers (1, 10, @collectZoneId)
			WHERE container_id = @containerId1
		)	THROW 55005, 'ContainerTest.GetCollectZoneContainersTest() FAILED', 1
		
		ROLLBACK;
		PRINT 'ContainerTest.GetCollectZoneContainersTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.GetRouteContainersTest
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
			(iot_id, active, latitude, longitude, height, container_type, collect_zone_id, configuration_id)
		VALUES
			('THIS IS A TEST 2', 'F', -1, -1, 1, 'general', @collectZoneId, @configId1)
		SET @containerId2 = @@IDENTITY

		--Execute
		DECLARE @count int
		SELECT @count = total_entries FROM dbo.GetRouteContainers (1,10, @routeId1)

		--Test
		IF(@count <> 2)
			THROW 55005, 'ContainerTest.GetRouteContainersTest() FAILED', 1

		IF NOT EXISTS(
			SELECT container_id FROM dbo.GetRouteContainers (1, 10, @routeId1)
			WHERE container_id = @containerId1
		)	THROW 55005, 'ContainerTest.GetRouteContainersTest() FAILED', 1

		IF NOT EXISTS(
			SELECT container_id FROM dbo.GetRouteContainers (1, 10, @routeId1)
			WHERE container_id = @containerId2
		)	THROW 55005, 'ContainerTest.GetRouteContainersTest() FAILED', 1

		IF EXISTS (
			SELECT container_id FROM dbo.GetRouteActiveContainers (1, 10, @routeId1)
			WHERE container_id = @containerId2
		)	THROW 55005, 'ContainerTest.GetRouteContainersTest() FAILED', 1
		
		IF NOT EXISTS(
			SELECT container_id FROM dbo.GetRouteActiveContainers (1, 10, @routeId1)
			WHERE container_id = @containerId1
		)	THROW 55005, 'ContainerTest.GetRouteContainersTest() FAILED', 1
		
		ROLLBACK;
		PRINT 'ContainerTest.GetRouteContainersTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.GetContainerInfoTest
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

		DECLARE @configId1 int
		INSERT INTO dbo.Configuration(configuration_name) VALUES ('Config Test 1')
		SET @configId1 = @@IDENTITY

		DECLARE @iotId nvarchar(250), @latitude decimal(9,6), @longitude decimal(9,6), @height int, 
			@type nvarchar(7), @containerId int
		SET @iotId = 'Test IOT'
		SET @latitude = 0
		SET @longitude = 0
		SET @height = 100
		SET @type = 'general'

		INSERT INTO dbo.Container 
		(iot_id, latitude, longitude, height, container_type, collect_zone_id, configuration_id)
		VALUES
		(@iotId, @latitude, @longitude, @height, @type, @collectZoneId, @configId1)
		SET @containerId = @@IDENTITY

		--Execute & Test
		IF NOT EXISTS(
			SELECT container_id FROM dbo.Container
			WHERE container_id = container_id AND iot_id = @iotId AND latitude = @latitude AND longitude = @longitude
				AND height = @height AND container_type = @type AND collect_zone_id = @collectZoneId 
				AND configuration_id = @configId1
		)	THROW 55005, 'ContainerTest.GetContainerInfoTest() FAILED', 1
		
		ROLLBACK;
		PRINT 'ContainerTest.GetContainerInfoTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.GetContainerStatisticsTest
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
		SET @collectDate = '11-11-1997 08:36'
		INSERT INTO dbo.Collect (container_id, collect_date) VALUES (@containerId, @collectDate)

		DECLARE @washDate datetime
		SET @washDate = '11-11-1997 08:40'
		INSERT INTO dbo.Wash (container_id, wash_date) VALUES (@containerId, @washDate)

		--Execute 
		DECLARE @numWashes int, @numCollects int
		SELECT @numCollects = num_collects, @numWashes = num_washes FROM dbo.GetContainerStatistics(@containerId)
		
		--Test
		IF (@numCollects <> 1 AND @numWashes <> 1) 
			THROW 55005, 'ContainerTest.GetContainerStatisticsTest() FAILED', 1
		
		ROLLBACK;
		PRINT 'ContainerTest.GetContainerStatisticsTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

/* Its not possible to test GetContainersWithOccupationBetweenRange after the system is in use, so
we have tested only GetContainersOfARouteWithOccupationBetweenRange since it has the same code with a different
WHERE clause
*/
CREATE PROC dbo.GetContainersOfARouteWithOccupationBetweenRangeTest
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
			(iot_id, latitude, longitude, height, container_type, occupation, collect_zone_id, configuration_id)
		VALUES
			('THIS IS A TEST', 0, 0, 1, 'general', 50, @collectZoneId, @configId1)
		SET @containerId1 = @@IDENTITY

		DECLARE @containerId2 int
		INSERT INTO dbo.Container
			(iot_id, active, latitude, longitude, height, container_type, occupation, collect_zone_id, configuration_id)
		VALUES
			('THIS IS A TEST 2', 'F', -1, -1, 1, 'general',100, @collectZoneId, @configId1)
		SET @containerId2 = @@IDENTITY

		--Execute
		DECLARE @occupation1_1 decimal(5,2)
		SELECT @occupation1_1 = dbo.GetContainersOfARouteWithOccupationBetweenRange(@routeId1, 0,75)
		DECLARE @occupation2_1 decimal(5,2)
		SELECT @occupation2_1 = dbo.GetContainersOfARouteWithOccupationBetweenRange(@routeId1, 75,100)
		DECLARE @occupation3_1 decimal(5,2)
		SELECT @occupation3_1 = dbo.GetContainersOfARouteWithOccupationBetweenRange(@routeId1, 0,100)

		--Test
		IF (@occupation1_1 <> 50.00)
			THROW 55005, '1 ContainerTest.GetContainersOfARouteWithOccupationBetweenRangeTest() FAILED', 1
		IF (@occupation2_1 <> 50.00)
			THROW 55005, '2 ContainerTest.GetContainersOfARouteWithOccupationBetweenRangeTest() FAILED', 1
		IF (@occupation3_1 <> 100.00)
			THROW 55005, '3 ContainerTest.GetContainersOfARouteWithOccupationBetweenRangeTest() FAILED', 1
		
		ROLLBACK;
		PRINT 'ContainerTest.GetContainersOfARouteWithOccupationBetweenRangeTest() SUCCEED'
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO