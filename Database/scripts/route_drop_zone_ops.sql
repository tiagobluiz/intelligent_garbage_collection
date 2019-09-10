USE db_waste_management
GO

CREATE PROC dbo.CreateRouteDropZone @routeId int, @dropZoneId int
AS
	SET TRAN ISOLATION LEVEL REPEATABLE READ
	BEGIN TRAN
	BEGIN TRY
		IF NOT EXISTS(
			SELECT route_id FROM dbo.Route
			WHERE route_id = @routeId
		) THROW 55001,'Route does not exists',1

		IF NOT EXISTS(
			SELECT drop_zone_id FROM dbo.DropZone
			WHERE drop_zone_id = @dropZoneId
		) THROW 55001,'Drop zone does not exists',1

		INSERT INTO dbo.RouteDropZone(route_id,drop_zone_id)
		VALUES (@routeId, @dropZoneId)
		COMMIT
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.DeleteRouteDropZone @routeId int, @dropZoneId int
AS
	DELETE FROM dbo.RouteDropZone
	WHERE route_id = @routeId AND drop_zone_id = @dropZoneId
		
	IF (@@ROWCOUNT = 0)
		THROW 55003,'There is no association between the route and drop zone passed as parameters',1
GO

CREATE FUNCTION dbo.GetRouteDropZones(@PageNumber int, @Rows int, @RouteId int) RETURNS TABLE AS
RETURN (
	SELECT COUNT(*) OVER() AS 'total_entries', RDZ.route_id, RDZ.drop_zone_id, S.latitude, S.longitude FROM (
	 SELECT route_id, drop_zone_id FROM dbo.RouteDropZone
	 WHERE route_id = @RouteId
	) AS RDZ
	INNER JOIN dbo.Station AS S
	ON S.station_id = RDZ.drop_zone_id
	ORDER BY RDZ.route_id,RDZ.drop_zone_id
	OFFSET ((@PageNumber - 1) * @Rows) ROWS
	FETCH NEXT @Rows ROWS ONLY
)
GO

CREATE FUNCTION dbo.GetRouteDropZone(@RouteId int, @DropZoneId int) RETURNS TABLE AS
RETURN (
	SELECT R.route_id, R.drop_zone_id, S.latitude, S.longitude FROM (
		SELECT latitude, longitude, station_id FROM dbo.Station
		WHERE station_id = @DropZoneId
	) AS S INNER JOIN (
		SELECT drop_zone_id, route_id FROM dbo.RouteDropZone
		WHERE drop_zone_id = @DropZoneId
	) AS R
	ON S.station_id = R.drop_zone_id
)
GO