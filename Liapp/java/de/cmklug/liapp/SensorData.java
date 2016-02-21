package de.cmklug.liapp;


public class SensorData {

    int _id;
    String _sensor_id;
    String _version;
    int _time_left;
    int _last_scan_sensor_time;
    String _start_date;
    String _expire_date;

    // Empty constructor
    public SensorData(){

    }
    // constructor
    public SensorData(int id, String sensor_id, String version, int time_left, int last_scan_sensor_time, String start_date, String expire_date){
        this._id = id;
        this._sensor_id = sensor_id;
        this._version = version;
        this._time_left = time_left;
        this._last_scan_sensor_time = last_scan_sensor_time;
        this._start_date = start_date;
        this._expire_date = expire_date;

    }

    // constructor
    public SensorData(String sensor_id, String version, int time_left, int last_scan_sensor_time, String start_date, String expire_date){
        this._sensor_id = sensor_id;
        this._version = version;
        this._time_left = time_left;
        this._last_scan_sensor_time = last_scan_sensor_time;
        this._start_date = start_date;
        this._expire_date = expire_date;    }

    // getting ID
    public int getID(){
        return this._id;
    }

    // setting id
    public void setID(int id){
        this._id = id;
    }

    // getting sensor_id
    public String getSensorID(){
        return this._sensor_id;
    }

    // setting sensor_id
    public void setSensorID(String sensor_id){
        this._sensor_id = sensor_id;
    }

    // getting version
    public String getVersion(){
        return this._version;
    }

    // setting version
    public void setVersion(String version){
        this._version = version;
    }

    // getting time_left
    public int getTimeLeft(){
        return this._time_left;
    }

    // setting time_left
    public void setTimeLeft(int time_left){
        this._time_left = time_left;
    }

    // getting last_scan_sensor_time
    public int getLastScanSensorTime(){
        return this._last_scan_sensor_time;
    }

    // setting last_scan_sensor_time
    public void setLastScanSensorTime(int last_scan_sensor_time){
        this._last_scan_sensor_time = last_scan_sensor_time;
    }

    // getting start_date
    public String getStartDate(){
        return this._start_date;
    }

    // setting start_date
    public void setStartDate(String start_date){
        this._start_date = start_date;
    }

    // getting expire_date
    public String getExpireDate(){
        return this._expire_date;
    }

    // setting expire_date
    public void setExpireDate(String expire_date){
        this._expire_date = expire_date;
    }

}
