USE db_waste_management
GO

CREATE PROC dbo.CreateConfiguration @configurationName nvarchar(100), @configurationId int OUT
AS
	INSERT INTO dbo.Configuration (configuration_name)
	VALUES (@configurationName)
	SET @configurationId = @@IDENTITY
GO

CREATE PROC dbo.UpdateConfiguration @configurationId int, @configurationName nvarchar(100)
AS
		UPDATE dbo.Configuration
		SET configuration_name = @configurationName
		WHERE configuration_id = @configurationId
		
		IF(@@ROWCOUNT = 0)
			THROW 55003,'The configuration passed as parameter does not exists',1
GO

CREATE PROC dbo.DeleteConfiguration @configurationId int
AS
	SET TRAN ISOLATION LEVEL SERIALIZABLE
	BEGIN TRAN
	BEGIN TRY
		IF EXISTS (
			SELECT TOP 1 container_id FROM dbo.Container
			WHERE configuration_id = @configurationId
		) THROW 55002, 'This configuration is still in use by one or more containers',1

		DELETE FROM dbo.ConfigurationCommunication
		WHERE configuration_id = @configurationId

		DELETE FROM dbo.Configuration
		WHERE configuration_id = @configurationId

		IF (@@ROWCOUNT = 0)
			THROW 55003, 'The configuration passed as parameter does not exists',1

		COMMIT
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE FUNCTION dbo.GetAllConfigurations (@PageNumber int, @Rows int) RETURNS TABLE AS
RETURN (
	SELECT COUNT(*) OVER() AS 'total_entries', configuration_id, configuration_name FROM dbo.Configuration
	ORDER BY configuration_name
	OFFSET ((@PageNumber - 1) * @Rows) ROWS
	FETCH NEXT @Rows ROWS ONLY
)
GO

CREATE FUNCTION dbo.GetConfiguration (@ConfigurationId int) RETURNS TABLE AS
RETURN (
	SELECT configuration_id, configuration_name FROM dbo.Configuration
	WHERE configuration_id = @ConfigurationId
)
GO