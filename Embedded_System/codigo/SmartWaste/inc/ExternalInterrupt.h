/**
* @file		ExternalInterrupt.h
* @brief	Contains the External Interrupt API.
* @author	Diogo Dias
*/

//pin that is connect with the switch in the binCover
#define WASTE_BIN_COVER 11 				//P2.11

//bit to enable EINT1 external interrupt
#define EINT1 1<<1

/** 
 * @brief  Enables external interrupt for pin EINT1
 * @return None
 */
void enableExternalInterrupt(void);

/** 
 * @brief  disables external interrupt for pin EINT1
 * @return None
 */
void disableExternalInterrupt(void);

/** 
 * @brief  Configures pin EINT1 to enable external interrupts  
 * @return None
 * @note   This function must be called before the others in this file
 */
void configureExternalInterrupt(void);
