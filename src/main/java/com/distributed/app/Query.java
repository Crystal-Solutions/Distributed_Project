package com.distributed.app;

/**
 * Created by Janaka on 2017-11-09.
 */
public class Query {
    String msg;
    Node sender;

    public Query(Node sender, String msg) {
        this.sender = sender;
        this.msg = msg;
    }
    public String getHash(){
        return sender.ip+":"+sender.port+":"+msg;
    }
}

