/*
	CREATION OF THE DATABASE
*/
--IF EXISTS (DATABASE db_waste_management)
--	DROP DATABASE db_waste_management
--use master
--DROP DATABASE db_waste_management

CREATE DATABASE db_waste_management
GO
USE db_waste_management
GO

BEGIN TRY
	BEGIN TRAN
		CREATE TABLE dbo.EmployeeDetailedInfo(
			username nvarchar(50) NOT NULL 
				CONSTRAINT employee_username_pk PRIMARY KEY,
			name nvarchar(100) NOT NULL,
			email nvarchar(100) NOT NULL
				CONSTRAINT employee_email_ak UNIQUE,
			phone_number numeric(9) NOT NULL
				CONSTRAINT employee_phone_ak UNIQUE
				CONSTRAINT employee_chk_phone CHECK (LEN(phone_number) = 9),
			job nvarchar(13) NOT NULL
				CONSTRAINT employee_chk_job CHECK(job IN('administrator','management','collector'))
			/*Warning: An alteration to jobs designations can affect some procedures located in
			employee_ops.sql, more precisely, the ChangeEmployeeJob and CreateEmployee. 
			Procede with caution*/
		)
		
		CREATE TABLE dbo.Station(
			station_id int identity(1,1) NOT NULL 
				CONSTRAINT station_pk PRIMARY KEY,
			station_name nvarchar(100) NOT NULL
				CONSTRAINT station_name_ak UNIQUE,
			latitude decimal(9,6) NOT NULL 
				CONSTRAINT station_chk_latitude CHECK(latitude>=-90 AND latitude<=90),
			longitude decimal(9,6) NOT NULL 
				CONSTRAINT station_chk_longitude CHECK(longitude>=-180 AND longitude<=180),
			station_type nvarchar(12) NOT NULL 
				CONSTRAINT station_chk_stationtype CHECK(station_type IN('drop_zone','base')),
			CONSTRAINT station_localization_ak UNIQUE(latitude,longitude)
		);

		CREATE TABLE dbo.DropZone(
			drop_zone_id int NOT NULL 
				CONSTRAINT dropzone_fk REFERENCES dbo.Station,
			CONSTRAINT dropzone_pk PRIMARY KEY (drop_zone_id)
		);

		CREATE TABLE dbo.Route(
			route_id int identity(1,1) NOT NULL 
				CONSTRAINT route_pk PRIMARY KEY,
			start_point int NOT NULL
				CONSTRAINT route_station_fk1 REFERENCES dbo.Station,
			finish_point int NOT NULL
				CONSTRAINT route_station_fk2 REFERENCES dbo.Station,
			active char(1)
				CONSTRAINT route_chk_active CHECK(active IN ('T','F'))
				CONSTRAINT route_active_dflt DEFAULT 'T'
		);

		CREATE TABLE dbo.Truck(
			registration_plate nvarchar(8) NOT NULL 
				CONSTRAINT truck_pk PRIMARY KEY
				CONSTRAINT truck_chk_plate CHECK(LEN(registration_plate)=8),
			active char(1)
				CONSTRAINT truck_chk_active CHECK(active IN ('T','F'))
				CONSTRAINT truck_active_dflt DEFAULT 'T'
		);
		
		CREATE TABLE dbo.RouteCollection(
			route_id int NOT NULL
				CONSTRAINT routecollection_route_fk REFERENCES dbo.Route,
			start_date datetime NOT NULL
				CONSTRAINT routecollection_start_date_dflt DEFAULT GETDATE(),
			finish_date datetime,
			truck_plate nvarchar(8) NOT NULL
				CONSTRAINT routecollection_truck_fk REFERENCES dbo.Truck,
			CONSTRAINT routecollection_pk PRIMARY KEY(route_id,start_date),
			CONSTRAINT routecollection_finish_date_chk CHECK(finish_date>start_date)
		);

		CREATE TABLE dbo.RouteDropZone(
			route_id int NOT NULL 
				CONSTRAINT routedropzone_route_fk REFERENCES dbo.Route,
			drop_zone_id int NOT NULL 
				CONSTRAINT routedropzone_dropzone_fk REFERENCES dbo.DropZone,
			CONSTRAINT routedropzone_pk PRIMARY KEY(route_id,drop_zone_id)
		);

		CREATE TABLE dbo.CollectZone(
			collect_zone_id int identity(1,1) NOT NULL
				CONSTRAINT collectzone_pk PRIMARY KEY,
			route_id int NOT NULL 
				CONSTRAINT collectzone_route_fk REFERENCES dbo.Route,
			pick_order int NOT NULL 
				CONSTRAINT collectzone_pick_order_chk CHECK(pick_order>0),
			active char(1)
				CONSTRAINT collectzone_chk_active CHECK(active IN ('T','F'))
				CONSTRAINT collectzone_active_dflt DEFAULT 'T'
		);

		CREATE TABLE dbo.Configuration(
			configuration_id int identity(1,1) NOT NULL 
				CONSTRAINT configuration_pk PRIMARY KEY,
			configuration_name nvarchar(100) NOT NULL 
				CONSTRAINT configuration_ak UNIQUE
		);

		CREATE TABLE dbo.Communication(
			communication_id int identity(1,1) NOT NULL
				CONSTRAINT communication_pk PRIMARY KEY,
			communication_designation nvarchar(100) NOT NULL 
				CONSTRAINT communication_ak UNIQUE
		);

		CREATE TABLE dbo.ConfigurationCommunication(
			configuration_id int NOT NULL 
				CONSTRAINT configurationcommunication_configuration_fk REFERENCES Configuration,
			communication_id int NOT NULL 
				CONSTRAINT configurationcommunication_comunication_fk REFERENCES Communication,
			value tinyint NOT NULL
				CONSTRAINT configurationcommunication_chk_value CHECK (value >=-15),
			CONSTRAINT configurationcommunication_pks PRIMARY KEY(configuration_id,communication_id)
		);

		CREATE TABLE dbo.Container(
			container_id int identity(1,1) NOT NULL 
				CONSTRAINT container_pks PRIMARY KEY,
			iot_id nvarchar(250) NOT NULL
				CONSTRAINT container_iotid_ak UNIQUE,
			active char(1) NOT NULL
				CONSTRAINT container_chk_active CHECK(active IN ('T','F'))
				CONSTRAINT container_active_dflt DEFAULT 'T',
			latitude decimal(9,6) NOT NULL 
				CONSTRAINT container_chk_latitude CHECK(latitude>=-90 AND latitude<=90),
			longitude decimal(9,6) NOT NULL 
				CONSTRAINT container_chk_longitude CHECK(longitude>=-180 AND longitude<=180),
			height int NOT NULL
				CONSTRAINT container_chk_height CHECK(height>0),
			container_type nvarchar(7) NOT NULL
				CONSTRAINT container_chk_type CHECK(container_type IN('general','plastic','paper','glass')),
			last_read_date datetime,
			battery smallint 
				CONSTRAINT container_chk_battery CHECK (battery>=-1 AND battery <= 100),
			occupation smallint 
				CONSTRAINT container_chk_occupation CHECK (occupation>=-1 AND occupation <= 100),
			temperature smallint 
				CONSTRAINT container_chk_temperature CHECK(temperature>=-15 OR temperature = -100),
			collect_zone_id int NOT NULL 
				CONSTRAINT container_collect_zone_fk REFERENCES dbo.CollectZone,
			configuration_id int NOT NULL 
				CONSTRAINT container_configuration_fk REFERENCES dbo.Configuration,
			CONSTRAINT container_ak UNIQUE(latitude,longitude)
		);

		CREATE TABLE dbo.Collect(
			container_id int NOT NULL 
				CONSTRAINT collect_container_fk REFERENCES dbo.Container,
			collect_date datetime NOT NULL 
				CONSTRAINT collect_date_dflt DEFAULT GETDATE(),
			confirmed char(1) NOT NULL
				CONSTRAINT collect_confirmed_check CHECK (confirmed IN ('T','F'))
				CONSTRAINT collect_confirmed_dflt DEFAULT 'F',
			CONSTRAINT collect_pk PRIMARY KEY(container_id,collect_date)
		);

		CREATE TABLE dbo.Wash(
			container_id int NOT NULL 
				CONSTRAINT wash_container_fk REFERENCES dbo.Container,
			wash_date datetime NOT NULL 
				CONSTRAINT wash_date_dflt DEFAULT GETDATE(),
			CONSTRAINT wash_pk PRIMARY KEY(container_id,wash_date)
		);
	COMMIT
END TRY
BEGIN CATCH	
	THROW;
	ROLLBACK;
END CATCH


