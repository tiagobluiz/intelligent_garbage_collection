/**
* @file		OneWire.h
* @brief	Contains the OneWire API.
* @author	Diogo Dias
*/

/**
 * @param 	pin: pin number to communicate, starts at 0
 * @brief 	Configure GPIO port to communicate
 * @return 	None
 * @note 	This function must be the first one to be called
 */
void OneWire_Init(int pin);


/**
 * @param 	Void
 * @brief	Sends initial command Reset
 * @return  0 if nobody send presence pulse, different of '0' otherwise
 */
int OneWire_Reset(void);

/**
 * @param 	bit: int where the less significant bit is the bit to write
 * @brief	Puts High or Low the Port with number 'pin', depending on value bit
 * @return 	None
 */
void OneWire_WriteBit(int bit);

/**
 * @param	Void
 * @brief 	Reads one bit of the bus
 * @return  '0' if as readed '0' or returns different of '0' if as readed 1
 */
int OneWire_ReadBit(void);


/**
 * @param	byte: to write
 * @brief	Writes one byte to the bus, it uses OneWire_WriteBit
 * @return 	None
 */
void OneWire_Write(int byte);

/**
 * @param	Void
 * @brief	Reads one byte from the bus, it uses OneWire_ReadBit
 * @return 	the byte read
 */
char OneWire_Read(void);

/**
 * @param	buffer: structure that contains the bytes to Write
 * @param 	count: number of bytes in the buffer
 * @brief	Writes a series of bytes in the bus, it uses OneWire_Write
 * @return 	None
 */
void OneWire_WriteBytes(char * buffer, int count);

/**
 * @param	buffer: structure to save the bytes read
 * @param 	count: number of bytes to read, and length of the buffer
 * @brief 	Reads a series of bytes from the bus, it uses OneWire_Read
 * @return 	None
 */
void OneWire_ReadBytes(char *buffer, int count);
