package com.distributed.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.util.*;

/**
 * Created by Janaka on 2017-11-08.
 */
public abstract class Client {



    protected Node bs;    // bootstrap server
    protected String ip;  // this node's IP
    protected int port_receive;
    protected String[] files; // files in the node
    protected String[] queries;
    protected String username;
    protected volatile boolean running;
    protected ArrayList<Node> knownNodes;

    protected abstract void startListening() throws SocketException ;
    protected abstract String sendAndReceive(String msg, Node node)  throws IOException ;
    protected abstract void send(String msg, Node node)  throws IOException ;
    protected abstract void printClientInfo();


    protected void start(){
        try {
            running = true;
            printClientInfo();
            startListening();
            joinToBS();

            // Join to the distributed network
            echo("Joining to known nodes");
            for (Node node : knownNodes) {
                String join_msg = "JOIN " + ip + " " + port_receive;
                String join_response = sendAndReceive(join_msg, node);
                echo("Join response:" + join_response);
            }

            BufferedReader bufferedReader = null;
            while (running) {
                bufferedReader = new BufferedReader(new InputStreamReader(System.in));
                String s = bufferedReader.readLine();
                // TODO: pani/ravi - take queries from user input and search(can use a seperate method
                if (s.equals("leave")) {
                    running = !running;
                } else if (s.equals("nodes")) {
                    // for debugging purposes
                    if (knownNodes != null) {
                        for (Node node : knownNodes) {
                            echo(node.ip + ":" + node.port);
                        }
                    }
                    continue;
                } else {
                    String searchText = s;
                    for (Node node : knownNodes) {
                        String search_msg = "SER " + ip + " " + port_receive + " " + "\"" + searchText + "\"" + " " + "234";
                        send(search_msg, node);
                    }
                }
            }

            //Leaving the network
            String unreg_msg = "UNREG " + ip + " " + port_receive + " " + username;
            send(unreg_msg, bs);
            bufferedReader.close();

        } catch (IOException e) {
            System.err.println("IOException " + e);
        } finally {
            if (running == true) {
                running = false;
            }
        }
    }

    //simple function to echo data to terminal
    public static void echo(String msg) {
        System.out.println(msg);
    }
    public static void echo(String info, String msg) {
        System.out.println(info + ": " + msg);
    }


    public void setQueries(String[] queries) {
        this.queries = queries;
    }
    public void setFiles(String[] files) {
        this.files = files;
    }

    protected void joinToBS() throws IOException {
        // Register with BS
        String reg_msg = "REG " + ip + " " + port_receive + " " + username;
        String reg_reply = sendAndReceive(reg_msg, bs);
        knownNodes = parseRegMessage(reg_reply);
        System.out.println("Known Nodes:");
        for (Node node : knownNodes) {
            echo(node.ip + ":" + node.port);
        }
    }
    protected String processMessage(String msg, DatagramPacket incoming) throws IOException {
        echo("Incoming Message", incoming.getAddress().getHostAddress() + " : " + incoming.getPort() + " - " + msg);

        StringTokenizer st = new StringTokenizer(msg, " ");
        st.nextToken();
        String command = st.nextToken();

        if (command.equals("JOIN")) {
            return processJoin(st, incoming);
        }
        else if (command.equals("SER")) {
            return processSearch(st);
        }
        else if (command.equals("LEAVE")) {
            return processLeave(st, incoming);
        }
        return null;
    }
    protected String processLeave(StringTokenizer st, DatagramPacket incoming) throws IOException {
        String reply = "LEAVEOK ";
        Node joinee = null;

        String ip = st.nextToken();
        int port = Integer.parseInt(st.nextToken());

        knownNodes.removeIf(p -> p.port == port && p.ip == ip);
        return reply;
    }
    protected String processSearch(StringTokenizer st) throws IOException {

        boolean isOkay = true;
        String reply = "SEROK ";
        Node sender = null;

        String ip = st.nextToken();
        int port = Integer.parseInt(st.nextToken());


        String query = "";

        while (st.hasMoreTokens()) {
            String value = st.nextToken();
            Character lastChar = value.charAt(value.length() - 1);
            if (lastChar.equals('\"')) {
                value = value.substring(0, value.length() - 1);
                query = query + value;
                break;
            } else {
                query = query + value + " ";
            }
        }

        int hops = Integer.parseInt(st.nextToken());

        String searchQuery = query.substring(1, query.length());

        List<String> results = search(searchQuery);

        if (results.isEmpty()) {
            reply += "0";
            isOkay = true;
        } else {
            reply += results.size() + " ";
            for (Object fileName : results) {
                reply += fileName.toString() + " ";
            }
            isOkay = true;
        }

        if (isOkay) {
            echo(ip + " " + port);
            sender = new Node(ip, port);
            send(reply, sender);
        }

        hops++;
        if (hops <= 235) {
            for (Node node : knownNodes) {
                String search_msg = "SER " + ip + " " + port + " " + "\"" + searchQuery + "\"" + " " + hops;
                //String search_msg = "SER " + ip + " " + port_receive + " " + "\"of Tintin\"";
                send(search_msg, node);

            }
        }
        return null;
    }
    protected String processJoin(StringTokenizer st, DatagramPacket incoming) throws IOException {
        boolean isOkay = true;
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

        if (isOkay) {
            joinee = new Node(ip, port);
            knownNodes.add(joinee);
            reply += "0";
            // incoming.getAddress() returns InetAddress like /127.0.0.1 - therefore convert to a ip string
            return reply;
        }
        return null;
    }
    protected List<String> search(String msg) {

        String[] queries = {"Adventures of Tintin", "Jack and Jill", "Mission Impossible", "Modern Family", "Adventures of Tintin 2", "Jack and Jill 2"};
        List<String> filesFound = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(msg, " ");

        while (st.hasMoreTokens()) {
            String value = st.nextToken();
            for (String s : files) {
                if (s.contains(value)) {
                    filesFound.add(s);
                }
            }
        }

        Set setOfFiles = new HashSet(filesFound);
        List<String> fileNames = new ArrayList<String>();

        for (Object i : setOfFiles) {
            String x = i.toString().replaceAll(" ", "_");
            fileNames.add(x);
        }

        return fileNames;
    }

    // Add length parameter in front of the message
    protected static String addLengthToMsg(String msg) {
        if (msg.length() > 9000)
            return "0004";
        int len = msg.length() + 5;
        msg = len + " " + msg;
        while (msg.length() < len) {
            msg = "0" + msg;
        }
        return msg;
    }
    protected static ArrayList<Node> parseRegMessage(String msg) {
        String[] parts = msg.split(" ");
        ArrayList<Node> nodes = new ArrayList<>();
        if (parts.length > 3) {
            //nodes = new com.distributed.app.Node[(parts.length - 3)/2];
            for (int i = 3; i < parts.length; i += 2) {
                //nodes[(i - 3)/2] = new com.distributed.app.Node(parts[i], Integer.valueOf(parts[i + 1]));
                nodes.add(new Node(parts[i], Integer.valueOf(parts[i + 1])));
            }
        }
        return nodes;
    }

}
