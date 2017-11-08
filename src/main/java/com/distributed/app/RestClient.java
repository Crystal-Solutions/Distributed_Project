package com.distributed.app;

import java.io.IOException;
import java.net.SocketException;

/**
 * Created by Janaka on 2017-11-08.
 */
public class RestClient extends Client {
    @Override
    protected void startListening() throws SocketException {

    }

    @Override
    protected String sendAndReceive(String msg, Node node) throws IOException {
        return null;
    }

    @Override
    protected void send(String msg, Node node) throws IOException {

    }

    @Override
    protected void printClientInfo() {

    }
}
