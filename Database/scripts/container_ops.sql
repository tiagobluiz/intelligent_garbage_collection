USE db_waste_management
GO

CREATE PROC dbo.CreateContainer @iotId nvarchar(250), @latitude decimal(9,6), @longitude decimal(9,6), @height int, @type nvarchar(7), 
	@collectZoneId int, @configurationId int, @containerId int OUT
AS
	--To avoid delete after verifying that CollectZone and Configuration exists
	SET TRAN ISOLATION LEVEL REPEATABLE READ
	BEGIN TRAN
	BEGIN TRY
		IF NOT EXISTS (
			SELECT collect_zone_id FROM dbo.ActiveCollectZones
			WHERE collect_zone_id = @collectZoneId
		) THROW 55001,'Collect Zone does not exists',1
		IF NOT EXISTS(
			SELECT configuration_id FROM dbo.Configuration
			WHERE configuration_id = @configurationId
		) THROW 55001,'Configuration does not exists',1

		INSERT INTO dbo.Container 
		(iot_id, latitude, longitude, height, container_type, collect_zone_id, configuration_id)
		VALUES
		(@iotId, @latitude, @longitude, @height, @type, @collectZoneId, @configurationId)
		SET @containerId = @@IDENTITY

		COMMIT
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.UpdateContainerConfiguration @containerId int, @iotId nvarchar(250), @height int, @type nvarchar(7), @configurationId int
AS
	SET TRAN ISOLATION LEVEL REPEATABLE READ
	BEGIN TRAN
	BEGIN TRY
		IF NOT EXISTS(
			SELECT configuration_id FROM dbo.Configuration
			WHERE configuration_id = @configurationId
		) THROW 55001,'Configuration does not exists',1

		UPDATE dbo.Container
		SET iot_id = @iotId, height = @height, container_type = @type, configuration_id = @configurationId
		WHERE container_id = @containerId

		IF(@@ROWCOUNT = 0)
			THROW 55003, 'The container passed as parameter does not exists',1
		COMMIT
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

/**
* If a new collect zone should be created and associated to this container, then @collectZoneId MUST have the value -1. However, if the
collect zone only have one container, then that zone will be reused.
The route will automatically be assumed by the route of the current collect zone. When associating the container to an existent
collect zone, than @collectZoneId MUST have a valid identifier.
If the container will be relocated to an existent collect zone that has already one or more containers, then the localization 
SHOULD be at a maximum of 10 meters of distance of the other containers.
*/
CREATE PROC dbo.UpdateContainerLocalization @containerId int, @latitude decimal(9,6), @longitude decimal(9,6),
	@collectZoneId int
AS
	SET TRAN ISOLATION LEVEL REPEATABLE READ
	BEGIN TRAN
	BEGIN TRY
		IF(@collectZoneId = -1)
		BEGIN
			DECLARE @numContainers int
			SELECT @numContainers = COUNT(*) OVER(), @collectZoneId = collect_zone_id FROM dbo.Container 
			WHERE collect_zone_id = (
				SELECT collect_zone_id FROM dbo.Container WHERE container_id = @containerId
			)
			IF(@numContainers > 1 )
			BEGIN
				DECLARE @routeId int
				SELECT @routeId = route_id FROM dbo.CollectZone
				WHERE collect_zone_id = @collectZoneId

				EXEC dbo.CreateCollectZone @routeId, @collectZoneId OUT
			END
		END
		ELSE
		BEGIN
			IF NOT EXISTS (
				SELECT TOP 1 collect_zone_id FROM dbo.ActiveCollectZones
				WHERE collect_zone_id = @collectZoneId
			) THROW 55001,'Collect Zone does not exists or is inactive',1
		END

		UPDATE dbo.Container
		SET latitude = @latitude, longitude = @longitude, collect_zone_id = @collectZoneId
		WHERE container_id = @containerId

		IF(@@ROWCOUNT = 0)
			THROW 55003, 'The container passed as parameter does not exists',1
		COMMIT
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.UpdateContainerReads @iotId nvarchar(250), @battery smallint, @occupation smallint, 
    @temperature smallint
