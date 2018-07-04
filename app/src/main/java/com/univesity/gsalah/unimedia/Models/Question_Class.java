package com.univesity.gsalah.unimedia.Models;

/**
 * Created by GSALAH on 12/24/2017.
 */

public class Question_Class {
    private String Username;
    private String Question;
    private long Date;

    public Question_Class(String username, String question, long date) {
        Username = username;
        this.Question = question;
        Date = date;
    }

    public String getUsername() {
        return Username;
    }

    public String getQuestion() {
        return Question;
    }

    public long getDate() {
        return Date;
    }
}
