USE db_waste_management
GO

CREATE PROC dbo.CreateRoute @startPoint int, @finishPoint int, @routeId int OUT
AS
	SET TRAN ISOLATION LEVEL REPEATABLE READ
	BEGIN TRAN
	BEGIN TRY
		IF NOT EXISTS (
			SELECT station_id FROM dbo.Station
			WHERE station_id = @startPoint
		) THROW 55001, 'Start Point does not exists',1
		IF NOT EXISTS (
			SELECT station_id FROM dbo.Station
			WHERE station_id = @finishPoint
		) THROW 55001, 'Finish Point does not exists',1

		INSERT INTO dbo.Route (start_point,finish_point)
		VALUES (@startPoint, @finishPoint)
		SET @routeId = @@IDENTITY

		COMMIT
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.UpdateRoute @routeId int, @startPoint int, @finishPoint int
AS
	SET TRAN ISOLATION LEVEL REPEATABLE READ
	BEGIN TRAN
	BEGIN TRY
		IF NOT EXISTS (
			SELECT station_id FROM dbo.Station
			WHERE station_id = @startPoint
		) THROW 55001, 'Start Point does not exists',1
		IF NOT EXISTS (
			SELECT station_id FROM dbo.Station
			WHERE station_id = @finishPoint
		) THROW 55001, 'Finish Point does not exists',1

		UPDATE dbo.Route
		SET start_point = @startPoint, finish_point = @finishPoint
		WHERE route_id = @routeId;

		IF(@@ROWCOUNT = 0)
			THROW 55003,'The route passed as parameter does not exists',1

		COMMIT
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.ActivateRoute @routeId int
AS
	UPDATE dbo.Route
	SET active = 'T'
	WHERE route_id = @routeId

	IF(@@ROWCOUNT = 0)
		THROW 55003,'The route passed as parameter does not exists',1
GO

CREATE PROC dbo.DeactivateRoute @routeId int
AS
	UPDATE dbo.Route
	SET active = 'F'
	WHERE route_id = @routeId

	IF(@@ROWCOUNT = 0)
		THROW 55003,'The route passed as parameter does not exists',1
GO

CREATE FUNCTION dbo.GetAllRoutes (@PageNumber int, @Rows int) RETURNS TABLE AS
RETURN (
	SELECT COUNT(*) OVER() AS 'total_entries', R.route_id, R.active,
		S1.station_name AS 'start_point_station_name', S1.latitude AS 'start_point_latitude', S1.longitude AS 'start_point_longitude', 
		S2.station_name AS 'finish_point_station_name', S2.latitude AS 'finish_point_latitude', S2.longitude AS 'finish_point_longitude'
	FROM dbo.Route AS R
	INNER JOIN dbo.Station AS S1
	ON R.start_point = S1.station_id
	INNER JOIN dbo.Station AS S2
	ON R.finish_point = S2.station_id
	ORDER BY R.route_id
	OFFSET ((@PageNumber - 1) * @Rows) ROWS
	FETCH NEXT @Rows ROWS ONLY
)
GO

CREATE FUNCTION dbo.GetAllActiveRoutes(@PageNumber int, @Rows int) RETURNS TABLE AS
RETURN (
	SELECT COUNT(*) OVER() AS 'total_entries', R.route_id, R.active,
		S1.station_name AS 'start_point_station_name', S1.latitude AS 'start_point_latitude', S1.longitude AS 'start_point_longitude', 
		S2.station_name AS 'finish_point_station_name', S2.latitude AS 'finish_point_latitude', S2.longitude AS 'finish_point_longitude'
	FROM dbo.ActiveRoutes AS R
	INNER JOIN dbo.Station AS S1
	ON R.start_point = S1.station_id
	INNER JOIN dbo.Station AS S2
	ON R.finish_point = S2.station_id
	ORDER BY R.route_id
	OFFSET ((@PageNumber - 1) * @Rows) ROWS
	FETCH NEXT @Rows ROWS ONLY
)
GO

CREATE FUNCTION dbo.GetRouteStatistics (@RouteId int) RETURNS TABLE AS
RETURN (
	SELECT 
		R.route_id,
		COUNT(DISTINCT CZ.collect_zone_id) AS num_collect_zones,
		COUNT(DISTINCT C.container_id) AS num_containers,
		COUNT(DISTINCT RC.start_date) AS num_collects
	FROM (
		SELECT route_id FROM dbo.Route
		WHERE route_id = @RouteId
	) AS R
	LEFT JOIN dbo.CollectZone AS CZ
	ON CZ.route_id = R.route_id
	LEFT JOIN dbo.Container AS C
	ON CZ.collect_zone_id = C.collect_zone_id
	LEFT JOIN dbo.RouteCollection AS RC
	ON CZ.route_id = RC.route_id
	GROUP BY R.route_id
)
GO

CREATE FUNCTION dbo.GetRouteInfo (@RouteId int) RETURNS TABLE AS
RETURN (
	SELECT R.route_id, R.active,
		S1.station_name AS 'start_point_station_name', S1.latitude AS 'start_point_latitude', S1.longitude AS 'start_point_longitude', 
		S2.station_name AS 'finish_point_station_name', S2.latitude AS 'finish_point_latitude', S2.longitude AS 'finish_point_longitude'
	FROM (
		SELECT route_id, active, start_point, finish_point FROM dbo.Route
		WHERE route_id = @RouteId
	) AS R
	INNER JOIN dbo.Station AS S1
	ON R.start_point = S1.station_id
	INNER JOIN dbo.Station AS S2
	ON R.finish_point = S2.station_id
)
GO

CREATE FUNCTION dbo.GetCollectableRoutes(@PageNumber int, @Rows int, @ContainerType nvarchar(7)) RETURNS TABLE AS
RETURN(
	SELECT COUNT(*) OVER() AS 'total_entries', route_id, start_point, finish_point, active FROM dbo.ActiveRoutes
	WHERE route_id NOT IN (
		SELECT route_id FROM dbo.RouteCollection
		WHERE finish_date IS NULL
	) AND (SELECT total_entries FROM dbo.GetRouteCollectionPlan(1,1,route_id, @ContainerType)) >= 1
	ORDER BY route_id
	OFFSET ((@PageNumber - 1) * @Rows) ROWS
	FETCH NEXT @Rows ROWS ONLY
)
GO