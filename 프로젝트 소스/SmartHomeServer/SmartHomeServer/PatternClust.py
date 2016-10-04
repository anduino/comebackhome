def enum(**enums):
    return type('Enum', (), enums)
import sqlite3







def haversine(lon1,lat1,lon2,lat2): # 전체 시스템에 들어갈 함수.
    '''위도와 경도를 이용해 두점사이의 대원거리(great circle distance)를 계산함.
    (위도와 경도는 degree로 계산)'''
    lon1,lat1,lon2,lat2 = map(radians,[lon1,lat1,lon2,lat2])

    dlon = lon2 - lon1
    dlat = lat2 - lat1
    
    a = sin(dlat / 2) ** 2 + cos(lat1) * cos(lat2) * sin(dlon / 2) ** 2
    c = 2 * atan2(sqrt(a),sqrt(1 - a))

    Km = 6367 * c
    return Km


from math import *

#from pylab import plot,show,xlim,ylim
from numpy import vstack,array
import random
import numpy as np
from numpy.random import rand
from scipy.cluster.vq import kmeans2,vq,kmeans  




DEF = enum(MAX_CLUSTER = 9,        #최대 클러스터 생성 수
    TEMP_GAP = 0.5,         #gaussian dist x축 값
    DATA_THERESHOLD = 10,   #데이터 개수 문턱값
    DIST_STD_THRESHOLD = 1, #표준 편차 문턱값(위치 km 단위)
    TIME_STD_THRESHOLD = 2, #표준 편차 문턱값(시간)
    CLUSTER_ITER = 50       #클러스터링 반복 횟수
    )

class PatternCluster:
    '''입력으로 받은 2차원 벡터를 클러스터링하는 클래스'''
    def __init__(self ,name, data=[]):

        self.data = data
        self.dataNum = len(data)
        self.optimize_k = None
        self.selected = []
        self.centroids = []
        self.idx = None
        self.name = name

        #self.pd = PatternDistribution(data)

    def setData(self,data):
        self.data = data
        self.dataNum = len(data)
        self.optimize_k = None
        self.selected.clear()

    def addData(self,data):
        self.data.extend(data)
        self.dataNum = self.dataNum + len(data)

    def run(self):
        '''클러스터링 작업을 위한 메인 함수'''

        self.optimize_k = self.elbowMethod()
        #print("optimize k = " + str(self.optimize_k))

        # elbow method 로 구한 k를 이용하여 클러스터링
        #centroids,_ = kmeans2(self.data, self.optimize_k,minit='points')
        centroids,_ = kmeans(self.data, self.optimize_k,iter= DEF.CLUSTER_ITER)
        idx,_ = vq(self.data, centroids, self.optimize_k)
        self.idx = idx
        self.centroids = centroids

        #print(self.name)
        for i in range(self.optimize_k):# 0축 -> 시간 // 1축 -> 거리 #
            mu = np.mean(self.data[idx == i],axis=0)
            std = np.std(self.data[idx == i],axis=0)
            cdn = len(self.data[idx == i]) # cluster data num
            
            #print(str(i)+"번째 클러스터")
            #print("평균 :"+ str(mu))
            #print("개수 :"+ str(cdn))

            # data threshold
            if(cdn < DEF.DATA_THERESHOLD):
                #print("데이터 개수 부족")
                #print()
                continue
            ## end threshold loop
            self.selected.append(i)
            #print()
        if(len(centroids) == 4):
            #plot(self.data[idx==0,0],self.data[idx==0,1],'or')
            #plot(self.data[idx==1,0],self.data[idx==1,1],'og')
            #plot(self.data[idx==2,0],self.data[idx==2,1],'ob')
            #plot(self.data[idx==3,0],self.data[idx==3,1],'oy')
            #plot(centroids[:,0],centroids[:,1],'sg',markersize=8)
            #show()
            #print()
            pass


    def getClusterNum(self):
        return self.optimize_k


    def getCluster(self):
        return self.centroids, self.idx, self.selected

    def elbowMethod(self):
        ''' SSE에 대한 미분변화율을 이용하여 optimize된 클러스터의 값을 구함 '''

        SSE = []
        #SSE(Sum of Squared Error)계산
        for k in range(1, DEF.MAX_CLUSTER + 1):
            #centroids,_ = kmeans2(self.data, k,minit='points')
            centroids,_ = kmeans(self.data, k,iter= DEF.CLUSTER_ITER)
            idx,_ = vq(self.data, centroids, k)

            value = self.calculateSSE(centroids, idx, k)
            SSE.append(value)
            #print("SSE = "+ str(value) + " (클러스터 갯수 = "+ str(k) + ")")
        
        #draw = [1,2,3,4,5,6,7,8,9]
        #plot(draw,SSE)
        #show()

        SSE_VAR_1 = [] 
        #1차 변화량
        for i in range(0,len(SSE) - 1):
            value = SSE[i + 1] - SSE[i]
            SSE_VAR_1.append(value)

        SSE_VAR_2 = []
        #2차 변화량
        for i in range(0, len(SSE) - 2):
            value = SSE_VAR_1[i + 1] - SSE_VAR_1[i]
            SSE_VAR_2.append(value)
        i = np.array(SSE_VAR_2).argmax()
        
        return (i + 2) # optimize k


    def calculateSSE(self, centroid, idx, cnum):
        '''
        sum of squared error(SSE)를 계산하는 함수
        클러스터비용을 계산한다. 클러스터비용은 각 클러스터에서 중심점과 클러스
        터 안의 다른 데이터 간 거리합이다.
        '''
        sum = 0
        for cn in range(cnum):
            if(len(self.data[idx == cn]) == 0):
                continue
            value = 0
            for dn in range(len(self.data[idx == cn])): # centroid과 클러스터 거리
                ## 2d distance
                value = value + self.dist(centroid[cn], self.data[idx == cn][dn])

                ## haversine distance
                #value = value +
                #self.haversine(centroid[cn][0],centroid[cn][1],self.data[idx
                #== cn][dn][0],self.data[idx == cn][dn][1])
            sum = sum + value
        return sum

    def haversine(self,lon1,lat1,lon2,lat2): # 전체 시스템에 들어갈 함수.
        '''위도와 경도를 이용해 두점사이의 대원거리(great circle distance)를 계산함.
        (위도와 경도는 degree로 계산)'''
        lon1,lat1,lon2,lat2 = map(radians,[lon1,lat1,lon2,lat2])

        dlon = lon2 - lon1
        dlat = lat2 - lat1
    
        a = sin(dlat / 2) ** 2 + cos(lat1) * cos(lat2) * sin(dlon / 2) ** 2
        c = 2 * atan2(sqrt(a),sqrt(1 - a))

        Km = 6367 * c
        return Km

    def dist(self, p1, p2):
        '''get 2d distance'''
        return np.sqrt((p1[0] - p2[0]) ** 2 + (p1[1] - p2[1]) ** 2) ** 2
        #return (abs(p1[0] - p2[0]) +abs(p1[1] - p2[1]))


