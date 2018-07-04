package com.univesity.gsalah.unimedia.Models;


public class Std_Class {
    String std_id;
    String std_name;
    String TA;

    public Std_Class(String std_id, String std_name,String TA) {
        this.std_id = std_id;
        this.std_name = std_name;
        this.TA=TA;
    }

    public void setTA(String TA) {
        this.TA = TA;
    }

    public String getStd_id() {
        return std_id;
    }

    public String getStd_name() {
        return std_name;
    }

    public Boolean isTA() {
        if(this.TA.equals("yes"))
            return true;
        else return false;
    }
}
