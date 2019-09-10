/**
* @file		gpio.h
* @brief	Contains the GPIO API.
* @author	Diogo Dias
*/

#ifndef _GPIO_H_
#define _GPIO_H_

#define INPUT 0
#define OUTPUT 1

//function number to change a pin to GPIO0
#define GPIO_PORT 0

/**
 * @brief	Sets the received pin into a specified direction
 * @param	pin	: The pin to change direction
 * @param	direction : The direction to be used by the pin (0 = INPUT; 1 = OUTPUT)
 * @return  None
 * @note	This function is called always before a read or write operation to a pin to change it's direction to the desired one
 */
void GPIO_SetIOPinDirection(int pin, int direction);

/**
 * @brief	Gets the received pin value to the received state
 * @param	pin	: The pin to put input direction
 * @return  state of GPIO0 pins
 */
int GPIO_GetIOPin(int pin);

/**
 * @brief	Sets the pin to OUTPUT and value to the received state
 * @param	pin	: The pin to change OUTPUT value
 * @param	state : The desired state of the pin, it can be '0' or '1'
 * @return  None
 */
void GPIO_SetIOPin(int pin, int state);

/**
 * @brief	Sets the function to be used in a pin
 * @param	pin	: The pin to set the function
 * @param	funcId	: The function the pin will take
 * @return  None
 */
void GPIO_SetPinFunction(int pin, int funcId);

#endif
