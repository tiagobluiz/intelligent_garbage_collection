#include "OneWire.h"
#include "LPC17xx.h"
#include "GPIO.h"
#include "SystemTick.h"

static int pin;

void OneWire_Init(int iopin){
	pin = iopin;
	GPIO_SetPinFunction(pin, GPIO_PORT);
	GPIO_SetIOPinDirection(pin, INPUT);
}

int OneWire_Reset(void){
	// wait for channel be high to initialize reset
	int numberOfTrys = 10;
	while(!(GPIO_GetIOPin(pin)&(1<<pin)))
		if(!numberOfTrys--)
			return 0;

	// pulling the channel low for 480 microseconds
	GPIO_SetIOPin(pin, 0);
	delay(480, microseconds);

	//go to receive mode and wait 60 microseconds to 240 microseconds max
	//to read the channel
	GPIO_SetIOPinDirection(pin, INPUT);
	delay(80, microseconds);

	//Read the channel and if is low, the slave is present
	int sensorSignal = !(GPIO_GetIOPin(pin) & (1<<pin));

	//Wait at least 480 microseconds to channel stabilize
	delay(480, microseconds);

	return sensorSignal;
}

void OneWire_WriteBit(int bit){
	if(bit){
		//pull the channel low at max 15 microseconds to initialize
		GPIO_SetIOPin(pin, 0);
		delay(7, microseconds);

		//pull the channel high to write 1
		GPIO_SetIOPin(pin, 1);
		delay(65, microseconds);
	}
	else {
		//pull the channel low at least 60 microseconds to initialize and write 0
		GPIO_SetIOPin(pin, 0);
		delay(65, microseconds);

		//pull the channel high to let channel stabilize
		GPIO_SetIOPin(pin, 1);
		delay(5, microseconds);
	}
}

int OneWire_ReadBit(void){

	//pull the bus low for a minimum 1 microsecond to initiate
	GPIO_SetIOPin(pin, 0);
	delay(2, microseconds);

	//release the bus and read the bus within 15 microseconds
	GPIO_SetIOPinDirection(pin, INPUT);
	delay(6, microseconds);

	int bit = GPIO_GetIOPin(pin) & (1 << pin);

	//all read time slots must have a minimum of 60 microseconds
	delay(60, microseconds);

	return bit;
}

void OneWire_Write(int byte){
	for(int i = 0; i < 8; i++, byte >>= 1)
		OneWire_WriteBit(byte & 1);
}

char OneWire_Read(){
	char byte = 0;
	for(char bitMask = 0x1; bitMask; bitMask<<=1)
		if(OneWire_ReadBit())
			byte|=bitMask;
	return byte;
}

void OneWire_WriteBytes(char * buffer, int count){
	for(int i = 0; i < count; i++)
		OneWire_Write(buffer[i]);
}


void OneWire_ReadBytes(char *buffer, int length) {
  for (int i = 0 ; i < length ; i++){
    buffer[i] = OneWire_Read();
  }
}
