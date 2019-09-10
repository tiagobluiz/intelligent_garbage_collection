#include "LPC17xx.h"
#include "GPIO.h"

void GPIO_SetIOPinDirection(int pin, int direction){
	if(direction == OUTPUT)
		LPC_GPIO0->FIODIR |= 1 << pin;
	else
		LPC_GPIO0->FIODIR &= ~(1 << pin);
}

void GPIO_SetIOPin(int pin, int state){
    GPIO_SetIOPinDirection(pin, OUTPUT);
    if(state == 0)
    	LPC_GPIO0->FIOCLR |= 1 << pin;
	else
		LPC_GPIO0->FIOSET |= 1 << pin;
}

int GPIO_GetIOPin(int pin){
	GPIO_SetIOPinDirection(pin, INPUT);
	return LPC_GPIO0->FIOPIN;
}

void GPIO_SetPinFunction(int pin, int funcId){
	pin *=2;
	if(pin < 16)
		LPC_PINCON->PINSEL0 = (LPC_PINCON->PINSEL0&(~(0x3<<pin))) | funcId<<pin;
	else{
		LPC_PINCON->PINSEL1 = (LPC_PINCON->PINSEL1&(~(0x3<<pin))) | funcId<<pin;
	}
}
