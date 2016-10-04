int INA = 9;
int INB = 8;
int tmpPin = A0;

String strData;
bool isOn = false;

void setup() {
  // put your setup code here, to run once:
  Serial3.begin(9600);
  //Serial.begin(9600);
  pinMode(INA,OUTPUT);
  pinMode(INB,OUTPUT);

}

void loop() {

  if(Serial3.available() > 0 )
  {
    strData = Serial3.readString();
    //Serial.println(strData);
    int index = strData.indexOf('/');
    String protocol = strData.substring(0, index);
    //Serial.println(protocol);

    if(protocol == "sendControl"){
      String rest = strData.substring(index+1, strData.length());
      
      index = rest.indexOf('/');
      String cname = rest.substring(0, index);
      
      //Serial.println(cname);
      String corder = rest.substring(index+1, rest.length());
      //Serial.println(corder);

      if(cname == "aircon")
      {
        if(corder == "1")
        {
          if(isOn == false)
          {
            digitalWrite(INB, HIGH);
            //Serial.println("On");
            Serial3.write("normalStatus/aircon/1");
            isOn = true;
          }
        }
        else if(corder =="0")
        {
          if(isOn == true)
          {
            digitalWrite(INB, LOW);
            //Serial.println("Off");
            Serial3.write("normalStatus/aircon/0");
            isOn = false;
          }
        }
      }
    }
        
  }
  delay(500);
}
