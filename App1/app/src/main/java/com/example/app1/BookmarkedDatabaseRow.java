package com.example.app1;

/**
 * Created by mithileshhinge on 06/01/18.
 */
public class BookmarkedDatabaseRow {

    String _url, _hashID;
    int _bkmrk;
    int _id;
    boolean _checkedStatus;

    public BookmarkedDatabaseRow(){

    }

    public BookmarkedDatabaseRow(int id, String url, int bkrmrk, String hashID){
        _id = id;
        _url = url;
        _bkmrk = bkrmrk;
        _hashID = hashID;

    }

    public BookmarkedDatabaseRow(String url, int bkmrk, boolean checkedStatus, String hashID){
        _url = url;
        _bkmrk = bkmrk;
        _checkedStatus = checkedStatus;
        _hashID = hashID;
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

    public String getHashID(){
        return _hashID;
    }

    public void setHashID(String hashID){
        _hashID = hashID;
    }


}
