#include "RTC.h"
#include "lpc17xx.h"

//boolean that is '1' when one day is passed, this bit is cleared when dayHasPassed is called
static int dayPassed;
static int day;


void RTC_Init(void){
	//Enable CLOCK into RTC
	LPC_SC->PCONP |= (1 << 9);

	//Disable RTC
	LPC_RTC->CCR = 0;

	//Alarm Disable
	LPC_RTC->AMR = 0xFF;

	//Clear interrupts
	LPC_RTC->ILR = 0x3;

	//Disable increment interrupts
	LPC_RTC->CIIR = 0;

	//Initialize variables
	dayPassed = 0;
}

void RTC_EnableInterrupt(void){
	NVIC_EnableIRQ(RTC_IRQn);
}

void RTC_DisableInterrupt(void){
	NVIC_DisableIRQ(RTC_IRQn);
}

void RTC_SetAlarmMask(int AlarmMask){
	LPC_RTC->AMR = AlarmMask;
}

void RTC_SetInterruptIncrement(int intIncrement){
	LPC_RTC->CIIR = intIncrement;
}

void RTC_Start(void){
	LPC_RTC->CCR |= RTC_CLOCK_ENABLE;
}

void RTC_SetTime(RTCTime * time){
	//set date/time
	LPC_RTC->SEC = time->sec;
	LPC_RTC->MIN = time->min;
	LPC_RTC->HOUR = time->hour;
	LPC_RTC->DOM = time->Mday;
	LPC_RTC->DOW = time->Wday;
	LPC_RTC->DOY = time->Yday;
	LPC_RTC->MONTH = time->mon;
	LPC_RTC->YEAR = time->year;

	//save initial day
	day = time->Mday;
}

int RTC_dayHasPassed(void){
	int aux = dayPassed;
	if(aux)
		dayPassed = 0;
	return aux;
}


void RTC_IRQHandler(void){
	//clear interrupt flag
	LPC_RTC->ILR = CLEAR_INCREMENT_INTERRUPT;

	//signal if a day passed
	if(day != LPC_RTC->DOM){
	  dayPassed = 1;
	  day = LPC_RTC->DOM;
	}
}
