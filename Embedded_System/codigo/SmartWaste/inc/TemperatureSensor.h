/**
* @file		TemperatureSensor.h
* @brief	Contains the Temperature Sensor API.
* @author	Diogo Dias
*/

#include "OneWire.h"
#include "SystemTick.h"
#include "GPIO.h"
#include <stdio.h>

#define MEMORY_LENGTH_BYTES 9
#define NUMBER_OF_BITS(x)  ((x - 9)<<5)
#define CONVERSION_TIME 750							//unity ms
#define CONVERSION_TIME_9_BITS CONVERSION_TIME/8	//unity ms

/* Sensor Commands */
#define COMMAND_SKIP_ROOM 0xCC
#define COMMAND_ALARM_SEARCH 0xEC

/* Sensor Functions */
#define FUNCTION_CONVERT_TEMPERATURE 0x44
#define FUNCTION_READ_SCRATCHPAD 0xBE
#define FUNCTION_WRITE_SCRATCHPAD 0x4E
#define FUNCTION_COPY_SCRATCHPAD 0x48

/**
 * @param	pin: that will be used to communicate with the temperature sensor
 * @param	numberOfBitsConversion: number of bits that sensor will use to give the temperature. This value must be 9, 10, 11 or 12
 * @param 	lowTemperature: min temperature to not trigger flag
 * @param 	highTemperature: max temperature to not trigger flag
 * @brief 	Configures OneWire_Init and the temperature sensor
 * @Note	This must be the first function to be called
 * @Return 	Void
 */
void TemperatureSensor_Init(int pin, int numberOfBitsConversion, int lowTemperature, int highTemperature);

/**
 * @param	none
 * @brief 	Reads the temperature from the sensor
 * @Return 	the temperature in ºC
 */
float TemperatureSensor_GetTemperature();

/**
 * @brief 	Checks if the sensor has alarm triggered or not
 * @Return 	returns -1 if there was an error, '0' if the alarm flag is not activated, '1' if the alarm flag is activated
 */
int TemperatureSensor_HasAlarm();


