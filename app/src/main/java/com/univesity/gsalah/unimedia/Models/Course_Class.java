package com.univesity.gsalah.unimedia.Models;

/**
 * Created by GSalah on 5/21/2017.
 */

public class Course_Class {
    int session_id;
    int course_id;
    String course_name,course_time,course_days,course_class,course_lec;

    public Course_Class(int session_id,int course_id, String course_name, String course_time, String course_days, String course_class, String course_lec) {
        this.session_id=session_id;
        this.course_id = course_id;
        this.course_name = course_name;
        this.course_time = course_time;
        this.course_days = course_days;
        this.course_class = course_class;
        this.course_lec = course_lec;
    }

    public int getCourse_id() {
        return course_id;
    }

    public int getSession_id() {
        return session_id;
    }

    public String getCourse_name() {
        return course_name;
    }

    public String getCourse_time() {
        return course_time;
    }

    public String getCourse_days() {
        return course_days;
    }

    public String getCourse_class() {
        return course_class;
    }

    public String getCourse_lec() {
        return course_lec;
    }
}
