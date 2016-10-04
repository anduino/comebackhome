int led1 = 2;
int led2 = 3;
int tmpPin = A0;

String strData;
bool isOn = false;

void setup() {
  // put your setup code here, to run once:
  Serial3.begin(9600);
  //Serial.begin(9600);
  pinMode(led1, OUTPUT);
  pinMode(led2, OUTPUT);

  digitalWrite(led1, LOW);
  digitalWrite(led2, LOW);
}

void loop() {

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

      if(cname == "lamp")
      {
        if(corder == "1")
        {
          if(isOn == false)
          {
            digitalWrite(led1, HIGH);
            digitalWrite(led2, HIGH);
            Serial.println("On");
            Serial3.write("normalStatus/lamp/1");
            isOn = true;
          }
        }
        else if(corder =="0")
        {
          if(isOn == true)
          {
            digitalWrite(led1, LOW);
            digitalWrite(led2, LOW);
            Serial.println("Off");
            Serial3.write("normalStatus/lamp/0");
            isOn = false;
          }
        }
      }
    }
    else if(protocol == "requestStatus"){

      String sname = strData.substring(index+1, strData.length());

      if(sname == "temperature"){
        int rawVol = analogRead(tmpPin);
        float celsiusTmp = (5.0 * rawVol * 100.0) / 1024.0;

        Serial.println(celsiusTmp);

        String message = "realtimeStatus/"+sname+"/"+String(celsiusTmp);
        
        Serial3.print(message);
        
        
      }   
    }
   
    
  }
  delay(200);
}

