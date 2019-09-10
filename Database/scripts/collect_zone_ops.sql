USE db_waste_management
GO

CREATE PROC dbo.CreateCollectZone @routeId int, @collectZoneId int OUT
AS
	SET TRAN ISOLATION LEVEL SERIALIZABLE
	BEGIN TRAN
	BEGIN TRY
		IF NOT EXISTS(
			SELECT route_id FROM dbo.ActiveRoutes
			WHERE route_id = @routeId
		) THROW 55001,'Route does not exists',1

		DECLARE @lastPickOrder int
		SELECT TOP 1 @lastPickOrder = MAX(pick_order) FROM dbo.CollectZone
		WHERE route_id = @routeId

		IF @lastPickOrder IS NULL
			SET @lastPickOrder = 0

		INSERT INTO dbo.CollectZone(route_id, pick_order)
		VALUES (@routeId, @lastPickOrder + 1)
		SET @collectZoneId = @@IDENTITY

		COMMIT
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.UpdateCollectZone @collectZoneId int, @routeId int
AS
	SET TRAN ISOLATION LEVEL REPEATABLE READ
	BEGIN TRAN
	BEGIN TRY
		IF NOT EXISTS (
			SELECT route_id FROM dbo.Route
			WHERE route_id = @routeId
		) THROW 55001,'Route does not exists',1

		UPDATE dbo.CollectZone
		SET route_id = @routeId
		WHERE collect_zone_id = @collectZoneId
		
		IF(@@ROWCOUNT = 0)
			THROW 55003,'The collect zone passed as parameter does not exists',1
		COMMIT
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.ActivateCollectZone @collectZoneId int
AS
	UPDATE dbo.CollectZone
	SET active = 'T'
	WHERE collect_zone_id = @collectZoneId
		
	IF(@@ROWCOUNT = 0)
		THROW 55003,'The collect zone passed as parameter does not exists',1
GO

CREATE PROC dbo.DeactivateCollectZone @collectZoneId int
AS
	UPDATE dbo.CollectZone
	SET active = 'F'
	WHERE collect_zone_id = @collectZoneId

	IF (@@ROWCOUNT = 0)
		THROW 55003,'The collect zone passed as parameter does not exists',1
GO

CREATE FUNCTION dbo.GetRouteCollectZones(@PageNumber int, @Rows int, @RouteId int) RETURNS TABLE AS
RETURN(
	SELECT COUNT(*) OVER() AS 'total_entries', CZ.collect_zone_id, CZ.route_id, CZ.pick_order, CZ.active,
		C.latitude, C.longitude
	FROM (
		SELECT collect_zone_id, active, route_id, pick_order FROM dbo.CollectZone
		WHERE route_id = @RouteId
	) AS CZ
	LEFT JOIN (
		SELECT TOP 1 WITH TIES latitude, longitude, occupation, collect_zone_id FROM dbo.Container
		ORDER BY ROW_NUMBER() OVER(PARTITION BY collect_zone_id ORDER BY occupation DESC)
	)AS C
	ON C.collect_zone_id = CZ.collect_zone_id
	ORDER BY CZ.pick_order
	OFFSET ((@PageNumber - 1) * @Rows) ROWS
	FETCH NEXT @Rows ROWS ONLY
)
GO

CREATE FUNCTION dbo.GetRouteActiveCollectZones(@PageNumber int, @Rows int, @RouteId int) RETURNS TABLE AS
RETURN(
	SELECT COUNT(*) OVER() AS 'total_entries', CZ.collect_zone_id, CZ.route_id, CZ.pick_order, CZ.active,
		C.latitude, C.longitude
	FROM (
		SELECT collect_zone_id, active, route_id, pick_order FROM dbo.ActiveCollectZones
		WHERE route_id = @RouteId
	) AS CZ
	LEFT JOIN (
		SELECT TOP 1 WITH TIES latitude, longitude, occupation, collect_zone_id FROM dbo.Container
		ORDER BY ROW_NUMBER() OVER(PARTITION BY collect_zone_id ORDER BY occupation DESC)
	)AS C
	ON C.collect_zone_id = CZ.collect_zone_id
	ORDER BY CZ.pick_order
	OFFSET ((@PageNumber - 1) * @Rows) ROWS
	FETCH NEXT @Rows ROWS ONLY
)
GO

