package com.example.app1;

/**
 * Created by mithileshhinge on 06/01/18.
 */
public class BookmarkedDatabaseRow {

    String _url;
    int _bkmrk;
    int _id;
    boolean _checkedStatus;

    public BookmarkedDatabaseRow(){

    }

    public BookmarkedDatabaseRow(int id, String url, int bkrmrk){
        _id = id;
        _url = url;
        _bkmrk = bkrmrk;

    }

    public BookmarkedDatabaseRow(String url, int bkmrk, boolean checkedStatus){
        _url = url;
        _bkmrk = bkmrk;
        _checkedStatus = checkedStatus;
    }

    public int getID(){
        return _id;
    }

    public void setID(int id){
        _id = id;
    }
    public String getUrl() {
        return _url;
    }

    public void setUrl(String url) {
        this._url = url;
    }

    public boolean getBkmrk() {
        return (_bkmrk == 1);
    }

    public void setBkmrk(Boolean bkmrk) {
        if (bkmrk) _bkmrk = 1;
        else _bkmrk = 0;
    }

    public void setBkmrk(int bkmrk){
        _bkmrk = bkmrk;
    }

    public Boolean getStatus()  { return _checkedStatus; }

    public void setStatus(Boolean checkedStatus) {this._checkedStatus = checkedStatus; }


}
