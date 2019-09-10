USE db_waste_management
GO

CREATE PROC dbo.CreateCollect @containerId int, @collectDate datetime
AS
	SET TRAN ISOLATION LEVEL REPEATABLE READ
	BEGIN TRAN
	BEGIN TRY
		IF NOT EXISTS ( 
			SELECT TOP 1 container_id FROM dbo.Container 
			WHERE container_id = @containerId
		) THROW 55001,'Container does not exists',1

		INSERT INTO dbo.Collect(container_id,collect_date) VALUES (@containerId, @collectDate)

		COMMIT
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.CollectCollectZoneContainers @collectZoneId int, @collectDate datetime, @containerType nvarchar(7)
AS
	INSERT INTO dbo.Collect(container_id,collect_date) 
		SELECT container_id, @collectDate FROM dbo.ActiveContainers
		WHERE collect_zone_id = @collectZoneId AND container_type = @containerType

	IF(@@ROWCOUNT = 0)
		THROW 55001,'The collect zone does not exists, or do not have containers for the given type',1
GO

CREATE PROC dbo.UpdateCollect @containerId int, @oldCollectDate datetime, @newCollectDate datetime
AS
	UPDATE dbo.Collect
	SET collect_date = @newCollectDate
	WHERE container_id = @containerId AND collect_date = @oldCollectDate

	IF(@@ROWCOUNT = 0)
		THROW 55003,'The collect passed as parameter does not exists',1
GO

CREATE FUNCTION dbo.GetContainerCollects (@PageNumber int, @Rows int, @containerId int) RETURNS TABLE AS
RETURN (
	SELECT COUNT(*) OVER() AS 'total_entries', container_id, collect_date, confirmed FROM dbo.Collect
	WHERE container_id = @containerId
	ORDER BY collect_date DESC
	OFFSET ((@PageNumber - 1) * @Rows) ROWS
	FETCH NEXT @Rows ROWS ONLY
)
GO

CREATE FUNCTION dbo.GetCollect(@ContainerId int, @CollectDate datetime) RETURNS TABLE AS
RETURN (
	SELECT container_id, collect_date, confirmed FROM dbo.Collect
	WHERE container_id = @containerId AND collect_date = @CollectDate
)
GO