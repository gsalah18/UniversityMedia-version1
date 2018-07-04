package com.univesity.gsalah.unimedia.Models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Post_Class implements Serializable{
    String id,title,content;
    long date;

    public Post_Class(String id, String title, String content, long date) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public long getDate() {
        return date;
    }

    public Map<String,Object> toMap(){
        Map<String,Object> map=new HashMap();
        map.put("title",this.title);
        map.put("content",this.content);
        map.put("date",this.date);
        return map;
    }
    @Override
    public String toString() {
        return "Post_Class{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", date=" + date +
                '}';
    }
}
