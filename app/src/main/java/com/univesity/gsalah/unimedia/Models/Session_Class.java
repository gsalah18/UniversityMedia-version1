package com.univesity.gsalah.unimedia.Models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Session_Class implements Serializable{
    private String studentId;
    private String studentName;
    private String sessionTitle;
    private String sessionId;
    private String token;

    public Session_Class(String studentId, String studentName, String sessionTitle, String sessionId, String token) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.sessionTitle = sessionTitle;
        this.sessionId = sessionId;
        this.token = token;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getSessionTitle() {
        return sessionTitle;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getToken() {
        return token;
    }

    @Override
    public String toString() {
        return sessionTitle;
    }
    public Map<String,Object> toMap(){
        Map<String,Object>map=new HashMap<>();
        map.put("studentId",studentId);
        map.put("studentName",studentName);
        map.put("sessionTitle",sessionTitle);
        map.put("sessionId",sessionId);
        map.put("token",token);
        return map;
    }
}
