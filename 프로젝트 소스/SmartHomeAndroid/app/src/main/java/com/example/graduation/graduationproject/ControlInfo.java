package com.example.graduation.graduationproject;

/**
 * Created by KIMYEACHAN on 2016-01-15.
 */
public class ControlInfo {
    private String sname = null;
    private String sstatus = null;;
    private String sdate = null;;
    private double temp = 0;

    ControlInfo(){}
    ControlInfo(String name,String status, String date){
        this.sname = name;
        this.sstatus = status;
        this.sdate = date;
    }

    ControlInfo(String name,String status, String date,double temp){
        this.sname = name;
        this.sstatus = status;
        this.sdate = date;
        this.temp = temp;
    }

    public void setSname(String _sname){sname = _sname;}
    public void setSstatus(String _sstatus){sstatus = _sstatus;}
    public void setSdate(String _sdate){sdate = _sdate;}
    public void setTemp(double temp){this.temp = temp;}

    public String getSname(){return sname;}
    public String getSstatus(){return sstatus;}
    public String getSdate(){return sdate;}
    public Double getTemp(){return this.temp;}

}
