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

int distance_cm = 0;


Servo monservo;  // crée l’objet pour contrôler le servomoteur

/* Speed of the sound in the air in mm/us */


void displayColor(byte r, byte g, byte b)

void setup() {
  // put your setup code here, to run once:
 
  Serial.begin(115200);
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
   // 1. First Launc a sound from trigger for determine the distance
  digitalWrite(TRIGGER_PIN, HIGH);
  delayMicroseconds(10);
  digitalWrite(TRIGGER_PIN, LOW);
  
  // 2. Evaluate the time between the trigger of the sound and his echo
  long measure = pulseIn(ECHO_PIN, HIGH, MEASURE_TIMEOUT);
   
  // 3. Calculate the distance between  
  float distance_mm = measure / 2.0 * SOUND_SPEED;
   
  //Show the distance in mm,cm and m
  Serial.print(F("Distance: "));
  Serial.print(distance_mm);
  Serial.print(F("mm ("));
  Serial.print(distance_mm / 10.0, 2);
  Serial.print(F("cm, "));
  Serial.print(distance_mm / 1000.0, 2);
  Serial.println(F("m)"));

  distance_cm = distance_mm / 10.0;
  Serial.print(distance_cm);
  if(distance_cm >= 1 && distance_cm < MAX_DIST_BEFORE_OPEN){
    monservo.write(90); 
    displayColor(0, 255, 0);
  }else{
    monservo.write(0);
    displayColor(255, 0, 0);
  }
  
  delay(100);

}

void displayColor(byte r, byte g, byte b) {
  analogWrite(PIN_LED_R, r);
  analogWrite(PIN_LED_G, g);
  analogWrite(PIN_LED_B, b);
}
