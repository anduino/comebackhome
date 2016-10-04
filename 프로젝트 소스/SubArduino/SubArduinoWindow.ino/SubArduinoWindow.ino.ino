int enablePin_1 = A0;
int in1Pin = 10;
int in2Pin = 9;

int magPin = 7;
 
int nSwitch = 0;
int flag = 0;
boolean bReverse = false;
 
int nSpeed = 255;
int prevState;
// 1-> open
// 0-> close

unsigned long duration = 3000; // ms 단위(2초)
unsigned long nextTogle;

String strData;

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  Serial3.begin(9600);

  pinMode(enablePin_1, OUTPUT);
  pinMode(in1Pin, OUTPUT);
  pinMode(in2Pin, OUTPUT);

  pinMode(magPin,INPUT);
  prevState = 0;
}

void loop() {

    int state = digitalRead(magPin);
    Serial.println(state);
    if(state == 0)//닫힘
    {
      if(prevState == 1)//이전 상태가 열려있었다면(상태가 변경)
      {
        Serial3.write("emergencyStatus/window/0");
      }
      prevState = 0;
    } else {
      if(prevState == 0)
      {
        Serial3.write("emergencyStatus/window/1");
      }
      prevState = 1;
   }
  delay(200);  

  if(Serial3.available() > 0 )
  {
    strData = Serial3.readString();
    Serial.println(strData);
    int index = strData.indexOf('/');
    String protocol = strData.substring(0, index);
    Serial.println(protocol);

    if(protocol == "sendControl"){
      String rest = strData.substring(index+1, strData.length());
      
      index = rest.indexOf('/');
      String cname = rest.substring(0, index);
      //Serial.println(cname);
      String corder = rest.substring(index+1, rest.length());
      //Serial.println(corder);

      if(cname == "window")
      {
        if(corder == "1")
        {
          //Serial3.write("normalStatus/window/1");
          //setMotor(nSpeed, !bReverse);
          windowControl(1);
        }
        else if(corder =="0")
        {
          //Serial3.write("normalStatus/window/0");
          //setMotor(nSpeed, bReverse);
          windowControl(0);
        }
      }
    }
    
  }
  delay(200);
}

void windowControl(int control) {
  nextTogle = millis() + duration;

  while(millis() < nextTogle)
  {
    if(control == 1)
    {
      setMotor(nSpeed, !bReverse);
    }else if(control == 0)
    {
      setMotor(nSpeed, bReverse);
    }
  }
  setMotor(0, bReverse);
}

void setMotor(int nSpeed, boolean bReverse)
{

  analogWrite(enablePin_1, nSpeed);
  digitalWrite(in1Pin, bReverse);
  digitalWrite(in2Pin, !bReverse);
}

