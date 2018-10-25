package com.ch_l.downbuttonrecorder;

public class recordItem {
    String item_name;
    String item_date;
    String item_size;
    String item_time;
    

    public recordItem(String item_name, String item_date, String item_size, String item_time) {
        this.item_name = item_name;
        this.item_date = item_date;
        this.item_size = item_size;
        this.item_time = item_time;
    }

    public String getItem_name() {
        return item_name;
    }

    public void setItem_name(String item_name) {
        this.item_name = item_name;
    }

    public void setItem_date(String item_date) {
        this.item_date = item_date;
    }

    public void setItem_size(String item_size) {
        this.item_size = item_size;
    }

    public void setItem_time(String item_time) {
        this.item_time = item_time;
    }

    public String getItem_time() {

        return item_time;
    }

    public String getItem_date() {
        return item_date;
    }


    public String getItem_size() {
        return item_size;
    }

}
