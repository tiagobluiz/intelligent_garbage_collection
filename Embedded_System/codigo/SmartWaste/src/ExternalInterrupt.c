#include "ExternalInterrupt.h"
#include "LPC17xx.h"

// function to be called when an external interrupt 1 happens
void EINT1_IRQHandler(void){
	LPC_SC->EXTINT = EINT1;
}

void enableExternalInterrupt(void){
	LPC_SC->EXTINT = EINT1;
	NVIC_EnableIRQ(EINT1_IRQn);
}

void disableExternalInterrupt(void){
	NVIC_DisableIRQ(EINT1_IRQn);
}

void configureExternalInterrupt(void){
	//select external interrupt 1 for pin P2.11 - function 01
	LPC_PINCON->PINSEL4 &= ~(3<<(WASTE_BIN_COVER*2));
	LPC_PINCON->PINSEL4 |= (1<<(WASTE_BIN_COVER*2));

	LPC_SC->EXTMODE = EINT1;
	LPC_SC->EXTPOLAR = 0;

	enableExternalInterrupt();
}
