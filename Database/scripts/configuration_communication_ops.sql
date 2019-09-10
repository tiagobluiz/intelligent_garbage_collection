USE db_waste_management
GO

CREATE PROC dbo.AssociateCommunicationToTheConfiguration @configurationId int, @communicationId int, @value tinyint
AS
	SET TRAN ISOLATION LEVEL REPEATABLE READ
	BEGIN TRAN
	BEGIN TRY
		IF NOT EXISTS(
			SELECT configuration_name FROM dbo.Configuration
			WHERE configuration_id = @configurationId
		) THROW 55001, 'The configuration passed as parameter does not exists',1

		IF NOT EXISTS(
			SELECT communication_designation FROM dbo.Communication
			WHERE communication_id = @communicationId
		) THROW 55001, 'The communication passed as parameter does not exists',1

		INSERT INTO dbo.ConfigurationCommunication (configuration_id, communication_id, value)
		VALUES (@configurationId, @communicationId, @value)

		COMMIT
	END TRY
	BEGIN CATCH
		ROLLBACK;
		THROW;
	END CATCH
GO

CREATE PROC dbo.DisassociateCommunicationToConfiguration @configurationId int, @communicationId int
AS	
	DELETE FROM dbo.ConfigurationCommunication
	WHERE @configurationId = @configurationId AND communication_id = @communicationId
	
	IF (@@ROWCOUNT = 0)
		THROW 55003,'There is no association between the given configuration and communication',1
GO

CREATE FUNCTION dbo.GetConfigurationCommunications (@PageNumber int, @Rows int, @ConfigurationId int) RETURNS TABLE AS
RETURN (
	SELECT COUNT(*) OVER() AS 'total_entries', CC.configuration_id, CC.communication_id, C.communication_designation, CC.value FROM dbo.ConfigurationCommunication AS CC
	INNER JOIN dbo.Communication AS C
	ON CC.communication_id = C.communication_id
	ORDER BY CC.communication_id
	OFFSET ((@PageNumber - 1) * @Rows) ROWS
	FETCH NEXT @Rows ROWS ONLY
)
GO

CREATE FUNCTION dbo.GetConfigurationCommunication (@ConfigurationId int, @CommunicationId int) RETURNS TABLE AS
RETURN (
	SELECT CC.configuration_id, CC.communication_id, C.communication_designation, CC.value FROM (
		SELECT configuration_id, communication_id, value FROM dbo.ConfigurationCommunication
		WHERE configuration_id = @ConfigurationId AND communication_id = @CommunicationId
	)AS CC
	INNER JOIN dbo.Communication AS C
	ON CC.communication_id = C.communication_id
)
GO

CREATE FUNCTION dbo.GetConfigurationCommunicationByName (@ConfigurationId int, @CommunicationName nvarchar(50)) RETURNS TABLE AS
RETURN (
	SELECT configuration_id, communication_id, value FROM dbo.ConfigurationCommunication
	WHERE configuration_id = @ConfigurationId AND communication_id = (
		SELECT communication_id FROM dbo.Communication
		WHERE communication_designation = @CommunicationName
	)
)
GO

/*Utilitary function*/
CREATE FUNCTION dbo.GetCommunicationValueForConfiguration (@ConfigurationId int, @CommunicationDesignation nvarchar(100)) RETURNS INT
AS BEGIN
	DECLARE @value int

	SELECT @value = CC.value FROM (
		SELECT communication_id FROM dbo.Communication
		WHERE communication_designation = @CommunicationDesignation
	) AS Com
	INNER JOIN (
		SELECT communication_id, value FROM dbo.ConfigurationCommunication
		WHERE configuration_id = @ConfigurationId
	) AS CC
	ON com.communication_id = CC.communication_id

	RETURN @value
END;
GO