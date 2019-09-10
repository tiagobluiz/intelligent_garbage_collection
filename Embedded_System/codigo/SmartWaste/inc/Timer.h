/**
* @file		Timer.h
* @brief	Contains the TIMER API.
* @author	Diogo Dias
*/

#ifndef _TIMER_H_
#define _TIMER_H_

#define MR0_ENABLE_INTERRUPT 1
#define MR0_STOP_TIMER 1<<2

/**
 * @brief	Initializes the LED API
 * @return	Nothing
 * @note	This function must be called prior to any other TIMER functions.
 */
void TIMER0_Init();

/**
 * @brief	Get's the current ticks
 * @return	Nothing
 * @note	The ticks are based on the frequency given on the initialization
 */
unsigned int TIMER0_GetValue(void);

/**
 * @brief	Get's the difference between the current ticks and the received parameter
 * @param	lastRead : ticks read last time
 * @return	Nothing
 */
unsigned int TIMER0_Elapse(unsigned int lastRead);

/**
 * @brief	disables the timer
 * @return	Nothing
 */
void TIMER0_Disable(void);

/**
 * @brief	Starts the timer to count at timeMagnitude unities
 * @param 	timeMagnitude: the unity of time, the value must be one represented in enum magnitude
 * @return	Nothing
 */
void TIMER0_SetCount(int timeMagnitude);

/**
 * @brief	Starts the timer to count at timeMagnitude unities, and enables an Interrupt at waitTime
 * @param 	timeMagnitude: the unity of time, the value must be one represented in enum magnitude
 * @param 	waitTime: time (in minutes) that will happen an Interrupt
 * @return	Nothing
 */
void TIMER0_SetWait(int timeMagnitude, int waitTime);

#endif
