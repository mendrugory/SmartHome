#include <SPI.h>
#include <Ethernet.h>

//CONSTANTS
#define PIN_NUMBER 33
#define STATUS 115
#define ON 110
#define OFF 111
#define HEADER 104
#define FOOTER 102
#define PORT 80
#define BUFFER_SIZE 3
#define ERROR  70
#define SUCCESS  83

byte buffer[BUFFER_SIZE];
boolean currentStatus = false;

// network configuration.  gateway and subnet are optional.

 // the media access control (ethernet hardware) address for the shield:
byte mac[] = { 0xDE, 0xAD, 0xBE, 0xEF, 0xFE, 0xED };  
//the IP address for the shield:
byte ip[] = { 192, 168, 1, 200};    
// the router's gateway address:
byte gateway[] = {192, 168, 1, 1};
// the subnet:
byte subnet[] = {255, 255, 255, 0};

// http defaults to port 80
EthernetServer server = EthernetServer(PORT);

void setup()
{
  // initialize the ethernet device
  Ethernet.begin(mac, ip, gateway, subnet);

  // init the pin
  pinMode(PIN_NUMBER, OUTPUT);

  //welcome
  welcome();

  // start listening for clients
  server.begin();
}


void loop()
{
  // if an incoming client connects, there will be bytes available to read:
  EthernetClient client = server.available();
  if (client == true) 
  {
    delay(500);
    work(&client);
    client.stop();
  }
}

void welcome()
{
  digitalWrite(PIN_NUMBER, HIGH);   
  delay(1000);               
  digitalWrite(PIN_NUMBER, LOW);   
  delay(500);  
  digitalWrite(PIN_NUMBER, HIGH);   
  delay(1000);               
  digitalWrite(PIN_NUMBER, LOW);   
  delay(500); 
  digitalWrite(PIN_NUMBER, HIGH);   
  delay(1000);               
  digitalWrite(PIN_NUMBER, LOW); 
}  

void work(EthernetClient *client)
{

  const unsigned char header = client->read();
  if(header != HEADER)
  {
    return;
  }

  const unsigned char action = client->read();
  if(action == 0xFF)
  {
    return;
  }

  const unsigned char footer = client->read();
  if(footer != FOOTER)
  {
    return;
  }

  unsigned char response[BUFFER_SIZE];
  int length = build_response(action, response);

  for(int i=0; i<length; i++)
  {
    client->write(response[i]);
  }

}    

int build_response(const unsigned char action, unsigned char *response)
{
  response[0] = HEADER;

  unsigned char result;
  switch (action) 
  {
      case STATUS:
        result = (currentStatus) ? ON : OFF;
        break;
      case ON:
        digitalWrite(PIN_NUMBER, HIGH);
        result = SUCCESS;
        currentStatus = true;
        break;
      case OFF:
        digitalWrite(PIN_NUMBER, LOW);
        result = SUCCESS;
        currentStatus = false;
        break;
      default:
        result = ERROR;
  }

  response[1] = result;
  response[2] = FOOTER;

  return BUFFER_SIZE;
}       