package de.cmklug.liapp;


public class GlucoseData {

    //private
    int _id;
    String _date;
    int _glucose_level;
    int _prediction;
    String _comment;

    //public
    public enum Prediction{FALLING, FALLING_SLOW, CONSTANT, RISING_SLOW, RISING };

    // Empty constructor
    public GlucoseData(){

    }
    // constructor
    public GlucoseData(int id, String date, int glucose_level, int prediction, String comment){
        this._id = id;
        this._date = date;
        this._prediction = prediction;
        this._glucose_level = glucose_level;
        this._comment = comment;
    }

    // constructor
    public GlucoseData( String date, int glucose_level, int prediction, String comment){
        this._date = date;
        this._glucose_level = glucose_level;
        this._prediction = prediction;
        this._comment = comment;
    }

    // getting ID
    public int getID(){
        return this._id;
    }

    // setting id
    public void setID(int id){
        this._id = id;
    }

    // getting date
    public String getDate(){
        return this._date;
    }

    // setting date
    public void setDate(String date){
        this._date = date;
    }

    // getting glucose level
    public int getGlucoseLevel(){
        return this._glucose_level;
    }

    // setting prediction
    public void setPrediction(int prediction){
        this._prediction = prediction;
    }

    // getting prediction
    public int getPrediction(){
        return this._prediction;
    }

    // setting glucose level
    public void setGlucoseLevel(int glucose_level){
        this._glucose_level = glucose_level;
    }

    // getting comment
    public String getComment(){
        return this._comment;
    }

    // setting comment
    public void setComment(String comment){
        this._comment = comment;
    }

}