CREATE FUNCTION dbo.GetCollectZoneInfo (@CollectZoneId int) RETURNS TABLE AS
RETURN (
	SELECT CZ.collect_zone_id, CZ.route_id, CZ.pick_order, CZ.active, 
		C.latitude, C.longitude, C.general_occupation, C.paper_occupation, C.plastic_occupation, C.glass_occupation
	FROM (
		SELECT collect_zone_id, active, route_id, pick_order FROM dbo.CollectZone
		WHERE collect_zone_id = @CollectZoneId	
	) AS CZ LEFT JOIN (
		SELECT collect_zone_id, latitude,longitude,
			MAX(CASE WHEN container_type = 'general' THEN occupation END) AS general_occupation,
			MAX(CASE WHEN container_type = 'paper' THEN occupation END) AS paper_occupation,
			MAX(CASE WHEN container_type = 'plastic' THEN occupation END) AS plastic_occupation,
			MAX(CASE WHEN container_type = 'glass' THEN occupation END) AS glass_occupation
		FROM dbo.Container
		WHERE collect_zone_id = @CollectZoneId
		GROUP BY collect_zone_id, latitude,longitude
	) AS C
	ON CZ.collect_zone_id = C.collect_zone_id
)
GO

CREATE FUNCTION dbo.GetCollectZoneStatistics (@CollectZoneId int) RETURNS TABLE AS
RETURN (
	SELECT collect_zone_id, COUNT(container_id) AS num_containers
	FROM (
		SELECT container_id, collect_zone_id FROM dbo.Container
		WHERE collect_zone_id = @CollectZoneId
	) AS C
	GROUP BY collect_zone_id	
)
GO

CREATE FUNCTION dbo.GetRouteCollectionPlan(@PageNumber int, @Rows int, @RouteId int, @ContainerType nvarchar(7)) RETURNS TABLE AS
RETURN(
	SELECT 
		 COUNT(*) OVER() AS 'total_entries', CZ.collect_zone_id, CZ.route_id, CZ.pick_order, CZ.active, C.latitude, C.longitude
	FROM (
		SELECT collect_zone_id, route_id, pick_order, active FROM dbo.ActiveCollectZones
		WHERE route_id = @RouteId
	) AS CZ
	INNER JOIN (
		SELECT TOP 1 WITH TIES latitude, longitude, occupation, collect_zone_id FROM dbo.ActiveContainers AS AC
		WHERE container_type = @ContainerType AND (occupation >= dbo.GetCommunicationValueForConfiguration(AC.configuration_id, 'max_threshold') 
		OR temperature >= dbo.GetCommunicationValueForConfiguration(AC.configuration_id, 'max_temperature'))
		ORDER BY ROW_NUMBER() OVER(PARTITION BY collect_zone_id ORDER BY occupation DESC)
	) AS C
	ON CZ.collect_zone_id = C.collect_zone_id
	ORDER BY CZ.pick_order
	OFFSET ((@PageNumber - 1) * @Rows) ROWS
	FETCH NEXT @Rows ROWS ONLY
)
GO

CREATE FUNCTION dbo.GetCollectZonesInRange(@Latitude decimal(9,6), @Longitude decimal(9,6), @Range smallint)
RETURNS TABLE AS RETURN(
	SELECT 
		CZ.collect_zone_id, CZ.route_id, CZ.pick_order, CZ.active, C.latitude, C.longitude 
	FROM dbo.CollectZone AS CZ
	INNER JOIN (
		SELECT TOP 1 WITH TIES latitude, longitude, occupation, collect_zone_id FROM dbo.Container
		ORDER BY ROW_NUMBER() OVER(PARTITION BY collect_zone_id ORDER BY occupation DESC)
	)AS C
	ON C.collect_zone_id = CZ.collect_zone_id
	WHERE geography::Point(C.latitude,C.longitude,4326).STDistance(geography::Point(@Latitude, @Longitude, 4326)) <= @Range
)