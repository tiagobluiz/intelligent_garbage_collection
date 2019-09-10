USE db_waste_management
GO

CREATE PROC dbo.CreateStation @stationName nvarchar(100), 
	@latitude decimal(9,6), @longitude decimal(9,6), @stationType nvarchar(12),
	@stationId int OUT
AS
	BEGIN TRAN
	BEGIN TRY
		
		INSERT INTO dbo.Station(station_name, latitude, longitude, station_type) 
		VALUES (@stationName, @latitude, @longitude, @stationType)
		SET @stationId = @@IDENTITY
		
		IF(@stationType = 'drop_zone')
			INSERT INTO dbo.DropZone(drop_zone_id) VALUES (@stationId)
	
		COMMIT
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.UpdateStation @stationId int, @stationName nvarchar(100), 
	@latitude decimal(9,6), @longitude decimal(9,6), @stationType nvarchar(12)
AS
	SET TRAN ISOLATION LEVEL REPEATABLE READ
	BEGIN TRAN
	BEGIN TRY
		DECLARE @currentStationType nvarchar(12)
		SELECT @currentStationType = station_type FROM dbo.Station
			WHERE station_id = @stationId
		IF (@currentStationType IS NULL) --We can't have a null station type
			THROW 55003, 'The station passed as parameter does not exists',1

		IF(@stationType <> @currentStationType)
		BEGIN
			IF(@currentStationType = 'drop_zone')
			BEGIN
				IF EXISTS (
					SELECT TOP 1 drop_zone_id FROM dbo.RouteDropZone
					WHERE drop_zone_id = @stationId
				) THROW 55002, 'This station is marked as drop zone of one or more routes',1
				
				DELETE FROM dbo.DropZone
				WHERE drop_zone_id = @stationId
			END
			IF(@currentStationType = 'base')
				INSERT INTO dbo.DropZone(drop_zone_id) VALUES (@stationId)
		END
		
		UPDATE dbo.Station
		SET latitude = @latitude, longitude = @longitude, station_type = @stationtype, station_name = @stationName
		WHERE station_id = @stationId

		COMMIT
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.DeleteStation @stationId int
AS
	SET TRAN ISOLATION LEVEL SERIALIZABLE --To prevent the case where a route adds this station
	BEGIN TRAN
	BEGIN TRY
		IF EXISTS (
			SELECT TOP 1 route_id FROM dbo.Route
			WHERE start_point = @stationId OR finish_point = @stationId
		) THROW 55002, 'This station is marked as start or finish point in one or more routes',1

		DECLARE @stationType nvarchar(12)
		SELECT @stationType = station_type FROM dbo.Station
		WHERE station_id = @stationId

		IF (@stationType = 'drop_zone')
		BEGIN
			IF EXISTS(
				SELECT TOP 1 drop_zone_id FROM dbo.RouteDropZone
				WHERE drop_zone_id = @stationId
			) THROW 55002, 'This station is marked as drop zone of one or more routes',1

			DELETE FROM dbo.RouteDropZone
			WHERE drop_zone_id = @stationId

			DELETE FROM dbo.DropZone
			WHERE drop_zone_id = @stationId
		END

		DELETE FROM dbo.Station
		WHERE station_id = @stationId

		IF (@@ROWCOUNT = 0)
			THROW 55003,'The station passed as parameter does not exists',1
		
		COMMIT
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE FUNCTION dbo.GetAllStations(@PageNumber int, @Rows int) RETURNS TABLE AS
RETURN(
	SELECT COUNT(*) OVER() AS 'total_entries', station_id, station_name, latitude, longitude, station_type FROM dbo.Station
	ORDER BY station_id
	OFFSET ((@PageNumber - 1) * @Rows) ROWS
	FETCH NEXT @Rows ROWS ONLY
)
GO

CREATE FUNCTION dbo.GetStationInfo(@StationId int) RETURNS TABLE AS
RETURN (
	SELECT station_id, station_name, latitude, longitude, station_type FROM dbo.Station
	WHERE station_id = @StationId
)
GO