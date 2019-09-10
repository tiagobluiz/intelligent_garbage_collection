#include <Wire.h>
#include <SigFox.h>
#include <ArduinoLowPower.h>

//max payload size that a receive message can contain
#define SIGFOX_MAX_RECEIVE_PAYLOAD 8
//max payload size that a transmit message can contain
#define SIGFOX_MAX_TRANSMITE_PAYLOAD 12

#define INPUT_PIN_REQUEST 0
#define OUTPUT_PIN_BUSY 1

//buffer that will contain the bin configuration
char configuration [SIGFOX_MAX_RECEIVE_PAYLOAD] = {0,0,0,80,30,0,0,0};
//configuration already obtained
bool configurationUpdated = false;
//dummy message to obtain our configuration 
byte dummyMessage [SIGFOX_MAX_TRANSMITE_PAYLOAD] = {0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF};
//configuration requested
bool requested = false;

void setup() {
  //join i2c bus with address #0
  Wire.begin(0);      
  //register event            
  Wire.onRequest(requestEvent);   
  //register event
  Wire.onReceive(receiveEvent);   

  //wire to read requests
  pinMode(INPUT_PIN_REQUEST, INPUT_PULLUP);      
  attachInterrupt(digitalPinToInterrupt(INPUT_PIN_REQUEST), requestConfiguration, FALLING);
  //wire to notify is busy 
  pinMode(OUTPUT_PIN_BUSY, OUTPUT); 
  //start idle            
  digitalWrite(OUTPUT_PIN_BUSY, LOW); 
}

void requestConfiguration(){
  requested = true;
}

void loop() {
  someoneRequested();
  __WFI();
  delay(1);
}

void someoneRequested(){
   //check if someone requested configuration
  if(requested && !configurationUpdated){
    //change state to busy
    digitalWrite(1, HIGH);

    delay(1100);

    //get configuration from Sigfox Cloud
    sendRequestAndGetResponse();
    configurationUpdated = true;
    requested = false;
      
    //change state to idle
    digitalWrite(1, LOW);
  }
}

// function that executes whenever data is requested by master
// this function is registered as an event
void requestEvent() {
  //write configuration
  Wire.write(configuration, SIGFOX_MAX_RECEIVE_PAYLOAD);       
}

// function that executes whenever a slave device receives a transmission from a master.
// this function is registered as an event
void receiveEvent(int howMany) {
  //don't do anything if it was read request
  if(howMany == 0)
    return;

  //tell i'm busy  
  digitalWrite(1, HIGH);

  //prepare variables
  byte buff [SIGFOX_MAX_TRANSMITE_PAYLOAD];
  int i = 0; 

  //start read
  while (howMany-->0)
    buff[i++]= Wire.read();                 

  //send information read to Sigfox Cloud
  sendRequest(buff);

  //change state to Idle
  digitalWrite(1, LOW);
}

void sendRequest(byte str [SIGFOX_MAX_TRANSMITE_PAYLOAD]) {
  //start request
  startRequest(str);

  // send buffer to SIGFOX network
  SigFox.endPacket();
 
  SigFox.status(SIGFOX);
  SigFox.status(ATMEL);
  
  SigFox.end();
}

void sendRequestAndGetResponse() {  
  //start request with dummy message 
  startRequest(dummyMessage);

  //send buffer to SIGFOX network and wait for a response
  SigFox.endPacket(true);  

  //check if packed received
  if (SigFox.parsePacket()) {
    //read packet from Sigfox module
    int i = 0;
    while (SigFox.available()) {
      configuration[i] = (char)SigFox.read();
      i++;
    }
  }
  SigFox.end();
}

void startRequest(byte buff [SIGFOX_MAX_TRANSMITE_PAYLOAD]){
  // Start the module
  SigFox.begin();
  
  // Wait at least 30mS after first configuration (100mS before)
  delay(100);

  //enable debug to actively wait
  SigFox.debug();
  
  // Clears all pending interrupts
  SigFox.status();
  delay(1);

  //start write message to Sigfox module
  SigFox.beginPacket();
  SigFox.write(buff, SIGFOX_MAX_TRANSMITE_PAYLOAD);
}


