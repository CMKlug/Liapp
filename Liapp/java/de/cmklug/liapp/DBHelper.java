package de.cmklug.liapp;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {
    private static DBHelper sInstance;
    SimpleDateFormat sqlDateFormat = new SimpleDateFormat("dd.MM.yyyy;HH:mm") ;

    //region ########################## static Strings ##########################
    public static final String TABLE_HISTORY = "history";
    public static final String TABLE_LASTSCAN = "lastscan";
    public static final String TABLE_SENSOR = "sensor";
    public static final String TABLE_SCANS = "scan";

    public static final String KEY_HISTORY_ID = "_id";
    public static final String KEY_HISTORY_DATE  = "date";
    public static final String KEY_HISTORY_GLUCOSELEVEL  = "glucoselevel";
    public static final String KEY_HISTORY_COMMENT  = "comment";

    public static final String KEY_LASTSCAN_ID = "_id";
    public static final String KEY_LASTSCAN_TYPE = "type";  //h:history; t:trent; p:prediction
    public static final String KEY_LASTSCAN_DATE  = "date";
    public static final String KEY_LASTSCAN_SENSORTIME    = "sensortime";
    public static final String KEY_LASTSCAN_GLUCOSELEVEL  = "glucoselevel";
    public static final String KEY_LASTSCAN_FIRSTHB  = "firsthb";
    public static final String KEY_LASTSCAN_THIRDB  = "thirdb";
    public static final String KEY_LASTSCAN_FOURTHB  = "fourthb";
    public static final String KEY_LASTSCAN_FIFTHB  = "fifthb";
    public static final String KEY_LASTSCAN_ERROR  = "error";

    public static final String KEY_SENSOR_ID = "_id";
    public static final String KEY_SENSOR_SENSORID  = "sensorid";
    public static final String KEY_SENSOR_VERSION  = "version";
    public static final String KEY_SENSOR_TIMELEFT  = "timeleft";
    public static final String KEY_SENSOR_LSST = "lastscansensortime";
    public static final String KEY_SENSOR_STARTDATE = "startdate";
    public static final String KEY_SENSOR_EXPIREDATE = "expiredate";

    public static final String KEY_SCAN_ID  = "_id";
    public static final String KEY_SCAN_DATE  = "date";
    public static final String KEY_SCAN_GLUCOSELEVEL = "glucoselevel";
    public static final String KEY_SCAN_PREDICTION = "prediction";
    public static final String KEY_SCAN_COMMENT = "comment";

    private static final String DATABASE_NAME = "liapp.db";
    private static final int DATABASE_VERSION = 1;
    //endregion

    public static synchronized DBHelper getInstance(Context context) {

        if (sInstance == null) {
            sInstance = new DBHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    private DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {

        String CREATE_HISTORY_TABLE = "CREATE TABLE " + TABLE_HISTORY +
                "(" +
                KEY_HISTORY_ID + " INTEGER PRIMARY KEY," + // Define a primary key
                KEY_HISTORY_DATE + " TEXT," +
                KEY_HISTORY_GLUCOSELEVEL + " INTEGER," +
                KEY_HISTORY_COMMENT + " INTEGER" +
                ")";

        String CREATE_LASTSCAN_TABLE = "CREATE TABLE " + TABLE_LASTSCAN +
                "(" +
                KEY_LASTSCAN_ID + " INTEGER PRIMARY KEY," + // Define a primary key
                KEY_LASTSCAN_TYPE + " TEXT," +
                KEY_LASTSCAN_DATE + " TEXT," +
                KEY_LASTSCAN_SENSORTIME + " INTEGER," +
                KEY_LASTSCAN_GLUCOSELEVEL + " INTEGER," +
                KEY_LASTSCAN_FIRSTHB + " TEXT," +
                KEY_LASTSCAN_THIRDB + " TEXT," +
                KEY_LASTSCAN_FOURTHB + " TEXT," +
                KEY_LASTSCAN_FIFTHB + " TEXT," +
                KEY_LASTSCAN_ERROR + " TEXT" +
                ")";

        String CREATE_SENSOR_TABLE = "CREATE TABLE " + TABLE_SENSOR +
                "(" +
                KEY_SENSOR_ID + " INTEGER PRIMARY KEY," + // Define a primary key
                KEY_SENSOR_SENSORID + " INTEGER," +
                KEY_SENSOR_VERSION + " INTEGER," +
                KEY_SENSOR_TIMELEFT + " INTEGER," +
                KEY_SENSOR_LSST + " INTEGER," +
                KEY_SENSOR_STARTDATE + " TEXT," +
                KEY_SENSOR_EXPIREDATE + " TEXT" +
                ")";

        String CREATE_SCANS_TABLE = "CREATE TABLE " + TABLE_SCANS +
                "(" +
                KEY_SCAN_ID + " INTEGER PRIMARY KEY," + // Define a primary key
                KEY_SCAN_DATE + " TEXT," +
                KEY_SCAN_GLUCOSELEVEL + " INTEGER," +
                KEY_SCAN_PREDICTION + " INTEGER," +
                KEY_SCAN_COMMENT + " INTEGER" +
                ")";

        db.execSQL(CREATE_HISTORY_TABLE);
        db.execSQL(CREATE_LASTSCAN_TABLE);
        db.execSQL(CREATE_SENSOR_TABLE);
        db.execSQL(CREATE_SCANS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LASTSCAN);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SENSOR);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCANS);

        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    //region ########################## add data ##########################
    public void addorUpdateHistoryGlucoseData(GlucoseData glucosedata) {
  
        SQLiteDatabase db = getWritableDatabase();


        db.beginTransaction();

        try {

            ContentValues values = new ContentValues();
            values.put(KEY_HISTORY_DATE, glucosedata.getDate());
            values.put(KEY_HISTORY_GLUCOSELEVEL, glucosedata.getGlucoseLevel());
            values.put(KEY_HISTORY_COMMENT, glucosedata.getComment());

            int rows = db.update(TABLE_HISTORY, values, KEY_HISTORY_DATE + "= ?", new String[]{String.valueOf(glucosedata.getDate())});

            if (rows <= 0) {
                db.insertOrThrow(TABLE_HISTORY, null, values);
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
        } finally {
            db.endTransaction();
        }
    }

    public void addLastScanGlucoseData(ScanData lastscandata) {
        SQLiteDatabase db = getWritableDatabase();


        db.beginTransaction();

        try {
            ContentValues values = new ContentValues();
            values.put(KEY_LASTSCAN_TYPE, lastscandata.getType());
            values.put(KEY_LASTSCAN_DATE, lastscandata.getDate());
            values.put(KEY_LASTSCAN_SENSORTIME, lastscandata.getSensorTime());
            values.put(KEY_LASTSCAN_GLUCOSELEVEL, lastscandata.getGlucoseLevel());
            values.put(KEY_LASTSCAN_FIRSTHB, lastscandata.getFirsthb());
            values.put(KEY_LASTSCAN_THIRDB, lastscandata.getThirdb());
            values.put(KEY_LASTSCAN_FOURTHB, lastscandata.getFourthb());
            values.put(KEY_LASTSCAN_FIFTHB, lastscandata.getFifthb());
            values.put(KEY_LASTSCAN_ERROR, lastscandata.getError());

            db.insertOrThrow(TABLE_LASTSCAN, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
		
        } finally {
            db.endTransaction();
        }
    }

    public void addorUpdateSensorData(SensorData sensordata) {
        SQLiteDatabase db = getWritableDatabase();


        db.beginTransaction();

        try {
            ContentValues values = new ContentValues();
            values.put(KEY_SENSOR_SENSORID, sensordata.getSensorID());
            values.put(KEY_SENSOR_VERSION, sensordata.getVersion());
            values.put(KEY_SENSOR_TIMELEFT, sensordata.getTimeLeft());
            values.put(KEY_SENSOR_LSST, sensordata.getLastScanSensorTime());
            values.put(KEY_SENSOR_STARTDATE, sensordata.getStartDate());
            values.put(KEY_SENSOR_EXPIREDATE, sensordata.getExpireDate());

            int rows = db.update(TABLE_SENSOR, values, KEY_SENSOR_SENSORID + "= ?", new String[]{sensordata.getSensorID()});

            if (rows <= 0) {
                db.insertOrThrow(TABLE_SENSOR, null, values);
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {

        } finally {
            db.endTransaction();
        }
    }

    public void addScanData(GlucoseData glucosedata) {
        SQLiteDatabase db = getWritableDatabase();


        db.beginTransaction();

        try {
            ContentValues values = new ContentValues();
            values.put(KEY_SCAN_DATE, glucosedata.getDate());
            values.put(KEY_SCAN_GLUCOSELEVEL, glucosedata.getGlucoseLevel());
            values.put(KEY_SCAN_PREDICTION, glucosedata.getPrediction());
            values.put(KEY_SCAN_COMMENT, glucosedata.getComment());

            db.insertOrThrow(TABLE_SCANS, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {

        } finally {
            db.endTransaction();
        }
    }
    //endregion

    //region ########################## delete Data ##########################
    public void deleteAllData() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_HISTORY, null, null);
            db.delete(TABLE_LASTSCAN, null, null);
            db.delete(TABLE_SENSOR, null, null);
            db.delete(TABLE_SCANS, null, null);

            db.setTransactionSuccessful();
        } catch (Exception e) {

        } finally {
            db.endTransaction();
        }
    }

    public void deleteLastScanData() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_LASTSCAN, null, null);

            db.setTransactionSuccessful();
        } catch (Exception e) {

        } finally {
            db.endTransaction();
        }
    }
    //endregion

    //region ########################## get Data ##########################
    // Get complete History
    public List<GlucoseData> getCompleteHistory() {
        List<GlucoseData> history = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + TABLE_HISTORY;

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        cursor.moveToFirst();

        try {
            if (cursor.moveToFirst()) {
                do {
                    GlucoseData glucosedata = new GlucoseData();
                    glucosedata.setID(cursor.getInt(cursor.getColumnIndex(KEY_HISTORY_ID)));
                    glucosedata.setDate(cursor.getString(cursor.getColumnIndex(KEY_HISTORY_DATE)));
                    glucosedata.setGlucoseLevel(cursor.getInt(cursor.getColumnIndex(KEY_HISTORY_GLUCOSELEVEL)));
                    glucosedata.setComment(cursor.getString(cursor.getColumnIndex(KEY_HISTORY_COMMENT)));

                    // Adding to list
                    history.add(glucosedata);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {

        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return history;
    }

    // Get History of day
    public List<GlucoseData> getHistoryOfDay(Date day) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
        List<GlucoseData> history = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + TABLE_HISTORY;

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        cursor.moveToFirst();

        try {
            if (cursor.moveToFirst()) {
                do {
                    GlucoseData glucosedata = new GlucoseData();
                    glucosedata.setID(cursor.getInt(cursor.getColumnIndex(KEY_HISTORY_ID)));
                    glucosedata.setDate(cursor.getString(cursor.getColumnIndex(KEY_HISTORY_DATE)));
                    glucosedata.setGlucoseLevel(cursor.getInt(cursor.getColumnIndex(KEY_HISTORY_GLUCOSELEVEL)));
                    glucosedata.setComment(cursor.getString(cursor.getColumnIndex(KEY_HISTORY_COMMENT)));


                    Date datadate = sqlDateFormat.parse(glucosedata.getDate());
                    // Adding to list
                    if(fmt.format(day).equals(fmt.format(datadate))) {
                        history.add(glucosedata);
                    }
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {

        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return history;
    }

    // Get History till date
    public List<GlucoseData> getHistoryTillDate (Date date) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
        List<GlucoseData> history = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + TABLE_HISTORY;

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        cursor.moveToFirst();

        try {
            if (cursor.moveToFirst()) {
                do {
                    GlucoseData glucosedata = new GlucoseData();
                    glucosedata.setID(cursor.getInt(cursor.getColumnIndex(KEY_HISTORY_ID)));
                    glucosedata.setDate(cursor.getString(cursor.getColumnIndex(KEY_HISTORY_DATE)));
                    glucosedata.setGlucoseLevel(cursor.getInt(cursor.getColumnIndex(KEY_HISTORY_GLUCOSELEVEL)));
                    glucosedata.setComment(cursor.getString(cursor.getColumnIndex(KEY_HISTORY_COMMENT)));


                    Date datadate = sqlDateFormat.parse(glucosedata.getDate());
                    // Adding to list
                    if(datadate.after(date)) {
                        history.add(glucosedata);
                    }
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            // Log.d(TAG, "Error while trying to get posts from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return history;
    }

    // Get last scan
    public List<ScanData> getLastScan() {
        List<ScanData> lastscan = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + TABLE_LASTSCAN;

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        cursor.moveToFirst();

        try {
            if (cursor.moveToFirst()) {
                do {
                    ScanData Lastscandata = new ScanData();
                    Lastscandata.setID(cursor.getInt(cursor.getColumnIndex(KEY_LASTSCAN_ID)));
                    Lastscandata.setType(cursor.getString(cursor.getColumnIndex(KEY_LASTSCAN_TYPE)));
                    Lastscandata.setDate(cursor.getString(cursor.getColumnIndex(KEY_LASTSCAN_DATE)));
                    Lastscandata.setSensorTime(cursor.getInt(cursor.getColumnIndex(KEY_LASTSCAN_SENSORTIME)));
                    Lastscandata.setGlucoseLevel(cursor.getInt(cursor.getColumnIndex(KEY_LASTSCAN_GLUCOSELEVEL)));
                    Lastscandata.setFirsthb(cursor.getString(cursor.getColumnIndex(KEY_LASTSCAN_FIRSTHB)));
                    Lastscandata.setThirdb(cursor.getString(cursor.getColumnIndex(KEY_LASTSCAN_THIRDB)));
                    Lastscandata.setFourthb(cursor.getString(cursor.getColumnIndex(KEY_LASTSCAN_FOURTHB)));
                    Lastscandata.setFifthb(cursor.getString(cursor.getColumnIndex(KEY_LASTSCAN_FIFTHB)));
                    Lastscandata.setError(cursor.getString(cursor.getColumnIndex(KEY_LASTSCAN_ERROR)));

                    // Adding to list
                    lastscan.add(Lastscandata);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            // Log.d(TAG, "Error while trying to get posts from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return lastscan;
    }

    // Get complete Scans
    public List<GlucoseData> getCompleteScans() {
        List<GlucoseData> scans = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + TABLE_SCANS;

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        cursor.moveToFirst();

        try {
            if (cursor.moveToFirst()) {
                do {
                    GlucoseData scandata = new GlucoseData();
                    scandata.setID(cursor.getInt(cursor.getColumnIndex(KEY_SCAN_ID)));
                    scandata.setDate(cursor.getString(cursor.getColumnIndex(KEY_SCAN_DATE)));
                    scandata.setGlucoseLevel(cursor.getInt(cursor.getColumnIndex(KEY_SCAN_GLUCOSELEVEL)));
                    scandata.setPrediction(cursor.getInt(cursor.getColumnIndex(KEY_SCAN_PREDICTION)));
                    scandata.setComment(cursor.getString(cursor.getColumnIndex(KEY_SCAN_COMMENT)));

                    // Adding to list
                    scans.add(scandata);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            // Log.d(TAG, "Error while trying to get posts from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return scans;
    }

    // Getting single sensor
    public SensorData getSensorData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_SENSOR, new String[] { KEY_SENSOR_ID,
                        KEY_SENSOR_SENSORID, KEY_SENSOR_VERSION, KEY_SENSOR_TIMELEFT,
                        KEY_SENSOR_LSST, KEY_SENSOR_STARTDATE, KEY_SENSOR_EXPIREDATE  }, KEY_SENSOR_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        SensorData sensor = new SensorData();
        sensor.setID(cursor.getInt(cursor.getColumnIndex(KEY_SENSOR_ID)));
        sensor.setSensorID(cursor.getString(cursor.getColumnIndex(KEY_SENSOR_SENSORID)));
        sensor.setVersion(cursor.getString(cursor.getColumnIndex(KEY_SENSOR_VERSION)));
        sensor.setTimeLeft(cursor.getInt(cursor.getColumnIndex(KEY_SENSOR_TIMELEFT)));
        sensor.setLastScanSensorTime(cursor.getInt(cursor.getColumnIndex(KEY_SENSOR_LSST)));
        sensor.setStartDate(cursor.getString(cursor.getColumnIndex(KEY_SENSOR_STARTDATE)));
        sensor.setExpireDate(cursor.getString(cursor.getColumnIndex(KEY_SENSOR_EXPIREDATE)));

        // return
        return sensor;
    }

    // Getting single scan
    public GlucoseData getScanData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_SCANS, new String[] { KEY_SCAN_ID,
                        KEY_SCAN_DATE, KEY_SCAN_GLUCOSELEVEL, KEY_SCAN_PREDICTION,
                        KEY_SCAN_COMMENT}, KEY_SCAN_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        GlucoseData scandata = new GlucoseData();
        scandata.setID(cursor.getInt(cursor.getColumnIndex(KEY_SCAN_ID)));
        scandata.setDate(cursor.getString(cursor.getColumnIndex(KEY_SCAN_DATE)));
        scandata.setGlucoseLevel(cursor.getInt(cursor.getColumnIndex(KEY_SCAN_GLUCOSELEVEL)));
        scandata.setPrediction(cursor.getInt(cursor.getColumnIndex(KEY_SCAN_PREDICTION)));
        scandata.setComment(cursor.getString(cursor.getColumnIndex(KEY_SCAN_COMMENT)));

        // return
        return scandata;
    }

    // Get complete Scans
    public List<SensorData> getCompleteSensors() {
        List<SensorData> sensors = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + TABLE_SENSOR;

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        cursor.moveToFirst();

        try {
            if (cursor.moveToFirst()) {
                do {
                    SensorData sensordata = new SensorData();
                    sensordata.setID(cursor.getInt(cursor.getColumnIndex(KEY_SENSOR_ID)));
                    sensordata.setSensorID(cursor.getString(cursor.getColumnIndex(KEY_SENSOR_SENSORID)));
                    sensordata.setVersion(cursor.getString(cursor.getColumnIndex(KEY_SENSOR_VERSION)));
                    sensordata.setTimeLeft(cursor.getInt(cursor.getColumnIndex(KEY_SENSOR_TIMELEFT)));
                    sensordata.setLastScanSensorTime(cursor.getInt(cursor.getColumnIndex(KEY_SENSOR_LSST)));
                    sensordata.setStartDate(cursor.getString(cursor.getColumnIndex(KEY_SENSOR_STARTDATE)));
                    sensordata.setExpireDate(cursor.getString(cursor.getColumnIndex(KEY_SENSOR_EXPIREDATE)));

                    // Adding to list
                    sensors.add(sensordata);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            // Log.d(TAG, "Error while trying to get posts from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return sensors;
    }

    // Getting single trent
    public ScanData getLastScanData(String type) {  //h:history; t:trent; p:prediction
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_LASTSCAN, new String[] { KEY_LASTSCAN_ID,
                        KEY_LASTSCAN_TYPE, KEY_LASTSCAN_DATE, KEY_LASTSCAN_SENSORTIME,
                        KEY_LASTSCAN_GLUCOSELEVEL, KEY_LASTSCAN_FIRSTHB, KEY_LASTSCAN_THIRDB,
                        KEY_LASTSCAN_FOURTHB, KEY_LASTSCAN_FIFTHB, KEY_LASTSCAN_ERROR
                }, KEY_LASTSCAN_TYPE + "=?",
                new String[] { String.valueOf(type) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        ScanData Lastscandata = new ScanData();
        Lastscandata.setID(cursor.getInt(cursor.getColumnIndex(KEY_LASTSCAN_ID)));
        Lastscandata.setType(cursor.getString(cursor.getColumnIndex(KEY_LASTSCAN_TYPE)));
        Lastscandata.setDate(cursor.getString(cursor.getColumnIndex(KEY_LASTSCAN_DATE)));
        Lastscandata.setSensorTime(cursor.getInt(cursor.getColumnIndex(KEY_LASTSCAN_SENSORTIME)));
        Lastscandata.setGlucoseLevel(cursor.getInt(cursor.getColumnIndex(KEY_LASTSCAN_GLUCOSELEVEL)));
        Lastscandata.setFirsthb(cursor.getString(cursor.getColumnIndex(KEY_LASTSCAN_FIRSTHB)));
        Lastscandata.setThirdb(cursor.getString(cursor.getColumnIndex(KEY_LASTSCAN_THIRDB)));
        Lastscandata.setFourthb(cursor.getString(cursor.getColumnIndex(KEY_LASTSCAN_FOURTHB)));
        Lastscandata.setFifthb(cursor.getString(cursor.getColumnIndex(KEY_LASTSCAN_FIFTHB)));
        Lastscandata.setError(cursor.getString(cursor.getColumnIndex(KEY_LASTSCAN_ERROR)));

        // return
        return Lastscandata;
    }

    // Getting single history
    public GlucoseData getHistoryData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_HISTORY, new String[] { KEY_HISTORY_ID,
                        KEY_HISTORY_DATE, KEY_HISTORY_GLUCOSELEVEL,
                        KEY_HISTORY_COMMENT }, KEY_HISTORY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        GlucoseData historydata = new GlucoseData();
        historydata.setID(cursor.getInt(cursor.getColumnIndex(KEY_HISTORY_ID)));
        historydata.setDate(cursor.getString(cursor.getColumnIndex(KEY_HISTORY_DATE)));
        historydata.setGlucoseLevel(cursor.getInt(cursor.getColumnIndex(KEY_HISTORY_GLUCOSELEVEL)));
        historydata.setComment(cursor.getString(cursor.getColumnIndex(KEY_HISTORY_COMMENT)));

        // return
        return historydata;
    }
    //endregion

    //region ########################## get Table Count ##########################
    // Getting History Count
    public int getHistoryCount() {
        String countQuery = "SELECT  * FROM " + TABLE_HISTORY;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }

    // Getting Sensor Count
    public int getSensorCount() {
        String countQuery = "SELECT  * FROM " + TABLE_SENSOR;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }

    // Getting Trent Count
    public int getTrentCount() {
        String countQuery = "SELECT  * FROM " + TABLE_LASTSCAN;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }

    // Getting Scan Count
    public int getScanCount() {
        String countQuery = "SELECT  * FROM " + TABLE_SCANS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }
    //endregion

    /*/ update history entry
    public int updateHistoryEntry(GlucoseData glucosedata) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_USER_PROFILE_PICTURE_URL, user.profilePictureUrl);

        // Updating profile picture url for user with that userName
        return db.update(TABLE_USERS, values, KEY_USER_NAME + " = ?",
                new String[]{String.valueOf(user.userName) });
    }*/
}
