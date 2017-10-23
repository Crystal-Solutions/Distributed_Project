import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by Janaka on 2017-10-23.
 */
public class Client {

    private Node bs;
    private String ip;
    private int port_receive;
    private int port_send;
    private String username;
    private boolean running;
    private Node[] knownNodes;

    private Client(String[] args){
         String bs_ip = args[0];
         int bs_port = Integer.valueOf(args[1]);
         this.bs = new Node(bs_ip,bs_port);
         ip = args[2];
         port_receive = Integer.valueOf(args[3]);
         port_send = Integer.valueOf(args[4]);
         username = args[5];
    }

    public void start(){
        try {

            echo("BS=>" + bs.ip + ":" + bs.port);
            echo("My IP=>" + ip + ":" + port_receive);
            echo("Send Port=>" + ip + ":" + port_send);

            // Register
            String join_msg = "REG " + ip + " " + port_receive + " " + username;
            String reply = send(join_msg, bs);
            knownNodes = parseRegMessage(reply);

            if(knownNodes!=null) {
                for (Node node : knownNodes) {
                    echo(node.ip + ":" + node.port);
                }
            }

            String leave_msg = "UNREG " + ip + " " + port_receive + " " + username;
            send(leave_msg, bs);


        } catch (IOException e) {
            System.err.println("IOException " + e);
        }
    }

    private String send(String msg, Node node) throws IOException {
        msg = addLengthToMsg(msg);
        echo("Send(" + ip + ":" + node.port + ")>>" + msg);
        DatagramSocket sock = new DatagramSocket(port_send);
        InetAddress bs_address = InetAddress.getByName(node.ip);
        byte[] buffer = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, bs_address, node.port);
        sock.send(packet);

        buffer = new byte[65536];
        String s;
        DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
        sock.receive(incoming);
        byte[] data = incoming.getData();
        s = new String(data, 0, incoming.getLength());

        echo("Receive(" + incoming.getAddress() + ":" + incoming.getPort() + ")>>" + s);
        sock.close();
        return s;
    }

    private static class Node{
        String ip;
        int port;

        public Node(String ip, int port){
            this.ip = ip;
            this.port = port;
        }
    }


    //Static Methods -------------------------------
    public static Client fromArgs(String[] args){
        return new Client(args);
    }


    //simple function to echo data to terminal
    public static void echo(String msg) {
        System.out.println(msg);
    }

    // Add length parameter in front of the message
    private static String addLengthToMsg(String msg) {
        if (msg.length() > 9000)
            return "0004";
        int len = msg.length() + 5;
        msg = len + " " + msg;
        while (msg.length() < len) {
            msg = "0" + msg;
        }
        return msg;
    }

    private static Node[] parseRegMessage(String msg){
        String[] parts = msg.split(" ");
        Node[] nodes = null;
        if(parts.length>3){
            nodes = new Node[(parts.length-3)/2];
            for(int i=3; i<parts.length; i+=2){
                nodes[(i-3)/2] = new Node(parts[i], Integer.valueOf(parts[i+1]));
            }
        }

        return nodes;
    }

}
