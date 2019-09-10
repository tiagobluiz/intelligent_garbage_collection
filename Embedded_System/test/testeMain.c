/*
int main(void) {
	SystemCoreClockUpdate();
*/

	/*
	int temperatureMax = 40;
	int temperatureMin = 0;

	int contentorHeight = 10;

	//configure Real Time Clock
	RTCTime rtc;
	rtc.Mday = 0;
	rtc.Wday = 0;
	rtc.Yday = 0;
	rtc.hour = 0;
	rtc.min = 0;
	rtc.mon = 0;
	rtc.sec = 0;
	rtc.year = 0;

	RTC_Init();
	RTC_SetTime(&rtc);
	RTC_Start();


	// start UltrasonicSensor
    UltrasonicSensor_Init(TRIGGER_PIN, ECHO_PIN);

    //start TemperatureSensor
    TemperatureSensor_Init(TEMPERATURE_PIN, TEMPERATURE_CONVERSION, temperatureMin, temperatureMax);

    while(1){
    	int hasAlarm = TemperatureSensor_HasAlarm();
    	int distance = UltrasonicSensor_getDistance(contentorHeight);

    	int waitTime = 0;

    	TIMER0_SetWait(second, waitTime);

    	__WFI();
    }

    */
/*
    TemperatureSensor_Init(TEMPERATURE_PIN, TEMPERATURE_CONVERSION, 2, 30);

    while(1){
     	int convert = TemperatureSensor_GetTemperature();
		printf("Temperature = %d \n", convert);
    	int alarm = TemperatureSensor_HasAlarm();
		printf("Alarm = %d \n", alarm);
    }
*//*
	TIMER0_Init(second);
	UltrasonicSensor_Init(TRIGGER_PIN, ECHO_PIN);
	while(1){
		printf("Distance %d \n", UltrasonicSensor_getDistance(300));
	}
	*/

	/*
	TIMER0_Init(second);
	TIMER0_SetWait(second, 1);
	while(1){

		printf("Time %d \n", TIMER0_GetValue());
		TIMER0_SetWait(second, 1);
	}

	*/


	//select external interrupt 1 for pin P0.11 - function 01
/*
	LPC_PINCON->PINSEL4 &= ~(3<<(WASTE_BIN_COVER*2));
	LPC_PINCON->PINSEL4 |= (1<<(WASTE_BIN_COVER*2));

	//enable external interrupt 1 to interrupt
	LPC_SC->EXTMODE = EINT1;
	LPC_SC->EXTPOLAR = EINT1;
	NVIC_EnableIRQ(EINT1_IRQn);


	while(1){
		__WFI();
		printf("GPIO Interrupt  %d\n", LPC_GPIO2->FIOPIN);
	}

    return 1;
}
*/