AS
	SET TRAN ISOLATION LEVEL REPEATABLE READ
	BEGIN TRAN
	BEGIN TRY
		DECLARE @lastOccupation smallint, @configurationId int, @containerId int
		SELECT @lastOccupation = occupation, @configurationId = configuration_id, @containerId = container_id FROM dbo.Container
		WHERE iot_id = @iotId
		
		UPDATE dbo.Container
		SET battery = @battery, occupation = @occupation, temperature = @temperature, last_read_date = GETDATE()
		WHERE container_id = @containerId
		
		IF (@@ROWCOUNT = 0)
			THROW 55003,'The container passed as parameter does not exists',1

		IF(@occupation < @lastOccupation AND 
			@occupation <= (SELECT value FROM dbo.ConfigurationCommunication 
							WHERE configuration_id=@configurationId AND
									communication_id = (
										SELECT communication_id FROM dbo.Communication
										WHERE communication_designation = 'collected_max_threshold'
									)
							)
		) BEGIN
			UPDATE dbo.Collect 
			SET confirmed='T' 
			WHERE container_id = @containerId AND collect_date = (
				SELECT TOP 1 collect_date FROM dbo.Collect
				WHERE container_id = 1
				ORDER BY collect_date DESC
			)
		END
		COMMIT
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.DeactivateContainer @containerId int
AS
	UPDATE dbo.Container
	SET active = 'F'
	WHERE container_id = @containerId

	IF (@@ROWCOUNT = 0)
		THROW 55003,'The container passed as parameter does not exists',1
GO

CREATE PROC dbo.ActivateContainer @containerId int
AS
		UPDATE dbo.Container
		SET active = 'T'
		WHERE container_id = @containerId

		IF (@@ROWCOUNT = 0)
			THROW 55003,'The container passed as parameter does not exists',1
GO

CREATE FUNCTION dbo.GetCollectZoneContainers (@PageNumber int, @Rows int, @CollectZoneId int) RETURNS TABLE AS
RETURN (
	SELECT 
		COUNT(*) OVER() AS 'total_entries',
		container_id, iot_id, active, latitude, longitude, height, container_type, 
		last_read_date, battery, occupation, temperature, collect_zone_id, configuration_id 
	FROM (
		SELECT 
			container_id, iot_id, active, latitude, longitude, height, container_type, 
			last_read_date, battery, occupation, temperature, collect_zone_id, configuration_id 
		FROM dbo.Container
		WHERE collect_zone_id = @CollectZoneId
	) AS C
	ORDER BY C.container_id
	OFFSET ((@PageNumber - 1) * @Rows) ROWS
	FETCH NEXT @Rows ROWS ONLY
)
GO

CREATE FUNCTION dbo.GetCollectZoneActiveContainers (@PageNumber int, @Rows int, @CollectZoneId int) RETURNS TABLE AS
RETURN (
	SELECT 
		COUNT(*) OVER() AS 'total_entries',
		container_id, iot_id, active, latitude, longitude, height, container_type, 
		last_read_date, battery, occupation, temperature, collect_zone_id, configuration_id
	FROM (
		SELECT 
			container_id, iot_id, active, latitude, longitude, height, container_type, 
			last_read_date, battery, occupation, temperature, collect_zone_id, configuration_id  FROM dbo.ActiveContainers
		WHERE collect_zone_id = @CollectZoneId
	) AS C
	ORDER BY C.container_id
	OFFSET ((@PageNumber - 1) * @Rows) ROWS
	FETCH NEXT @Rows ROWS ONLY
)
GO

CREATE FUNCTION dbo.GetRouteContainers (@PageNumber int,  @Rows int, @RouteId int) RETURNS TABLE AS
RETURN (
	SELECT COUNT(*) OVER() AS 'total_entries',
		container_id, iot_id, active, latitude, longitude, height, container_type, 
		last_read_date, battery, occupation, temperature, collect_zone_id, configuration_id
	FROM (
		SELECT 
			container_id, iot_id, active, latitude, longitude, height, container_type, 
			last_read_date, battery, occupation, temperature, collect_zone_id, configuration_id 
		FROM dbo.Container
		WHERE collect_zone_id IN (
			SELECT collect_zone_id FROM dbo.ActiveCollectZones
			WHERE route_id = @RouteId
		)
	) AS C
	ORDER BY C.container_id
	OFFSET ((@PageNumber - 1) * @Rows) ROWS
	FETCH NEXT @Rows ROWS ONLY
)
GO

