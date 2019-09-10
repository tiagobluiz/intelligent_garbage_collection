USE db_waste_management
GO

CREATE PROC dbo.CreateRouteCollection @routeId int, @truckPlate nvarchar(8), @startDate datetime
AS
	SET TRAN ISOLATION LEVEL REPEATABLE READ
	BEGIN TRAN
	BEGIN TRY
		IF NOT EXISTS(
			SELECT route_id FROM dbo.ActiveRoutes
			WHERE route_id = @routeId
		) THROW 55001,'The route passed as parameter does not exists',1

		IF NOT EXISTS(
			SELECT registration_plate FROM dbo.ActiveTrucks
			WHERE registration_plate = @truckPlate
		) THROW 55001,'The truck passed as parameter does not exists',1

		IF EXISTS(
			SELECT start_date FROM dbo.RouteCollection
			WHERE route_id = @routeId AND finish_date IS NULL
		) THROW 55001,'This route is already being collected',1

		IF EXISTS(
			SELECT truck_plate FROM dbo.RouteCollection
			WHERE truck_plate = @truckPlate AND finish_date IS NULL
		) THROW 55001,'This truck is already collecting a route',1

		INSERT INTO dbo.RouteCollection(route_id,start_date,truck_plate)
		VALUES(@routeId,@startDate,@truckPlate)

		COMMIT
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

/*@latitude and @longitude corresponds to the current location of the application user*/
CREATE PROC dbo.CollectRoute @latitude decimal(9,6), @longitude decimal(9,6), @truckPlate nvarchar(8), 
	@startDate datetime, @containerType nvarchar(7), @routeId int OUT
AS
	SELECT TOP 1 @routeId = route_id FROM dbo.ActiveRoutes
	WHERE route_id NOT IN (
		SELECT route_id FROM dbo.RouteCollection
		WHERE finish_date IS NULL
	) AND start_point = (
		SELECT TOP 1 station_id FROM dbo.Station
		ORDER BY geography::Point(latitude,longitude,4326).STDistance(geography::Point(@Latitude, @Longitude, 4326)) ASC
	) AND (SELECT total_entries FROM dbo.GetRouteCollectionPlan(1,1,route_id, @ContainerType)) >= 1

	IF(@routeId IS NULL)
		THROW 55001, 'There is no routes available to be collected', 1

	EXEC dbo.CreateRouteCollection @routeId, @truckPlate, @startDate
GO

CREATE PROC dbo.UpdateRouteCollection @routeId int, @startDate datetime, @finishDate datetime, @truckPlate nvarchar(8)
AS
	SET TRAN ISOLATION LEVEL REPEATABLE READ
	BEGIN TRAN
	BEGIN TRY
		IF NOT EXISTS (
			SELECT registration_plate FROM dbo.Truck
			WHERE registration_plate = @truckPlate
		) THROW 55001,'Truck does not exists',1

		UPDATE dbo.RouteCollection
		SET truck_plate = @truckPlate, finish_date = @finishDate
		WHERE route_id = @routeId AND start_date = @startDate
	
		IF(@@ROWCOUNT = 0)
			THROW 55003,'The route collection passed as parameter does not exists',1

		COMMIT
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE FUNCTION dbo.GetRouteCollections(@PageNumber int, @Rows int, @RouteId int) RETURNS TABLE AS
RETURN(
	SELECT COUNT(*) OVER() AS 'total_entries', route_id, start_date,finish_date,truck_plate 
	FROM dbo.RouteCollection
	WHERE route_id = @RouteId
	ORDER BY start_date DESC
	OFFSET ((@PageNumber - 1) * @Rows) ROWS
	FETCH NEXT @Rows ROWS ONLY
)
GO

CREATE FUNCTION dbo.GetRouteCollection(@RouteId int, @StartDate datetime) RETURNS TABLE AS
RETURN(
	SELECT route_id, start_date,finish_date,truck_plate FROM dbo.RouteCollection
	WHERE route_id = @RouteId AND start_date = @StartDate
)
GO

CREATE FUNCTION dbo.GetTruckCollections(@PageNumber int, @Rows int, @TruckPlate nvarchar(8)) RETURNS TABLE AS
RETURN(
	SELECT COUNT(*) OVER() AS 'total_entries', route_id,start_date,finish_date, truck_plate FROM dbo.RouteCollection
	WHERE truck_plate = @TruckPlate
	ORDER BY route_id,start_date DESC, finish_date DESC
	OFFSET ((@PageNumber - 1) * @Rows) ROWS
	FETCH NEXT @Rows ROWS ONLY
)
GO