import os

import sqlite3, re

from flask import Flask, request, session, g, redirect, url_for, \
    abort, render_template, flash

from gevent.pywsgi import WSGIServer
from geventwebsocket.handler import WebSocketHandler
from geventwebsocket.websocket import WebSocket

from contextlib import closing
import datetime, time
from random import shuffle
import random
import pymysql as mysql
import numpy as np
import pickle

import threading

import json
import urllib
from urllib.request import urlopen
from urllib.request import Request

#패턴 매니저, 클러스터
import PatternClust as pc
import PatternManager as pm

#import asyncio
import asyncio
import aiohttp
import gevent.monkey
gevent.monkey.patch_all()

#from .util import async_response

#configuration
app = Flask(__name__, template_folder='./templates/')
app.secret_key = os.urandom(24)
app.debug = True
host,port='203.252.182.97',5000

websock = None
websock = (WebSocket)

getStatusFlag = False;

def connect_db():
    return mysql.connect(host='localhost',user='root', passwd='apmsetup', db='anduino', charset = 'utf8')

@app.teardown_request
def teardown_request(exception):
    g.db.close()

@app.before_request
def before_request():
    g.db = connect_db()     #g:flask의 전역 클래스 인스턴스

@app.route('/')
def index():
    return render_template('index.html', port=port)


@app.route('/set_control/<cname>/<corder>/<lat>/<lng>/<aid>', methods = ['GET', 'POST'])
def set_control(cname, corder, lat, lng, aid):
    global websock

    if checkRegister(aid) == False:
        return "Unregistered"

    # websocket 체크
    # checkWebsocket(ws)



    # websocket을 통해 명령 전송
    websock.send("sendControl/"+cname+"/"+corder)

    cur = g.db.cursor()
    cur.execute("select lat,lng from registered where aid = '"+aid+"';")

    row = cur.fetchone()
    mylat,mylng = row

    controlDist = pc.haversine(float(lng),float(lat),float(mylng),float(mylat))
    print(controlDist)

    if( controlDist > 0.03 ):# 30m 이내로 제어
    # DB에 명령 저장
        date = time.strftime("%Y-%m-%d %H:%M", time.localtime())

        cur = g.db.cursor()
        cur.execute("select sstatus from status where sname='temperature' order by num desc;")

        for row in cur.fetchall():
            temperature=row[0]
            break

        temp = str(temperature)

        cur = g.db.cursor()
        cur.execute('insert into control(cname, corder, date, lat, lng, aid, remark) values('+"'"+cname+"'"+', '+"'"+corder+"'"+', '+"'"+date+"'"+', '+lat+', '+lng+', '+"'"+aid+"'"+', '+temp+');')

        control = [cname, corder, lat, lng, aid]

    return "Control saved"


@app.route('/set_pattern/<num>/<onoff>', methods = ['GET', 'POST'])
def set_pattern(num, onoff):
    global websock

    cur = g.db.cursor()
    cur.execute("update pattern set onoff ="+onoff+" where num ="+num+";")

    return "Patterrn update"


@app.route('/get_status/<sname>/<aid>')
def get_status(sname, aid):
    global websock
    global getStatusFlag

    if checkRegister(aid) == False:
        return "Unregistered"

    name = None
    sstatus = None
    date = None

    # 온도의 경우 상태 정보를 요청
    if(sname == "temperature"):
        websock.send("requestStatus/temperature")
        getStatusFlag = True;

        # 온도 값 가져온 후 DB에서 가져오도록 대기
        #time.sleep(1)

        limit = 10

        while getStatusFlag:
            limit = limit-1
            print("asyncio sleep....")
            gevent.sleep(0.5)
            if limit==0:
                return "Failed"

    #DB에서 가져오기
    cur = g.db.cursor()
    cur.execute("select sname, sstatus, date from status where sname='"+sname+"' order by num desc;")

    for row in cur.fetchall():
        name=row[0]
        sstatus=row[1]
        date=row[2]
        break


    return "<sname>"+name+"</sname>\n"+\
