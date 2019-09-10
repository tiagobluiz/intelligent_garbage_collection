#include "UltrasonicSensor.h"
#include "LPC17xx.h"
#include "GPIO.h"
#include "SystemTick.h"
#include "Timer.h"

static int triggerPin;
static int echoPin;

void UltrasonicSensor_Init(int iotriggerPin, int ioechoPin){
	triggerPin = iotriggerPin;
	echoPin = ioechoPin;

	//Configure Ports
	GPIO_SetPinFunction(triggerPin, GPIO_PORT);
	GPIO_SetPinFunction(echoPin, GPIO_PORT);
	GPIO_SetIOPinDirection(triggerPin, OUTPUT);
	GPIO_SetIOPin(iotriggerPin, 0);
	GPIO_SetIOPinDirection(echoPin, INPUT);

	//Pull down the Echo Pin
	LPC_PINCON->PINMODE0 |= 0x3 << 14;
}

int UltrasonicSensor_getDistance(){

	//Trigger the signal
	GPIO_SetIOPin(triggerPin, 1);
	delay(10, microseconds);
	GPIO_SetIOPin(triggerPin, 0);

	//start time
	TIMER0_SetCount(microseconds);

	int echoStart = TIMER0_GetValue();
	//wait to echo raise up to start count
	while(!(GPIO_GetIOPin(echoPin)&(1<<echoPin))){
		if(TIMER0_Elapse(echoStart)> ECHO_WAIT_TIME)
			return -1;
	}

	int initial = TIMER0_GetValue();

	//wait to echo go low
	while((GPIO_GetIOPin(echoPin)&(1<<echoPin)))
		if(TIMER0_Elapse(initial)> MAX_DIST)
			return -1;

	//get final time
	int final = TIMER0_GetValue();

	//calculate the time of Echo Up
	int time = final - initial;

	//calculate the distance
	int distance = time/58;

	//wait at least 60 milliseconds to channel stabilize
	delay(60, miliseconds);

	return distance;
}
