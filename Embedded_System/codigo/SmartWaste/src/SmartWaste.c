/*
 ===============================================================================
 Name        : SmartWaste.c
 Author      : Diogo Dias
 Version     : Beta
 Description : main definition
 ===============================================================================
 */

#ifdef __USE_CMSIS
#include "LPC17xx.h"
#endif

#include <stdio.h>
#include <cr_section_macros.h>
#include "GPIO.h"
#include "SystemTick.h"
#include "TemperatureSensor.h"
#include "UltrasonicSensor.h"
#include "RTC.h"
#include "Timer.h"
#include "Sigfox.h"
#include "ExternalInterrupt.h"

#define ECHO_PIN 7
#define TRIGGER_PIN 8
#define TEMPERATURE_PIN 9
#define TEMPERATURE_CONVERSION 9
#define WAIT_COVER_TIME 30 					//in seconds
#define ENABLE_CLOCK_POWER_TIMER0 1<<1
#define ENABLE_CLOCK_POWER_GPIO 1<<15


int waitForCoverToClose() {

	//start time
	TIMER0_SetCount(second);
	int start = TIMER0_GetValue();

	//wait because of bounce
	while (TIMER0_Elapse(start) < 1);

	//wait to echo go low
	while (LPC_GPIO2->FIOPIN & (1 << WASTE_BIN_COVER))
		if (TIMER0_Elapse(start) > WAIT_COVER_TIME)
			return 0;

	start = TIMER0_GetValue();
	//wait because of bounce
	while (TIMER0_Elapse(start) < 1);

	return 1;
}

void enableWakeUpInt(int coverClosed, int sleepTime) {
	//if nobody closed the cover, we will set a timer to wake up periodic
	if (!coverClosed)
		TIMER0_SetWait(second, sleepTime);

	//enable wake up from external
	enableExternalInterrupt();
}

void disableWakeUpInt() {
	//disable external wake up
	disableExternalInterrupt();

	//stop timer
	//we always disable because its more fast than checking 'coverClosed' variable
	TIMER0_Disable();
}

void compactInformation(char * buffer, int temperature, int percentage, int isCover) {
	buffer[0] = 0;
	buffer[1] = 0;
	buffer[2] = 0;
	buffer[3] = 0;
	buffer[4] = 0;
	buffer[5] = 0;
	buffer[6] = 0;
	buffer[7] = 0;
	buffer[8] = 0;
	buffer[9] = isCover;
	buffer[10] = percentage;
	buffer[11] = temperature;
}

int readPercentageOfGarbage(int maxDistance) {
	int distance = UltrasonicSensor_getDistance();
	if (distance == -1 || distance > maxDistance)
		return -1;

	int percentage = distance * 100;
	percentage /= maxDistance;
	percentage = 100 - percentage;
	return percentage;
}

void init() {
	//get value core clock
	SystemCoreClockUpdate();

	//only enable clock/power to TIMER0 and GPIO
	LPC_SC->PCONP = ENABLE_CLOCK_POWER_TIMER0 | ENABLE_CLOCK_POWER_GPIO;

	//enable external interrupt
	configureExternalInterrupt();

	//start UltrasonicSensor
	UltrasonicSensor_Init(TRIGGER_PIN, ECHO_PIN);

	//start TemperatureSensor
	TemperatureSensor_Init(TEMPERATURE_PIN, TEMPERATURE_CONVERSION, 1, 50);

	//start timer
	TIMER0_Init(second);

	//start Sigfox to communicate
	Sigfox_Init();

	//start a default timer structure
	RTCTime time;
	time.sec = 0;
	time.min = 0;
	time.hour = 0;
	time.Mday = 1;
	time.Wday = 0;
	time.Yday = 1;
	time.mon = 1;
	time.year = 2000;

	//start RTC
	RTC_Init();
	RTC_SetTime(&time);
	RTC_SetInterruptIncrement(ENABLE_INCREMENT_INTERRUPT_HOUR);
	RTC_EnableInterrupt();
	RTC_Start();
}

int main(void) {
	//start all peripherals
	init();

	//get configuration with Sigfox cloud
	char receiveBuffer [SIGFOX_RECEIVE_FRAME_LENGTH] = {0,0,0,0,0,0,0,0};
	while(!Sigfox_Read(receiveBuffer, SIGFOX_RECEIVE_FRAME_LENGTH)){
		TIMER0_SetWait(second, 1*60);									//sleep for 1 hour
		__WFI();
	}

	//parse configurations
	int maxDistance = ((unsigned char)receiveBuffer[SIFOX_PARSE_CONTENTOR_LENGTH_0]<<24) | (receiveBuffer[SIFOX_PARSE_CONTENTOR_LENGTH_1]<<16)
			| (receiveBuffer[SIFOX_PARSE_CONTENTOR_LENGTH_2]<<8) | receiveBuffer[SIFOX_PARSE_CONTENTOR_LENGTH_3];
	int sleepTime = (unsigned char)receiveBuffer[SIFOX_PARSE_IDLE_TIME];

	int oldPercentage = -5;
	int coverClosed = 1;


	while (1) {
		int temperature = TemperatureSensor_GetTemperature();
		int percentage = readPercentageOfGarbage(maxDistance);

		//if one day has passed, the percentage of garbage grown up 5% or bin has been cleared, we notify the central server
		if (RTC_dayHasPassed() || percentage > oldPercentage + 4 || oldPercentage > percentage) {

			//save last percentage
			oldPercentage = percentage;

			//write information to buffer and transmit
			char sendBuffer [SIGFOX_TRANSMITE_FRAME_LENGTH];
			compactInformation(sendBuffer, temperature, percentage, coverClosed);
			Sigfox_Write(sendBuffer, SIGFOX_TRANSMITE_FRAME_LENGTH);
		}

		coverClosed = LPC_GPIO2->FIOPIN & (1 << WASTE_BIN_COVER) ? 0 : 1;
		//enable interrupts before going to sleep, and enable when we wake up
		enableWakeUpInt(coverClosed, sleepTime);
		__WFI();
		disableWakeUpInt();

		//wait for coverage to close
		coverClosed = waitForCoverToClose();
	}
}
