USE db_waste_management
GO

CREATE INDEX idx_collect_zone_pick_order
	ON dbo.CollectZone (pick_order)

CREATE INDEX idx_employee_name
	ON dbo.EmployeeDetailedInfo(name)