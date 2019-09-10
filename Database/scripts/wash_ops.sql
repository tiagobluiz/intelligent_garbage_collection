USE db_waste_management
GO

CREATE PROC dbo.CreateWash @containerId int, @washDate datetime
AS
	SET TRAN ISOLATION LEVEL REPEATABLE READ
	BEGIN TRAN
	BEGIN TRY
		IF NOT EXISTS ( 
			SELECT TOP 1 container_id FROM dbo.Container 
			WHERE container_id = @containerId
		)
			THROW 55001,'Container does not exists',1

		INSERT INTO dbo.Wash (container_id,wash_date) VALUES (@containerId, @washDate)
		COMMIT
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.WashCollectZoneContainers @collectZoneId int, @washDate datetime, @containerType nvarchar(7)
AS
	INSERT INTO dbo.Wash(container_id,wash_date) 
		SELECT container_id, @washDate FROM dbo.ActiveContainers
		WHERE collect_zone_id = @collectZoneId AND container_type = @containerType

	IF(@@ROWCOUNT = 0)
		THROW 55001,'The collect zone does not exists, or do not have containers for the given type',1
GO

CREATE PROC dbo.UpdateWash @containerId int, @oldWashDate datetime, @newWashDate datetime
AS
	UPDATE dbo.Wash
	SET wash_date = @newWashDate
	WHERE container_id = @containerId AND wash_date = @oldWashDate

	IF(@@ROWCOUNT = 0)
		THROW 55003,'The wash passed as parameter does not exists',1

GO

CREATE FUNCTION dbo.GetContainerWashes (@PageNumber int, @Rows int, @containerId int) RETURNS TABLE AS
RETURN (
	SELECT COUNT(*) OVER() AS 'total_entries', container_id, wash_date FROM dbo.Wash
	WHERE container_id = @containerId
	ORDER BY wash_date DESC
	OFFSET ((@PageNumber - 1) * @Rows) ROWS
	FETCH NEXT @Rows ROWS ONLY
)
GO

CREATE FUNCTION dbo.GetWash(@ContainerId int, @WashDate datetime) RETURNS TABLE AS
RETURN (
	SELECT container_id, wash_date FROM dbo.Wash
	WHERE container_id = @containerId AND wash_date = @WashDate
)
GO