package com.plough.nativeprint.serversocket;

/**
 * Created by plough on 2019/2/25.
 */
public class ChatObject {

    private String message;

    public ChatObject() {
    }

    public ChatObject(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