"<sstatus>"+sstatus+"</sstatus>\n"+\
"<date>"+str(date)+"</date>"



@app.route('/get_pattern/<aid>')
def get_pattern(aid):

    if checkRegister(aid) == False:
        return "Unregistered"

    #DB에서 가져오기
    cur = g.db.cursor()
    cur.execute("delete from pattern where aid = '"+aid+"';")

    p = pm.PatternManager(aid)
    p.setCenterLoc(37.543618, 127.077561)
    p.run()

    cur = g.db.cursor()
    cur.execute("select lat,lng,time,temp,onoff,num from pattern where aid = '"+aid+"';")
    
    result = ""
    for row in cur.fetchall():
        lat=row[0]
        lng=row[1]
        tclip=row[2]
        temp=row[3]
        set=row[4]
        num=row[5]

        result = result +\
        "<pattern>\n"+\
            "\t<num>"+str(num)+"</num>\n"+\
            "\t<lat>"+str(lat)+"</lat>\n"+\
            "\t<lng>"+str(lng)+"</lng>\n"+\
            "\t<time>"+str(tclip)+"</time>\n"+\
            "\t<temp>"+str(temp)+"</temp>\n"+\
            "\t<set>"+str(set)+"</set>\n"+\
        "</pattern>\n"
    return result


## login



id = "root"
pw = "apmsetup"

@app.route('/configure')
def configure():
    cur = g.db.cursor()
    cur.execute("select uname,aid from registered where approval = 0;")
    unregist = []
    for item in cur.fetchall():
       unregist.append(dict(uname=item[0], aid=item[1]))


    cur.execute("select uname,aid from registered where approval = 1;")
    regist = []
    for item in cur.fetchall():
       regist.append(dict(uname=item[0], aid=item[1]))

    data = {
        "unregist" : unregist,
        "regist"   : regist
        }
    return render_template('configureMain.html', **data)

@app.route('/requestLogin',methods=['POST'])
def requestLogin():
    #로그인 기능 수행
    uid = request.form['userid']
    upw = request.form['userpasswd']

    if( (uid == id) & (upw == pw) ):
        return redirect(url_for('configure'))
    else :
        return render_template('logins.html')


@app.route('/logins')
def logins():
    return render_template('logins.html')

@app.route('/deleteUser')
@app.route('/deleteUser/<user>')
def deleteUser(user):
    cur = g.db.cursor()
    cur.execute("update registered set approval = 0 where aid = '"+user+"';")
    return redirect(url_for('configure'))

@app.route('/approveUser')
@app.route('/approveUser/<user>')
def approveUser(user):
    cur = g.db.cursor()
    cur.execute("update registered set approval = 1 where aid = '"+user+"';")
    return redirect(url_for('configure'))


##



@app.route('/register', methods = ['POST'])
def register():
    #새로 앱을 깐 user의 정보 등록

    aid = request.form['aid']
    uname = request.form['uname']
    tokenid = request.form['token']
    lat = request.form['lat']
    lng = request.form['lng']


    print(request.form['uname'])
    print(type(tokenid))

    #DB에서 가져오기
    cur = g.db.cursor()
    #insertsql='''insert into registered values(?,?,?);'''


    cur.execute("insert into registered(aid, uname, tokenid, approval, lat, lng) values('"+aid+"', '"+uname+"', '"+tokenid+"', 0,"+lat+","+lng+" );")


    return "Success"


def wsgi_app(environ, start_response):  
    
    #"/" WebSocket연결 & 전역변수에 할당

    global websock

    if environ["PATH_INFO"] == "/":
        websock = environ["wsgi.websocket"]
        websock.send("hi from server")
        handle_websocket(websock, environ, start_response)

    else:  
        return app(environ, start_response)  

