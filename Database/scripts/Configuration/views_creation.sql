USE db_waste_management
GO

CREATE VIEW dbo.ActiveRoutes WITH SCHEMABINDING AS
	SELECT route_id, active, start_point, finish_point FROM dbo.Route
	WHERE active = 'T'
GO

CREATE VIEW dbo.ActiveCollectZones WITH SCHEMABINDING AS
	SELECT collect_zone_id, route_id, pick_order, active FROM dbo.CollectZone
	WHERE active = 'T'
GO

CREATE VIEW dbo.ActiveTrucks WITH SCHEMABINDING AS
	SELECT registration_plate, active FROM dbo.Truck
	WHERE active = 'T'
GO

CREATE VIEW dbo.ActiveContainers WITH SCHEMABINDING AS
	SELECT 
		container_id, iot_id, active, latitude, longitude, height, container_type, 
		last_read_date, battery, occupation, temperature, collect_zone_id, configuration_id 
	FROM dbo.Container
	WHERE active= 'T'