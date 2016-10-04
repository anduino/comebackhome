import PatternClust as clust
import datetime
import pickle
import pymysql as mysql
import math
from numpy import vstack
import numpy as np

def enum(**enums):
    return type('Enum', (), enums)

def connect_db():
    return mysql.connect(host='localhost',user='root', passwd='apmsetup', db='anduino')

DEF = enum(
    TIME_DIFF = 30
    )

class PatternData:
    ''' 추출된 패턴에 대한 정보를 담는 class '''
    def __init__(self, lat, lng, time, temp):
        self.lat = lat
        self.lng = lng
        self.time = time
        self.temp = temp
    def show(self):
        print("Pattern : ",end="")
        print("lat = " + str(self.lat) + " lng = " + str(self.lng) + " time = " + str(self.time) + " temp = " + str(self.temp))


class PatternManager:
    # 디비에 접근해서 time, (lat,lng), temp 를 분배해서 클러스터링을 하도록 하는 클래스.
    def __init__(self,aid):
        #디비 커넥션
        self.aid = aid
        self.clat = 0
        self.clng = 0
        self.timeCluster_weekday = clust.PatternCluster("time weekday") # time에 관한 cluster - time  (평일)
        self.timeCluster_weekend = clust.PatternCluster("time weekend") # time에 관한 cluster - time  (주말)

        self.locCluster_weekday =  clust.PatternCluster("location weekday") # 위치에 관한 cluster - lat, lng (평일)
        self.locCluster_weekend =  clust.PatternCluster("location weekend") # 위치에 관한 cluster - lat, lng (주말)

        self.tempCluster =  clust.PatternCluster("temperature") # temperature에 관한 cluster - temp
        
        self.timeData_weekday = []
        self.timeData_weekend = []

        self.locData_weekday = []
        self.locData_weekend = []

        self.tempData_weekday = []
        self.tempData_weekend = []

        self.pData_weekday = []
        self.pData_weekend = []

    def setCenterLoc(self, lat, lng):
        self.clat = lat
        self.clng = lng


    def haversine(self, lon1,lat1,lon2,lat2): # 전체 시스템에 들어갈 함수.
        '''위도와 경도를 이용해 두점사이의 대원거리(greate circle distance)를 계산함.
        (위도와 경도는 degree로 계산)'''
        lon1,lat1,lon2,lat2 = map(math.radians,[lon1,lat1,lon2,lat2])

        dlon = lon2-lon1
        dlat = lat2-lat1
        
        a = math.sin(dlat/2)**2 + math.cos(lat1)*math.cos(lat2)*math.sin(dlon/2)**2
        c = 2*math.atan2(math.sqrt(a),math.sqrt(1-a)) 

        Km = 6367*c
        return Km

    def vectoring(self):
        
        db = connect_db()
        cur = db.cursor()

        # aid 조건 추가
        cur.execute("select lat,lng, date, remark from control where corder = '1' and cname = 'aircon' and aid = '"+self.aid+"';")

        for row in cur.fetchall():

            # time 
            value = float(int((row[2].hour*60 + row[2].minute)/DEF.TIME_DIFF))
            
            # lat, lng
            if(row[2].weekday() > 4):
                self.locData_weekend.append([row[0],row[1]])
                self.timeData_weekend.append([value,0])
                self.tempData_weekend.append(row[3])
            else:
                self.locData_weekday.append([row[0],row[1]])
                self.timeData_weekday.append([value,0])
                self.tempData_weekday.append(row[3])


        self.locData_weekend = vstack(self.locData_weekend)
        self.locData_weekday = vstack(self.locData_weekday)

        self.timeData_weekend = vstack(self.timeData_weekend)
        self.timeData_weekday = vstack(self.timeData_weekday)

        self.tempData_weekday = vstack(self.tempData_weekday)
        self.tempData_weekend = vstack(self.tempData_weekend)

    def run(self):
        # db tuple -> vector data
        self.vectoring()

        #print("WEEKDAY CLUSTERING")
        # 위치에 대한 클러스터링 수행 (weekday)
        self.locCluster_weekday.setData(self.locData_weekday)
        self.locCluster_weekday.run()
        cent, idx, sel = self.locCluster_weekday.getCluster() # centroid , data index, selected cluster

        for i in sel: # 위치 클러스터링 결과 -> 시간 클러스터링, 온도 mu 값 
            temp = np.mean(self.tempData_weekday[idx==i])

            self.timeCluster_weekday.setData(self.timeData_weekday[idx==i])
            self.timeCluster_weekday.run()
            tcent, _, tsel = self.timeCluster_weekday.getCluster()

            for t in tsel:
                self.pData_weekday.append(PatternData(cent[i][0],cent[i][1],int(math.floor(tcent[t][0]+0.5)), temp))

        for j in self.pData_weekday:
            j.show()
            

        # 위치에 대한 클러스터링 수행 (weekend)
        #print("WEEKEND CLUSTERING")
        self.locCluster_weekend.setData(self.locData_weekend)
        self.locCluster_weekend.run()
        cent, idx, sel = self.locCluster_weekend.getCluster() # centroid , data index, selected cluster

        for i in sel: # 위치 클러스터링 결과 -> 시간 클러스터링, 온도 mu 값 
            temp = np.mean(self.tempData_weekend[idx==i])

            self.timeCluster_weekend.setData(self.timeData_weekend[idx==i])
            self.timeCluster_weekend.run()
            tcent, _, tsel = self.timeCluster_weekend.getCluster()

            for t in tsel:
                self.pData_weekend.append(PatternData(cent[i][0],cent[i][1],math.floor(tcent[t][0]+0.5), temp))

        #for j in self.pData_weekend:
        #    j.show()
        self.insertDB()
        return

    def insertDB(self):
        # DB insert 수행-> 이미 존재하는 지 확인 -> 없으면 insert.
        db = connect_db()

        cur = db.cursor()

        deleteSql = "delete from pattern;"
        cur.execute(deleteSql)

        for item in self.pData_weekday: 
            dist = self.haversine(self.clng, self.clat, item.lng, item.lat)

            insertSql = "insert into pattern(aid, lat, lng, dist, time, temp, onoff)"\
               +" values('"+self.aid+"', "+str(float(item.lat))+", "+str(float(item.lng))+", "+str(float(dist))+", "+str(int(item.time))+", "+str(float(item.temp))+", 0);"
            cur.execute(insertSql)

#p = PatternManager("bfb13243ad6157b2")#37.543618, 127.077561
#p.setCenterLoc(37.543618, 127.077561)
#p.run()

