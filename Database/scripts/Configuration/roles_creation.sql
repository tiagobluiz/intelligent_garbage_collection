USE db_waste_management
GO
 
CREATE ROLE administrator;
GO
CREATE ROLE management;
GO
CREATE ROLE collector;
GO
CREATE ROLE iot;
GO

--Stop anyone from CREATE, ALTER or DROP any of the tables of this database
DENY ALTER ON SCHEMA::dbo TO administrator;
GO
DENY ALTER ON SCHEMA::dbo TO management;
GO
DENY ALTER ON SCHEMA::dbo TO collector;
GO
DENY ALTER ON SCHEMA::dbo TO iot;
GO

--Allow everyone to connect
GRANT CONNECT TO administrator
GO
GRANT CONNECT TO management
GO
GRANT CONNECT TO collector
GO
GRANT CONNECT TO iot
GO
/*
* Setup Administrator Role
*/
--GRANT ALTER ANY LOGIN TO administrator --Can create users and change their logins
GRANT EXECUTE, SELECT TO administrator
GRANT ALTER ANY ROLE TO administrator
GRANT ALTER ANY USER TO administrator

/*
* Setup Management Role
*/
GRANT EXECUTE, SELECT TO management

DENY EXECUTE ON dbo.CreateTruck TO management
DENY EXECUTE ON dbo.ActivateTruck TO management
DENY EXECUTE ON dbo.DeactivateTruck TO management
DENY EXECUTE ON dbo.CreateEmployee TO management
DENY EXECUTE ON dbo.UpdatePassword TO management
DENY EXECUTE ON dbo.DeleteEmployee TO management
DENY EXECUTE ON dbo.ChangeEmployeeJob TO management
DENY SELECT ON dbo.GetAllEmployees TO management
DENY SELECT ON dbo.GetEmployeeInfo TO management
DENY EXECUTE ON dbo.CreateRouteDropZone TO management
DENY EXECUTE ON dbo.DeleteRouteDropZone TO management
DENY EXECUTE ON dbo.CreateRoute TO management
DENY EXECUTE ON dbo.UpdateRoute TO management
DENY EXECUTE ON dbo.ActivateRoute TO management
DENY EXECUTE ON dbo.DeactivateRoute TO management
DENY EXECUTE ON dbo.CreateStation TO management
DENY EXECUTE ON dbo.UpdateStation TO management
DENY EXECUTE ON dbo.DeleteStation TO management
DENY EXECUTE ON dbo.CollectRoute TO management
DENY EXECUTE ON dbo.CreateRouteCollection TO management
DENY EXECUTE ON dbo.CreateWash TO management
DENY EXECUTE ON dbo.CreateCollect TO management
DENY EXECUTE ON dbo.UpdateContainerReads TO management

/*
* Setup Collecter Role
*/

GRANT EXECUTE ON dbo.CollectRoute TO collector
GRANT EXECUTE ON dbo.CreateRouteCollection TO collector
GRANT SELECT ON dbo.GetCollectableRoutes TO collector
GRANT SELECT ON dbo.GetRouteCollectionPlan TO collector
GRANT SELECT ON dbo.GetRouteCollection TO collector
GRANT EXECUTE ON dbo.UpdateRouteCollection TO collector
GRANT EXECUTE ON dbo.CollectCollectZoneContainers TO collector
GRANT EXECUTE ON dbo.WashCollectZoneContainers TO collector
GRANT EXECUTE ON dbo.CreateWash TO collector
GRANT EXECUTE ON dbo.CreateCollect TO collector
GRANT SELECT ON dbo.GetCurrentEmployeeInfo TO collector
GRANT SELECT ON dbo.GetRouteInfo TO collector
GRANT SELECT ON dbo.GetRouteStatistics TO collector

/**
* Setup IOT Role
*/
GRANT EXECUTE ON dbo.UpdateContainerReads TO iot
GRANT SELECT ON dbo.GetConfigurationCommunicationByName TO iot
GRANT SELECT ON dbo.GetContainerByIotId TO iot