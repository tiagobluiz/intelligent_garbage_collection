USE db_waste_management
GO

CREATE PROC dbo.CreateTruck @registration_plate nvarchar(8)
AS
	INSERT INTO dbo.Truck (registration_plate)
	VALUES (@registration_plate)
GO

CREATE PROC dbo.ActivateTruck @registrationPlate nvarchar(8) 
AS
	UPDATE dbo.Truck
	SET active = 'T'
	WHERE registration_plate = @registrationPlate

	IF(@@ROWCOUNT = 0)
		THROW 55003,'The truck passed as parameter does not exists',1
GO

CREATE PROC dbo.DeactivateTruck @registrationPlate nvarchar(8)
AS
	UPDATE dbo.Truck
	SET active = 'F'
	WHERE registration_plate = @registrationPlate

	IF (@@ROWCOUNT = 0)
		THROW 55003,'The truck passed as parameter does not exists',1
GO

CREATE FUNCTION dbo.GetAllTrucks(@PageNumber int, @Rows int) RETURNS TABLE AS
RETURN(
	SELECT COUNT(*) OVER() AS 'total_entries', registration_plate,active FROM dbo.Truck
	ORDER BY registration_plate
	OFFSET ((@PageNumber - 1) * @Rows) ROWS
	FETCH NEXT @Rows ROWS ONLY
)
GO

CREATE FUNCTION dbo.GetAllActiveTrucks(@PageNumber int, @Rows int) RETURNS TABLE AS
RETURN(
	SELECT COUNT(*) OVER() AS 'total_entries', registration_plate, active FROM dbo.ActiveTrucks
	ORDER BY registration_plate
	OFFSET ((@PageNumber - 1) * @Rows) ROWS
	FETCH NEXT @Rows ROWS ONLY
)
GO


CREATE FUNCTION dbo.GetTruck (@TruckPlate nvarchar(8)) RETURNS TABLE AS
RETURN (
	SELECT registration_plate, active FROM dbo.Truck
	WHERE registration_plate = @TruckPlate
)
GO