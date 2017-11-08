package com.distributed.app;

import java.io.IOException;
import java.net.*;

/**
 * Created by Janaka on 2017-10-23.
 */
public class UdpClient extends Client {

    private int port_send;

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
    // Send and receive on the same port
    protected String sendAndReceive(String msg, Node node) throws IOException {
        msg = addLengthToMsg(msg);
        echo("Send and relieve(to:" + node.ip + ":" + node.port + ")", msg);
        DatagramSocket sock = new DatagramSocket(port_send);
        InetAddress node_address = InetAddress.getByName(node.ip);
        byte[] buffer = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, node_address, node.port);
        sock.send(packet);

        buffer = new byte[65536];
        String s;
        DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
        sock.receive(incoming);
        byte[] data = incoming.getData();
        s = new String(data, 0, incoming.getLength());

        echo("Receive(to:" + incoming.getAddress() + ":" + incoming.getPort() + ")", s);
        sock.close();
        return s;
    }

    @Override
    // only send do not wait for a response
    protected void send(String msg, Node node) throws IOException {

        synchronized (this) {
            msg = addLengthToMsg(msg);
            echo("Send(to: " + ip + ":" + node.port + ")", msg);
            DatagramSocket sock = new DatagramSocket(port_send);
            // node address - node to recieve the msg
            InetAddress node_address = InetAddress.getByName(node.ip);
            byte[] buffer = msg.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, node_address, node.port);
            sock.send(packet);
            sock.close();
        }

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
                        String reply = processMessage(s, incoming);
                        if (reply != null)
                            send(reply, new Node(incoming.getAddress().toString().substring(1), incoming.getPort()));

                    } catch (SocketTimeoutException e) {
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                rec_socket.close();
            }
        });
        t.start();
    }


    @Override
    protected void printClientInfo(){
        echo("BS", bs.ip + ":" + bs.port);
        echo("My IP", ip + ":" + port_receive);
        echo("Send Port",ip + ":" + port_send);
    }


    //Static Methods -------------------------------
    public static Client fromArgs(String[] args) {
        return new UdpClient(args);
    }


}
