/**
* @file		RTC.h
* @brief	Contains the RTC API.
* @author	Diogo Dias
*/

/* structure used to set date/time */
typedef struct {
	unsigned int sec;     /* Second value - [0,59] */
    unsigned int min;     /* Minute value - [0,59] */
    unsigned int hour;    /* Hour value - [0,23] */
    unsigned int Mday;    /* Day of the month value - [1,31] */
    unsigned int mon;     /* Month value - [1,12] */
    unsigned int year;    /* Year value - [0,4095] */
    unsigned int Wday;    /* Day of week value - [0,6] */
    unsigned int Yday;    /* Day of year value - [1,365] */
} RTCTime;

#define RTC_CLOCK_ENABLE 1
#define CLEAR_INCREMENT_INTERRUPT 1
#define ENABLE_INCREMENT_INTERRUPT_HOUR 1<<2

/** 
 * @brief  Configures RTC peripheral
 * @note   MUST be called before any other RTC functions
 * @return None
 */
void RTC_Init(void);

/** 
 * @brief  Enables the RTC peripheral to start 
 * @note   To change time/date RTC_SetTime must be called before this function
 * @return None
 */
void RTC_Start(void);

/** 
 * @brief  Change the time and date of RTC peripheral
 * @note   To a normal use of RTC, this funcion must be called when RTC is disable
 * @param  time: structure that contains the new data and time to be set
 * @return None
 */
void RTC_SetTime(RTCTime * time);

/** 
 * @brief  Enables RTC interrupts
 * @return None
 */
void RTC_EnableInterrupt(void);

/** 
 * @brief  Disables RTC interrupts
 * @return None
 */
void RTC_DisableInterrupt(void);

/** 
 * @brief  Enables/Disables the counters to be compared with the alarm, to generate an interrupt
 * @param  AlarmMask: value to set the alarm mask register
 * @return None
 */
void RTC_SetAlarmMask(int AlarmMask);

/** 
 * @brief  Enables/Disables to generate an interrupt every time a counter is incremented
 * @param  intIncrement: value to set the increment interrupt register
 * @return None
 */
void RTC_SetInterruptIncrement(int intIncrement);

/** 
 * @brief  Check if one day has passed
 * @note   This function clears dayPassed variable
 * @return 1 if one day has passed from last clear, or 0 if not 
 */
int RTC_dayHasPassed(void);
