import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import java.io.*;
import java.net.SocketTimeoutException;
import java.util.*;

/**
 * Created by Janaka on 2017-10-23.
 */
public class Client {

    private Node bs;    // bootstrap server
    private String ip;  // this node's IP
    private int port_receive;
    private int port_send;
    private String username;
    private volatile boolean running;
    private ArrayList<Node> knownNodes;
    private String[] files; // files in the node
    private String[] queries;

    private DatagramSocket rec_socket = null;

    private Client(String[] args){
        String bs_ip = args[0];
        int bs_port = Integer.valueOf(args[1]);
        this.bs = new Node(bs_ip, bs_port);
        this.ip = args[2];
        this.port_receive = Integer.valueOf(args[3]);
        this.port_send = Integer.valueOf(args[4]);
        this.username = args[5];

    }

    public void start(){
        try {
            search("of Tintin Jack");

            running = true;
            echo("BS=>" + bs.ip + ":" + bs.port);
            echo("My IP=>" + ip + ":" + port_receive);
            echo("Send Port=>" + ip + ":" + port_send);

            // Register with BS
            String reg_msg = "REG " + ip + " " + port_receive + " " + username;
            String reg_reply = sendAndRecieve(reg_msg, bs);
            knownNodes = parseRegMessage(reg_reply);

            if(knownNodes!=null) {
                for (Node node : knownNodes) {
                    echo(node.ip + ":" + node.port);
                }
            }

            // Listen to the new joining nodes
            rec_socket = new DatagramSocket(port_receive);
            rec_socket.setSoTimeout(1000);
            echo("Client listening at " + ip + ":" + port_receive);

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (running){
                        byte[] buffer = new byte[65536];
                        DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
                        try {
                            boolean isOkay = true;
                            rec_socket.receive(incoming);

                            byte[] data = incoming.getData();
                            String s = new String(data, 0, incoming.getLength());

                            //echo the details of incoming data - client ip : client port - client message
                            echo(incoming.getAddress().getHostAddress() + " : " + incoming.getPort() + " - " + s);

                            StringTokenizer st = new StringTokenizer(s, " ");

                            String length = st.nextToken();
                            String command = st.nextToken();

                            if (command.equals("JOIN")) {
                                String reply = "JOINOK ";
                                Node joinee = null;

                                String ip = st.nextToken();
                                int port = Integer.parseInt(st.nextToken());

                                for (int i = 0; i < knownNodes.size(); i++) {
                                    if (knownNodes.get(i).getIp().equals(ip) && knownNodes.get(i).getPort() == port) {
                                        reply += "9999";
                                        isOkay = false;
                                    }
                                }

                                if (isOkay){
                                    joinee = new Node(ip, port);
                                    knownNodes.add(joinee);
                                    reply += "0";
                                    // incoming.getAddress() returns InetAddress like /127.0.0.1 - therefore convert to a ip string
                                    send(reply, new Node(incoming.getAddress().toString().substring(1), incoming.getPort()));
                                }
                            }

                            //----------------------------------
                            if (command.equals("SER")) {
                                String reply = "SEROK ";
                                Node joinee = null;

                                String ip = st.nextToken();
                                int port = Integer.parseInt(st.nextToken());

                                String query = "";

                                while(st.hasMoreTokens()){
                                    String value = st.nextToken();
                                    Character lastChar = value.charAt(value.length()-1);
                                    if (lastChar.equals('\"')){
                                        value = value.substring(0, value.length() - 1);
                                        query = query + value;
                                    }
                                    else{
                                        query = query + value + " ";
                                    }

                                }

                                String searchQuery = query.substring(1,query.length());
                                echo("File searched: " + searchQuery);

                                List<String> results = search(searchQuery);

                                if (results.isEmpty()){
                                    reply += "0";
                                    isOkay = true;
                                }

                                else{
                                    reply += results.size() + " ";
                                    for (Object fileName: results){
                                        reply += fileName.toString()+ " ";
                                    }
                                    isOkay = true;
                                }

                                if (isOkay){
                                    //joinee = new Node(ip, port);
                                    //knownNodes.add(joinee);
                                    //reply += "0";
                                    // incoming.getAddress() returns InetAddress like /127.0.0.1 - therefore convert to a ip string
                                    send(reply, new Node(incoming.getAddress().toString().substring(1), incoming.getPort()));
                                }
                            }
                            //------------------

                            if (command.equals("LEAVE")) {
                                processLeave(st,incoming);
                            }

                        } catch (SocketTimeoutException e) {

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    rec_socket.close();
                }
            });
            t.start();


            // Join to the distributed network
            for (Node node : knownNodes) {
                String join_msg = "JOIN " + ip + " " + port_receive;
                String join_reply = sendAndRecieve(join_msg, node);
            }

            // TODO: pani - take queries from file and search(can use a seperate method

            for (Node node : knownNodes) {
                String search_msg = "SER " + ip + " " + port_receive + " " + "\"of Tintin\"";
                String search_reply = sendAndRecieve(search_msg, node);
            }

            //String search_reply = sendAndRecieve(search_msg, node);

            BufferedReader bufferedReader = null;
            while (running) {
                bufferedReader = new BufferedReader(new InputStreamReader(System.in));
                String s = bufferedReader.readLine();
                // TODO: pani/ravi - take queries from user input and search(can use a seperate method
                if (s.equals("leave")){
                    running = !running;
                } else if (s.equals("nodes")) {
                    // for debugging purposes
                    if(knownNodes!=null) {
                        for (Node node : knownNodes) {
                            echo(node.ip + ":" + node.port);
                        }
                    }
                    continue;
                }else {
                    String searchText = s;
                    for (Node node : knownNodes) {
                        String search_msg = "SER " + ip + " " + port_receive + " " + "\"" + searchText + "\"";
                        String search_reply = sendAndRecieve(search_msg, node);
                    }
                }

                // TODO: ravi - leave network
                // Leave the distributed network


                // Unregister with BS
                String unreg_msg = "UNREG " + ip + " " + port_receive + " " + username;
                sendAndRecieve(unreg_msg, bs);
            }
            bufferedReader.close();

        } catch (IOException e) {
            System.err.println("IOException " + e);
        } finally {
            if (running == true) { running = false; }
        }
    }

    private String sendAndRecieve(String msg, Node node) throws IOException {
        msg = addLengthToMsg(msg);
        echo("Send and recieve(" + node.ip + ":" + node.port + ")>>" + msg);
        DatagramSocket sock = new DatagramSocket(port_send);
        // node address - node to recieve the msg
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

        echo("Receive(" + incoming.getAddress() + ":" + incoming.getPort() + ")>>" + s);
        sock.close();
        return s;
    }

    private void send(String msg, Node node) throws IOException {
        msg = addLengthToMsg(msg);
        echo("Send(" + ip + ":" + node.port + ")>>" + msg);
        DatagramSocket sock = new DatagramSocket(port_send);
        // node address - node to recieve the msg
        InetAddress node_address = InetAddress.getByName(node.ip);
        byte[] buffer = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, node_address, node.port);
        sock.send(packet);
        sock.close();
    }

    public void setFiles(String[] files) {
        this.files = files;
    }

    private List<String> search(String msg) {

        String[] queries = {"Adventures of Tintin","Jack and Jill","Mission Impossible","Modern Family","Adventures of Tintin 2"};
        List<String> filesFound = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(msg, " ");

        while (st.hasMoreTokens()) {
            String value = st.nextToken();
            for (String s: queries){
                if (s.contains(value)){
                    filesFound.add(s);
                }
            }
        }

        Set setOfFiles = new HashSet(filesFound);
        List<String> fileNames = new ArrayList<String>();

        for (Object i: setOfFiles){
            String x = i.toString().replaceAll(" ","_");
            fileNames.add(x);
        }

        return fileNames;
    }

    public void setQueries(String[] queries) {
        this.queries = queries;
    }

    private void processLeave( StringTokenizer st,DatagramPacket incoming) throws IOException {
        String reply = "LEAVEOK ";
        Node joinee = null;

        String ip = st.nextToken();
        int port = Integer.parseInt(st.nextToken());
        send(reply, new Node(incoming.getAddress().toString().substring(1), incoming.getPort()));

        knownNodes.removeIf(p->p.port== port && p.ip==ip);
    }

    private static class Node{
        String ip;
        int port;

        public Node(String ip, int port){
            this.ip = ip;
            this.port = port;
        }

        public String getIp(){
            return this.ip;
        }

        public int getPort(){
            return this.port;
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

    private static ArrayList<Node> parseRegMessage(String msg){
        String[] parts = msg.split(" ");
        ArrayList<Node> nodes = new ArrayList<>();
        if(parts.length > 3){
            //nodes = new Node[(parts.length - 3)/2];
            for(int i = 3; i < parts.length; i += 2){
                //nodes[(i - 3)/2] = new Node(parts[i], Integer.valueOf(parts[i + 1]));
                nodes.add(new Node(parts[i], Integer.valueOf(parts[i + 1])));
            }
        }
        return nodes;
    }

}
