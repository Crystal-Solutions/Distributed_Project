package com.distributed.app;

import java.io.IOException;
import java.net.*;

/**
 * Created by Janaka on 2017-10-23.
 */
public class UdpClient extends Client {

    private DatagramSocket rec_socket = null;

    private UdpClient(String[] args) {
        String bs_ip = args[0];
        int bs_port = Integer.valueOf(args[1]);
        this.bs = new Node(bs_ip, bs_port);
        this.ip = args[2];
        this.port_receive = Integer.valueOf(args[3]);
        this.port_send = Integer.valueOf(args[4]);
        this.username = args[5];
    }


    @Override
    protected void startListening() throws SocketException {
        // Listen to the new joining nodes

        echo("Send Port", ip + ":" + port_send);
        rec_socket = new DatagramSocket(port_receive);
        rec_socket.setSoTimeout(1000);
        echo("com.distributed.app.UdpClient listening at", ip + ":" + port_receive);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (running) {
                    byte[] buffer = new byte[65536];
                    DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
                    try {
                        rec_socket.receive(incoming);
                        byte[] data = incoming.getData();
                        String s = new String(data, 0, incoming.getLength());
                        String reply = processMessage(s);
                        if (reply != null)
                            send(reply, new Node(incoming.getAddress().toString().substring(1), incoming.getPort()));

                    } catch (SocketTimeoutException e) {
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                rec_socket.close();
            }
        });
        t.start();
    }

    // Send and receive on the same port
    @Override
    protected String sendAndReceive(String msg, Node node) throws IOException {
        return sendAndReceiveUdp( msg, node);
    }

    // only send do not wait for a response
    protected void send(String msg, Node node) throws IOException {
        sendUdp(msg,node);
    }


    //Static Methods -------------------------------
    public static Client fromArgs(String[] args) {
        return new UdpClient(args);
    }


}
