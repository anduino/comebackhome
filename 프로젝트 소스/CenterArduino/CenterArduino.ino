/*
 * 중앙 아두이노
 * ParsingFromServer
 * 웹서버로부터 명령을 받아서 서브아두이노로 송신한다.
 * 서브 아두이노로부터 상태를 받아 웹서버로 송신하다.
 */

#include <Arduino.h>
//#include <Base64.h>
//#include <sha1.h>
//#include <WebSocket.h>
#include <SPI.h>
#include <Ethernet.h>
#include <TextFinder.h>

#include <WebSocketsClient.h>

// Mac주소. 여기선 디폴트
byte mac[] = { 0xDE, 0xAD, 0xBE, 0xEF, 0xFE, 0xED };
// 웹서버의 ip주소
IPAddress server(203,252,182,96);   
// 아두이노에 할당할 ip주소
IPAddress ip(192, 168, 227, 18);

// Control 관련 변수들
// Control테이블의 attribute
char cname[50]; char corder[10];
String inMessage;

//명령 관련 변수들
String control;
String query;
String strData;

String test = "aircon/1";

//상태 관련 변수들
String sname;
String sstatus;

//웹소켓 클라이언트
WebSocketsClient webSocket;


int cnt = 0;

/*
 * 함수들
*/
//웹소켓 이벤트
void webSocketEvent(WStype_t type, uint8_t * payload, size_t length) {

    switch(type) {
      
        //웹소켓 disconnected
        case WStype_DISCONNECTED:
            Serial.println("disconnected please");
            
            break;
            
        //웹소켓 connected
        case WStype_CONNECTED:
            Serial.println("Ws Connected");
//            webSocket.sendTXT("Connected/Hi/arduino");
            break;
            
        //웹소켓에서 텍스트 데이터가 들어왔을 때
        //(명령을 서브아두이노로 송신)
        case WStype_TEXT:
          inMessage = (char*)payload;
          Serial3.print(inMessage);
          Serial.println(inMessage);
          break;
            
        //웹소켓에서 바이너리 데이터가 들어왔을 때
        case WStype_BIN:
//            Serial.print("[WSc] get binary length: ");
//            Serial.println(length);
          break;
    }

}



//서브 아두이노에서 받은 데이터 서버로 송신
void setStatus(){
  

  if(Serial3.available() > 0){ 
    Serial.println("dataIn");
    
    //서브 아두이노에서 받은 데이터가 있으면
    strData = Serial3.readString();
    Serial.println(strData);
    webSocket.sendTXT(strData);

//    //이더넷 연결
//    EthernetClient client;
//    if (client.connect(server, 5000))
////    Serial3.println("connected");
////  else
////    Serial3.println("connection failed");
//
//    //sub에서 보낸 sname이랑 sstatus를 변수에 저장
//    strData = Serial3.readString();
//
//    int index = strData.indexOf('/');
//    sname = strData.substring(0, index);
//    sstatus = strData.substring(index+1, strData.length());
//
//    //웹서버에 HTTP request:
//    query = "GET /set_status/"+sname+"/"+sstatus+" HTTP/1.0";
//    
//    client.println(query);
//    client.println("Connection: close");
//    client.println();

    delay(200);
  }
  //서브 아두이노에서 받은 데이터가 없으면
  return;
}


void setup() {
  // 시리얼 모니터 시작
  Serial.begin(9600);
  Serial3.begin(9600);

  Serial.println("before connect ethernet");
  //이더넷 연결 시작
  Ethernet.begin(mac);
  //이더넷 연결 시간 주기
  delay(1000);
  Serial.println("start(ethernet connect complete)");
  
  Serial.println(Ethernet.localIP());

  //웹소켓 연결 시작
  webSocket.begin("203.252.182.96", 5000);
  //웹소켓 이베벤트 등록
  webSocket.onEvent(webSocketEvent);


}


void loop()
{
  cnt++;

  //서브 아두이노가 보내는 데이터(상태)있으면 받기
  setStatus();
  
  //웹소켓 유지
  webSocket.loop();

  delay(200);

  if(cnt==900){
    cnt=0;
    webSocket.sendTXT("resetConnection/null/null");
  }

}

