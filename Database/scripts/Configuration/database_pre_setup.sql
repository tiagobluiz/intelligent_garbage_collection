USE db_waste_management
GO

--Create first administrator in order to have someone to create all the needed logins
DECLARE @pass nvarchar(8)
EXEC dbo.CreateEmployee 'admin', 'Administrator', 'therealadmin@wastecollecter.com', 912388798, 'administrator', @pass out
PRINT @pass
--Admin password: z&p/GNiz

--Create account for iot devices to allow them to publish reads and obtain configuration
CREATE USER iot_user WITH PASSWORD = 'aa_/REnz'
ALTER ROLE iot ADD MEMBER iot_user
--Iot password: aa_/REnz

--Fill communications that are mandatory
--Defines the threshold value in which a container should be considered to collect
INSERT INTO dbo.Communication(communication_designation)
VALUES('max_threshold') 

--Defines the temperature value in which a container should be considered to collect
INSERT INTO dbo.Communication(communication_designation)
VALUES('max_temperature')

--Defines the time value in which a container device must communicate informations
INSERT INTO dbo.Communication(communication_designation)
VALUES('communication_time_interval')

--Defines the maximum value for the threshold after a collect. if the threshold is higher, the collect
--is invalid, if not, is confirmed
INSERT INTO dbo.Communication(communication_designation)
VALUES('collected_max_threshold')
