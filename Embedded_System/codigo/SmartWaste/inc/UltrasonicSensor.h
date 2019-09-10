/**
* @file		UltrasonicSensor.h
* @brief	Contains the Ultrasonic Sensor API.
* @author	Diogo Dias
*/

//this number will divide the time (in microseconds) to get the distance in centimeters
#define FORMULA 58

// Anything over 400 centimeters (23200 us pulse) is "out of range"
#define MAX_DIST 23200

//wait to echo rise up in microseconds. If doesn't rise up within that time, problems with ultrasound sensor
#define ECHO_WAIT_TIME 900

/**
 * @brief	Initializes the Ultrasonic Sensor API
 * @return	Nothing
 * @note	This function must be called prior to any other UltrasonicSensor functions.
 */
void UltrasonicSensor_Init(int iotriggerPin, int ioechoPin);

/**
 * @brief	calculates the distance of free space in container
 * @return	distance in centimeters
 */
int UltrasonicSensor_getDistance();
