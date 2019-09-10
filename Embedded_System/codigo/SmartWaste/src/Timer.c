#include "LPC17xx.h"
#include "Timer.h"
#include "SystemTick.h"

#define TIMER_ON 1
#define TIMER_OFF 2

void TIMER0_Init(){
	LPC_TIM0->TCR = TIMER_OFF;
	LPC_SC->PCLKSEL0 &= ~(3<<2);
	LPC_SC->PCLKSEL0 |= 1 << 2;
	NVIC_EnableIRQ(TIMER0_IRQn);
}

unsigned int TIMER0_GetValue(void){
	return LPC_TIM0->TC;
}

unsigned int TIMER0_Elapse(unsigned int lastRead){
	return LPC_TIM0->TC - lastRead;
}

void TIMER0_SetCount(int timeMagnitude){
	LPC_TIM0->TCR = TIMER_OFF;
	LPC_TIM0->PR = (((SystemCoreClock )) / timeMagnitude) - 1;
	LPC_TIM0->MCR = 0;
	LPC_TIM0->EMR = 0;
	LPC_TIM0->CTCR = 0;
	LPC_TIM0->TCR = TIMER_ON;
}

void TIMER0_SetWait(int timeMagnitude, int waitTime){
	LPC_TIM0->TCR = TIMER_OFF;
	LPC_TIM0->IR = 1;
	LPC_TIM0->PR = (((SystemCoreClock )) / timeMagnitude) - 1;
	LPC_TIM0->MCR = MR0_ENABLE_INTERRUPT | MR0_STOP_TIMER;
	LPC_TIM0->EMR = 0;

	//Convert waitTime to seconds
	LPC_TIM0->MR0 = waitTime * 60;

	//start time
	LPC_TIM0->TCR = TIMER_ON;
}

void TIMER0_Disable(void){
	LPC_TIM0->TCR = TIMER_OFF;
}

void TIMER0_IRQHandler (void){
	//clear interrupt flag
	if ( LPC_TIM0->IR & 1 )
		LPC_TIM0->IR = 1;
}
