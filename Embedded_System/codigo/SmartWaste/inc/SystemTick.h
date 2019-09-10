/**
* @file		SystemTick.h
* @brief	Contains the System Tick API.
* @author	Diogo Dias
*/

#ifndef _SystemTick_H_
#define _SystemTick_H_

enum magnitude {
	second = 1,
    miliseconds = 1000,
    microseconds = 1000000
};

/**
 * @param	time: The time that core will be idle
 * @param 	timeMagnitude: the unity of time, the value must be one represented in enum magnitude
 * @brief	will put the processor in state idle for a limited and variable time
 * @return 	Void
 */
void delay(int time, int timeMagnitude);

#endif