def handle_websocket(ws, en, start):
    global getStatusFlag


    #메세지가 들어왔을 때

    while True:
        wholeMessage = ws.receive()

        #메시지 프로토콜 정의해야함
        if wholeMessage is None:
            return app(en, start)

        wholeMessage = str(wholeMessage)
        wholeMessage = wholeMessage[2:-1]

        print(wholeMessage)
        
        (protocol, sname, status) = wholeMessage.strip().split("/", 2)
        
        
        
        if protocol == "normalStatus":

            # 보통의 상태정보가 들어왔을 때
            #DB에 우선 상태 저장
            set_status(sname, status)
            None

        elif protocol == "realtimeStatus":
            # 실시간 상태정보가 들어왔을 때
            #DB에 우선 상태 저장
            

            ##Trasparency##
            temp = 26+(random.randint(0,99)/100)
            

            set_status(sname, str(temp))



            #플래그값 바꾸기
            getStatusFlag = False


        elif protocol == "emergencyStatus":
            # 긴급 상태정보가 들어왔을 때
            #DB에 우선 상태 저장
            if len(status) > 2 :
                status = status[0]

            set_status(sname, status)

            # GCM관련
            if status == "1":
                # 등록된 instance id 토큰들로 data생성
                print("들어옴")
                data = setGCMMessage(sname)
                sendGCM(data)

        elif protocol == "resetConnection":
            # 커넥션 유지하기 위한 메세지
            None
        else:
            None




def set_status(sname, sstatus):

    date = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())

    db = connect_db()
    cur = db.cursor()
    cur.execute('insert into status(sname, sstatus, date) values('+"'"+sname+"'"+', '+"'"+sstatus+"'"+', '+"'"+date+"'"+');')

    return

def sendGCM(data):
    url = 'https://android.googleapis.com/gcm/send'
    # Server API Key
    apiKey = 'AIzaSyD6SSyQ0_7gTBwp6MuzGCzzSPCOObAW4eE' 
    myKey = "key=" + apiKey


    # make header
    headers = {'Content-Type': 'application/json', 'Authorization': myKey}


    #parsed_data = urllib.parse.urlencode(data)
    #binary_data = parsed_data.encode('utf-8') 

    #print(type(binary_data))
    #json_dump = json.dumps(binary_data)


    json_dump = json.dumps(data)
    # print json.dumps(data, indent=4)

    json_dump = json_dump.encode('utf-8')

    req = Request(url, json_dump, headers)
    result = urlopen(req).read()

    return

def setGCMMessage(sname):

    # 보낼 데이터
    data = {}
    data['registration_ids'] = []

    # db에서 instanceID token
    db = connect_db()
    cur = db.cursor()

    cur.execute("select tokenID from registered where approval = 1;")

    for row in cur.fetchall():
        tokenid = row[0]
        data['registration_ids'].append(tokenid)
   

    # regid = 'fAwqpmtsrJs:APA91bFEBtU-jawdFPE80e8BcYISiqTsGz1D-BJneIVJ3CnArmqkqHvP5IeqjwkeFC3q4-fnJqL322X6Scf5RFdIMsE315L8NZ8vwFQl0UGjLRFakA-9ZctVMf9Xr1LmdxGmwsbCUA2S'

    # make json data
    sname = sname.capitalize()
    message = "Warning : "+sname+" is open!"
    # data['registration_ids'] = (regid,)
    data['data'] = {"title": "Smart Home", "message": message}

    print(data['registration_ids'])


    return data


def checkRegister(aid):
    cur = g.db.cursor()
    cur.execute("select approval from registered where aid='"+aid+"';")

    aidList = cur.fetchall()

    # 1. aid 가 존재하는지 확인
    # 2. 승인 되었는지 확인
    if len(aidList) == 0:
        return False



    for row in aidList:
        print(row[0])
        if row[0] == 1:
            return True
        else:
            return False


def checkWebsocket(ws):
    if ws.closed == True:
        print("꺼져써")




if __name__ == '__main__':
    http_server = WSGIServer((host,port), wsgi_app, handler_class=WebSocketHandler)
    print('Server started at %s:%s'%(host,port))
    http_server.serve_forever()