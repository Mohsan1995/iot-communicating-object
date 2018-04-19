#include <Servo.h>


/* Const for pin */
const byte TRIGGER_PIN = 3; // Pin TRIGGER
const byte ECHO_PIN = 4;    // Pin ECHO
const byte MONSERVO_PIN = 2;// Pin ENGIGNE

/* Const fo pin RGB */
const byte PIN_LED_R = 11;
const byte PIN_LED_B = 10;
const byte PIN_LED_G = 9;

const float SOUND_SPEED = 340.0 / 1000;
/* Const for timeout */
const unsigned long MEASURE_TIMEOUT = 25000UL; // 25ms = ~8m à 340m/s

int MAX_DIST_BEFORE_OPEN = 50;

int color_detect_r = 0;
int color_detect_g = 255;
int color_detect_b = 0;

int color_undetect_r = 255;
int color_undetect_g = 0;
int color_undetect_b = 0;

int distance_cm = 0;


Servo monservo;  // crée l’objet pour contrôler le servomoteur

/* Speed of the sound in the air in mm/us */


String bluetoothInput = "";
boolean b = false;


boolean doorOpen = false;


void displayColor(byte r, byte g, byte b);

void setup() {
  // put your setup code here, to run once:
 
  Serial.begin(9600);
  Serial.println("Type AT commands!");
  
  // The HC-06 defaults to 9600 according to the datasheet.
  Serial1.begin(9600);
  /* Init pin for the sound wave*/

  pinMode(TRIGGER_PIN, OUTPUT);
  digitalWrite(TRIGGER_PIN, LOW); // La broche TRIGGER doit être à LOW au repos
  pinMode(ECHO_PIN, INPUT);

  pinMode(PIN_LED_R, OUTPUT);
  pinMode(PIN_LED_G, OUTPUT);
  pinMode(PIN_LED_B, OUTPUT);
  
  monservo.write(0);
  monservo.attach(MONSERVO_PIN);  // utilise la broche 9 pour le contrôle du servomoteur

  displayColor(0, 0, 0);

}

void loop() {
  if (Serial1.available()) {
    char c = (char)Serial1.read();
    if (c == '@') {
      if (b) {
        onBluetoothIncomingMessage(bluetoothInput);
        bluetoothInput = "";
        b = false;
      } else {
        bluetoothInput = "";
        b = true;
      }
    } else {
      bluetoothInput += String(c);
    }
  }

  
   // 1. First Launc a sound from trigger for determine the distance
  digitalWrite(TRIGGER_PIN, HIGH);
  delayMicroseconds(10);
  digitalWrite(TRIGGER_PIN, LOW);
  
  // 2. Evaluate the time between the trigger of the sound and his echo
  long measure = pulseIn(ECHO_PIN, HIGH, MEASURE_TIMEOUT);
   
  // 3. Calculate the distance between  
  float distance_mm = measure / 2.0 * SOUND_SPEED;
  int new_distance_cm = distance_mm / 10.0;
  Serial.print("Distance cm: ");
  Serial.println(distance_cm);

  //if (distance_cm != new_distance_cm) {
    writeToBT(String("D#") + String(distance_cm));
  //}


  distance_cm = new_distance_cm;
  
  if(distance_cm >= 1 && distance_cm < MAX_DIST_BEFORE_OPEN){
    monservo.write(90); 
    if (!doorOpen) {
      doorOpen = true;
      writeToBT("DOOR#1");
    }
  } else {
    monservo.write(0);
    if (doorOpen) {
      doorOpen = false;
      writeToBT("DOOR#0");
    }
  }

  invalideColor();

  delay(300);
}

void displayColor(byte r, byte g, byte b) {
  analogWrite(PIN_LED_R, r);
  analogWrite(PIN_LED_G, g);
  analogWrite(PIN_LED_B, b);
}



void writeToBT(String s) {
  Serial.print("Write to BT: ");
  Serial.println(s);
  
  Serial1.write("@");
  delay(30);
  for (int i = 0; i < s.length(); i++) {
    char c = s.charAt(i);
    
    Serial1.write(s.charAt(i));
    delay(30);
  }
  Serial1.write("$");
  delay(30);
}

void onBluetoothIncomingMessage(String msg) {
  if (msg == "INIT") {
    char hexoutColorDetected[7];
    sprintf(hexoutColorDetected,"%02x%02x%02x", color_detect_r, color_detect_g, color_detect_b);

    char hexoutColorUndetected[7];
    sprintf(hexoutColorUndetected,"%02x%02x%02x", color_undetect_r, color_undetect_g, color_undetect_b);
    
    writeToBT(String("DT#") + String(MAX_DIST_BEFORE_OPEN));
    writeToBT(String("CD#") + String(hexoutColorDetected));
    writeToBT(String("CU#") + String(hexoutColorUndetected));
    writeToBT(String("DOOR#") + String(doorOpen ? "1" : "0"));
    return;
  }


  
  if (msg.length() <= 3) {
    return;
  }
  String cmd = msg.substring(0, 2);
  String arg = msg.substring(3, msg.length());
  Serial.println(cmd);
  Serial.println(arg);
  if (cmd == "DT") {
    MAX_DIST_BEFORE_OPEN = arg.toInt();
    writeToBT(msg);
  } else if (cmd == "CD") {
    String hexstring = String("#") + arg;
    long number = (long) strtol( &hexstring[1], NULL, 16);
    color_detect_r = number >> 16 & 0xFF;
    color_detect_g = number >> 8 & 0xFF;
    color_detect_b = number & 0xFF;
    writeToBT(msg);
    invalideColor();
  } else if (cmd == "CU") {
    String hexstring = String("#") + arg;
    long number = (long) strtol( &hexstring[1], NULL, 16);
    color_undetect_r = number >> 16;
    color_undetect_g = number >> 8 & 0xFF;
    color_undetect_b = number & 0xFF;
    writeToBT(msg);
    invalideColor();
  }
}

void invalideColor() {
  if (doorOpen) {
    displayColor(color_detect_r, color_detect_g, color_detect_b);
  } else {
    displayColor(color_undetect_r, color_undetect_g, color_undetect_b);
  }
}

