package com.distributed.app;

/**
 * Created by Janaka on 2017-11-09.
 */
public class Query {
    String msg;
    Node sender;
    String uuid;

    public Query(Node sender, String msg, String uuid) {
        this.sender = sender;
        this.msg = msg;
        this.uuid = uuid;
    }
    public String getHash(){
        String response = sender.ip+"_"+sender.port+":";
        for(String s:msg.split("\\s+")){
            response+= s+"_";
        }
        return response.substring(0, response.length() - 1)+":"+uuid;
    }

    public String getShortHash(){
        String response = sender.ip+"_"+sender.port+": ";
        for(String s:msg.split("\\s+")){
            response+= s+"_";
        }
        return response.substring(0, response.length() - 1)+":"+ uuid.substring(4);
    }
}

