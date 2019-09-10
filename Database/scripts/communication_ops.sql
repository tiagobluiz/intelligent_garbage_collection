USE db_waste_management
GO

CREATE PROC dbo.CreateCommunication @communicationDesignation nvarchar(100), @communicationId int OUT
AS
	INSERT INTO dbo.Communication(communication_designation)
	VALUES (@communicationDesignation)
	SET @communicationId = @@IDENTITY
GO

CREATE PROC dbo.UpdateCommunication @communicationId int, @communicationDesignation nvarchar(100)
AS
		UPDATE dbo.Communication
		SET communication_designation = @communicationDesignation
		WHERE communication_id = @communicationId
		
		IF(@@ROWCOUNT = 0)
			THROW 55003,'The communication passed as parameter does not exists',1
GO

CREATE PROC dbo.DeleteCommunication @communicationId int
AS
	SET TRAN ISOLATION LEVEL SERIALIZABLE
	BEGIN TRAN
	BEGIN TRY
		IF EXISTS (
			SELECT TOP 1 * FROM dbo.ConfigurationCommunication
			WHERE communication_id = @communicationId
		) THROW 55002, 'This communication is used by one or more configurations',1

		DELETE FROM dbo.Communication
		WHERE communication_id = @communicationId

		IF (@@ROWCOUNT = 0)
			THROW 55003,'The communication passed as parameter does not exists',1
		COMMIT
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE FUNCTION dbo.GetAllCommunications(@PageNumber int, @Rows int) RETURNS TABLE AS
RETURN(
	SELECT COUNT(*) OVER() AS 'total_entries', communication_id, communication_designation FROM dbo.Communication
	ORDER BY communication_id
	OFFSET ((@PageNumber - 1) * @Rows) ROWS
	FETCH NEXT @Rows ROWS ONLY
)
GO

CREATE FUNCTION dbo.GetCommunication (@Communicationid int) RETURNS TABLE AS
RETURN(
	SELECT communication_id, communication_designation FROM dbo.Communication
	WHERE communication_id = @Communicationid
)
GO