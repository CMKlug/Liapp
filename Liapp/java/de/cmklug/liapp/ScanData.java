package de.cmklug.liapp;


public class ScanData {

    //private variables
    int _id;
    String _type;
    String _date;
    int _sensor_time;
    int _glucose_level;
    String _firsthb;
    String _thirdb;
    String _fourthb;
    String _fifthb;
    String _error;

    // Empty constructor
    public ScanData(){

    }
    // constructor
    public ScanData(int id, String type, String date, int sensor_time, int glucose_level,String firsthb,String thirdb,String fourthb,String fifthb,String error){
        this._id = id;
        this._type = type;
        this._date = date;
        this._sensor_time = sensor_time;
        this._glucose_level = glucose_level;
        this._firsthb = firsthb;
        this._thirdb = thirdb;
        this._fourthb = fourthb;
        this._fifthb = fifthb;
        this._error = error;
    }

    // constructor
    public ScanData(String type, String date, int sensor_time, int glucose_level,String firsthb,String thirdb,String fourthb,String fifthb,String error){
        this._type = type;
        this._date = date;
        this._sensor_time = sensor_time;
        this._glucose_level = glucose_level;
        this._firsthb = firsthb;
        this._thirdb = thirdb;
        this._fourthb = fourthb;
        this._fifthb = fifthb;
        this._error = error;
    }

    // getting ID
    public int getID(){
        return this._id;
    }

    // setting id
    public void setID(int id){
        this._id = id;
    }

    // getting type
    public String getType(){
        return this._type;
    }

    // setting type
    public void setType(String type){
        this._type = type;
    }

    // getting date
    public String getDate(){
        return this._date;
    }

    // setting date
    public void setDate(String date){
        this._date = date;
    }

    // getting glucose_level
    public int getGlucoseLevel(){
        return this._glucose_level;
    }

    // setting glucose_level
    public void setGlucoseLevel(int glucoseLevel){
        this._glucose_level = glucoseLevel;
    }

    // getting sensor time
    public int getSensorTime(){
        return this._sensor_time;
    }

    // setting sensor time
    public void setSensorTime(int sensorTime){
        this._sensor_time = sensorTime;
    }

    // getting firsthb
    public String getFirsthb(){
        return this._firsthb;
    }

    // setting firsthb
    public void setFirsthb(String firsthb){
        this._firsthb = firsthb;
    }

    // getting thirdb
    public String getThirdb(){
        return this._thirdb;
    }

    // setting thirdb
    public void setThirdb(String thirdb){
        this._thirdb = thirdb;
    }

    // getting fourthb
    public String getFourthb(){
        return this._fourthb;
    }

    // setting fourthb
    public void setFourthb(String fourthb){
        this._fourthb = fourthb;
    }

    // getting fifthb
    public String getFifthb(){
        return this._fifthb;
    }

    // setting fifthb
    public void setFifthb(String fifthb){
        this._fifthb = fifthb;
    }

    // getting error
    public String getError(){
        return this._error;
    }

    // setting error
    public void setError(String error){
        this._error = error;
    }
}
