package com.example.app1;

/**
 * Created by mithileshhinge on 12/07/17.
 */
public class ImageModel {

    String name, url;
    boolean status;

    public ImageModel(){

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean getBkmrk() { return status ;}

    public void setBkmrk(Boolean status) { this.status = status;}


}
