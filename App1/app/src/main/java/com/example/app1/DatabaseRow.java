package com.example.app1;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Sibhali on 7/30/2017.
 */
public class DatabaseRow {

    int _id;
    String _name;
    String _date;
    int _isBookmarked;
    String _thumbpath;

    public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());


    // Empty constructor
    public DatabaseRow(){

    }
    // constructor
    public DatabaseRow(int id, String name, String date, int isBookmarked, String thumbpath ){
        this._id = id;
        this._name = name;
        this._date = date;
        this._isBookmarked = isBookmarked;
        this._thumbpath = thumbpath;
    }

    // constructor
    public DatabaseRow(String name, String date, int isBookmarked, String thumbpath){
        this._name = name;
        this._date= date;
        this._isBookmarked = isBookmarked;
        this._thumbpath = thumbpath;
    }
    // getting ID
    public int getID(){
        return this._id;
    }

    // setting id
    public void setID(int id){
        this._id = id;
    }

    // getting name
    public String getName(){
        return this._name;
    }

    // setting name
    public void setName(String name){
        this._name = name;
    }


    public String getDateTime() {
        return _date;
    }

    public void setDateTime(String dateTime){this._date = dateTime;}

    public boolean isBookmarked(){
        return (_isBookmarked == 1);
    }

    public void setBookmarked(int bookmarked){
        _isBookmarked = bookmarked;
    }

    public void setBookmarked(boolean bookmarked){
        if (bookmarked) _isBookmarked = 1;
        else _isBookmarked = 0;
    }

    public String getThumbpath(){
        return _thumbpath;
    }

    public void setThumbpath(String thumbpath){
        _thumbpath = thumbpath;
    }
}