CREATE FUNCTION dbo.GetRouteActiveContainers (@PageNumber int,  @Rows int, @RouteId int) RETURNS TABLE AS
RETURN (
	SELECT COUNT(*) OVER() AS 'total_entries',
		container_id, iot_id, active, latitude, longitude, height, container_type, 
		last_read_date, battery, occupation, temperature, collect_zone_id, configuration_id
	FROM (
		SELECT 
			container_id, iot_id, active, latitude, longitude, height, container_type, 
			last_read_date, battery, occupation, temperature, collect_zone_id, configuration_id 
		FROM dbo.ActiveContainers
		WHERE collect_zone_id IN (
			SELECT collect_zone_id FROM dbo.ActiveCollectZones
			WHERE route_id = @RouteId
		)
	) AS C
	ORDER BY C.container_id
	OFFSET ((@PageNumber - 1) * @Rows) ROWS
	FETCH NEXT @Rows ROWS ONLY
)
GO

CREATE FUNCTION dbo.GetContainerInfo (@ContainerId int) RETURNS TABLE AS
RETURN (
	SELECT 
		container_id, iot_id, active, latitude, longitude, height, container_type, 
		last_read_date, battery, occupation, temperature, collect_zone_id, configuration_id
	FROM dbo.Container
	WHERE container_id = @ContainerId
)
GO

CREATE FUNCTION dbo.GetContainerStatistics (@ContainerId int) RETURNS TABLE AS
RETURN (
	SELECT
		C.container_id, 
		COUNT(wash_date) as num_washes,
		COUNT(collect_date) as num_collects
	FROM (
		SELECT container_id FROM dbo.Container
		WHERE container_id = @ContainerId
	) AS C
	LEFT JOIN dbo.Wash AS W ON C.container_id = W.container_id
	LEFT JOIN dbo.Collect AS CL ON C.container_id = CL.container_id 
	GROUP BY C.container_id
)
GO

CREATE FUNCTION dbo.GetContainerByIotId (@IotId nvarchar(250)) RETURNS TABLE AS
RETURN (
	SELECT 
		container_id, iot_id, active, latitude, longitude, height, container_type, 
		last_read_date, battery, occupation, temperature, collect_zone_id, configuration_id
	FROM dbo.Container
	WHERE iot_id = @IotId
)
GO

/**
* Get percentage of containers where occupation its on a given range
* @param MinOccupation beginning of the interval, in an inclusive way
* @param MaxOccupation end of the interval, in an inclusive way
* @return a decima
l(4,2) with the obtained percentage
*/
CREATE FUNCTION dbo.GetContainersWithOccupationBetweenRange (@MinOccupation int, @MaxOccupation int) 
RETURNS DECIMAL(5,2)
AS BEGIN
	DECLARE @numContainers decimal(17,2)
	SELECT @numContainers = COUNT(container_id) FROM dbo.Container
	IF (@numContainers = 0) RETURN 0

	DECLARE @numContainersOnRange decimal(17,2)
	SELECT @numContainersOnRange =  COUNT(container_id) FROM dbo.Container
	WHERE occupation >= @MinOccupation AND occupation <= @MaxOccupation

	RETURN (@numContainersOnRange/@numContainers) * 100
END;
GO

/**
* Get percentage of containers where occupation its on a given range of a route
* @param MinOccupation beginning of the interval, in an inclusive way
* @param MaxOccupation end of the interval, in an inclusive way
* @return a decimal(4,2) with the obtained percentage
*/
CREATE FUNCTION dbo.GetContainersOfARouteWithOccupationBetweenRange (@RouteId int,@MinOccupation int, @MaxOccupation int) 
RETURNS DECIMAL(5,2)
AS BEGIN
	DECLARE @numContainers decimal(17,2)
	SELECT @numContainers = COUNT(container_id) FROM dbo.Container
	WHERE collect_zone_id IN (
			SELECT collect_zone_id FROM dbo.CollectZone
			WHERE route_id = @RouteId
	)
	IF (@numContainers = 0) RETURN 0

	DECLARE @numContainersOnRange decimal(17,2)
	SELECT @numContainersOnRange = COUNT(container_id) FROM (
		SELECT container_id FROM dbo.Container
		WHERE collect_zone_id IN (
			SELECT collect_zone_id FROM dbo.CollectZone
			WHERE route_id = @RouteId
		) AND occupation >= @MinOccupation AND occupation <= @MaxOccupation
	) AS RouteData

	RETURN (@numContainersOnRange/@numContainers) * 100
END;
GO