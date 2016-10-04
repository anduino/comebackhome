import sqlite3
import pymysql as mysql
import pickle

def connect_db():
    return mysql.connect(host='localhost',user='root', passwd='apmsetup', db='anduino')

db = connect_db()
cur = db.cursor()

cur.execute("select lat,lng, date, remark from control where corder = '1' and cname = 'aircon'")

allData = []
locData = []
timeData = [] 
tempData = [] 

for row in cur.fetchall():
    allData.append(row)
    locData.append([row[0],row[1]])
    timeData.append([row[2],0])
    tempData.append([row[3],0])

jitem = {"item" : allData}
 

f = open("item.txt", 'wb')
pickle.dump(allData, f)
#a = pickle.load(f)
#print(a)
#print(len(a))
f.close()