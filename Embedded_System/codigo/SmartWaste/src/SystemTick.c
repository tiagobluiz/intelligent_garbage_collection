#include "SystemTick.h"
#include "LPC17xx.h"
#include <stdio.h>

static int timerSignaled;

void delay(int time, int timeMagnitude){
	int a = SystemCoreClock / timeMagnitude;
	int wait = a * time;
	SysTick_Config(wait);

	while(!timerSignaled);

	//disable System Tick
	SysTick->CTRL = 4;
	timerSignaled = 0;
}

void SysTick_Handler(void){
	timerSignaled = 1;
}
