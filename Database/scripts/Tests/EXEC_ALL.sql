USE db_waste_management
GO

/*
	Collect Zone Tests
*/
EXEC dbo.CreateCollectZoneTest

EXEC dbo.UpdateCollectZoneTest

EXEC dbo.DeactivateCollectZoneTest

EXEC dbo.ActivateCollectZoneTest

EXEC dbo.GetRouteCollectZonesTest

EXEC dbo.GetRouteActiveCollectZonesTest

EXEC dbo.GetCollectZoneInfoTest

EXEC dbo.GetCollectZoneStatisticsTest

/*The procedure GetRouteCollectionPlan can't be tested, since we can't create a communication with
the name 'max_threshold', cause it could already exist, and it would cause a conflict*/

/*
	Collect Tests
*/
EXEC dbo.CreateCollectTest

EXEC dbo.CollectCollectZoneContainersTest

EXEC dbo.UpdateCollectTest

EXEC dbo.GetContainerCollectsTest

EXEC dbo.GetCollectTest

/*
	Wash Tests
*/
EXEC dbo.CreateWashTest

EXEC dbo.WashCollectZoneContainersTest

EXEC dbo.UpdateWashTest

EXEC dbo.GetContainerWashesTest

EXEC dbo.GetWashTest

/*
	Communication
*/
EXEC dbo.CreateCommunicationTest

EXEC dbo.UpdateCommunicationTest

EXEC dbo.DeleteCommunicationTest

EXEC dbo.GetAllCommunicationsTest

EXEC dbo.GetCommunicationTest

/*
	Configuration
*/
EXEC dbo.CreateConfigurationTest

EXEC dbo.DeleteConfigurationTest

EXEC dbo.GetAllConfigurationsTest

EXEC dbo.GetConfigurationTest

/*
	Configuration Communication
*/

EXEC dbo.AssociateCommunicationToTheConfigurationTest

EXEC dbo.DisassociateCommunicationToConfigurationTest

EXEC dbo.GetConfigurationCommunicationsTest

EXEC dbo.GetConfigurationCommunicationTest

EXEC dbo.GetConfigurationCommunicationByNameTest

EXEC dbo.GetCommunicationValueForConfigurationTest

EXEC dbo.GetConfigurationCommunicationByNameTest
/*
	Container
*/
EXEC dbo.CreateContainerTest

EXEC dbo.UpdateContainerConfigurationTest

EXEC dbo.UpdateContainerLocalizationTest

EXEC dbo.UpdateContainerReadsTest

EXEC dbo.DeactivateContainerTest

EXEC dbo.ActivateContainerTest

EXEC dbo.GetCollectZoneContainersTest

EXEC dbo.GetRouteContainersTest

EXEC dbo.GetContainerInfoTest

EXEC dbo.GetContainerStatisticsTest

EXEC dbo.GetContainersOfARouteWithOccupationBetweenRangeTest

/*
	Route Collection
*/

EXEC dbo.CreateRouteCollectionTest

EXEC dbo.CollectRouteTest

EXEC dbo.CollectNearestRouteTest

EXEC dbo.CollectWithOccupiedTruck

EXEC dbo.UpdateRouteCollectionTest

EXEC dbo.GetRouteCollectionsTest

EXEC dbo.GetRouteCollectionTest

EXEC dbo.GetTruckCollectionsTest

/*
	Route Drop Zone
*/

EXEC dbo.CreateRouteDropZoneTest

EXEC dbo.DeleteRouteDropZoneTest

EXEC dbo.GetRouteDropZonesTest

EXEC dbo.GetRouteDropZoneTest

/*
	Route
*/
EXEC dbo.CreateRouteTest

EXEC dbo.UpdateRouteTest

EXEC dbo.ActivateRouteTest

EXEC dbo.DeactivateRouteTest

EXEC dbo.GetAllRoutesTest

EXEC dbo.GetAllActiveRoutesTest

/*
	GetRouteStatisticsTest launches a warning. This warning is laucnehd due to the way that
	the query makes the COUNT, which includes NULL values. This warning should not be considered
*/
EXEC dbo.GetRouteStatisticsTest

EXEC dbo.GetRouteInfoTest

EXEC dbo.GetCollectableRoutesTest

/*
	Station
*/

EXEC dbo.CreateStationTest

EXEC dbo.UpdateStationTest

EXEC dbo.DeleteStationTest

EXEC dbo.DeleteUsedStationTest

EXEC dbo.GetAllStationsTest

EXEC dbo.GetStationInfoTest

/*
	Truck
*/

EXEC dbo.CreateTruckTest

EXEC dbo.ActivateTruckTest

EXEC dbo.DeactivateTruckTest

EXEC dbo.GetAllTrucksTest

EXEC dbo.GetAllActiveTrucksTest

EXEC dbo.GetTruckTest

/*
	Employee
*/

/*Note that, as documented, when a password is generated it is possible that it doesn't mmet policy requirements in terms of
complexity, in that case the procedures/tests must be executed again in order to suceed*/
PRINT 'CreateEmployeeTest could fail if the password generated is not strong enough, in that case, the test should be executed again'
EXEC dbo.CreateEmployeeTest

PRINT 'UpdatePasswordTest could fail if the password generated is not strong enough, in that case, the test should be executed again'
EXEC dbo.UpdatePasswordTest

EXEC dbo.DeleteEmployeeTest

EXEC dbo.ChangeEmployeeJobTest

EXEC dbo.GetAllEmployeesTest

EXEC dbo.GetEmployeeInfoTest

--ROLLBACK