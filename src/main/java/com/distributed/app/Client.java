package com.distributed.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.*;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Created by Janaka on 2017-11-08.
 */
public abstract class Client {


    protected Node bs;    // bootstrap server
    protected String ip;  // this node's IP
    protected int port_receive;
    protected int port_send;
    protected String[] files; // files in the node
    protected String[] queries;
    protected String username;
    protected volatile boolean running;
    protected ArrayList<Node> knownNodes;

    HashMap<String, Long> passedQueries = new HashMap<>();
    HashMap<String, List<String>> queryResults = new HashMap<>();

    protected abstract void startListening() throws SocketException;

    protected abstract String sendAndReceive(String msg, Node node) throws Exception;

    protected abstract void send(String msg, Node node) throws Exception;


    protected void start() {
        try {
            running = true;
            printClientInfo();
            startListening();
            joinToBS();

            // Join to the distributed network
            echo("Joining to known nodes");
            for (Node node : knownNodes) {
                String join_msg = Constants.COMMAND_JOIN + " " + ip + " " + port_receive;
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
                } else if (s.length() > 0) {
                    trigerSearch(s);
                }
            }

            //Leaving the network
            String unreg_msg = Constants.COMMAND_UNREG+" " + ip + " " + port_receive + " " + username;
            sendUdp(unreg_msg, bs);
            bufferedReader.close();

        } catch (Exception e) {
            System.err.println("Exception " + e);
        } finally {
            if (running == true) {
                running = false;
            }
        }
    }

    public void trigerSearch(String searchText) throws Exception {
        UUID uuid = UUID.randomUUID();
        for (Node node : knownNodes) {
            String search_msg = Constants.COMMAND_SEARCH+" " + uuid + " " + ip + " " + port_receive + " " + "\"" + searchText + "\"" + " " + "1";
            send(search_msg, node);
        }
    }

    //simple function to echo data to terminal
    public static void echo(String msg) {
        System.out.println(msg);
    }

    public static void echo(String info, String msg) {
        System.out.println(info + ": " + msg);
    }

    protected void printClientInfo() {
        echo("BS", bs.ip + ":" + bs.port);
        echo("My IP", ip + ":" + port_receive);
        echo("Send Port", ip + ":" + port_send);
    }

    public void setQueries(String[] queries) {
        this.queries = queries;
    }

    public void setFiles(String[] files) {
        this.files = files;
    }

    protected void joinToBS() throws IOException {
        // Register with BS
        String reg_msg = Constants.COMMAND_REG+" " + ip + " " + port_receive + " " + username;
        String reg_reply = sendAndReceiveUdp(reg_msg, bs);
        knownNodes = parseRegMessage(reg_reply);
        System.out.println("Known Nodes:");
        for (Node node : knownNodes) {
            echo(node.ip + ":" + node.port);
        }
    }

    protected String processMessage(String msg) throws Exception {
        StringTokenizer st = new StringTokenizer(msg, " ");
        st.nextToken();
        String command = st.nextToken();

        if (command.equals(Constants.COMMAND_JOIN)) {
            return processJoin(st);
        } else if (command.equals(Constants.COMMAND_SEARCH)) {
            return processSearch(st);
        } else if (command.equals(Constants.COMMAND_LEAVE)) {
            return processLeave(st);
        } else if (command.equals(Constants.COMMAND_SEARCH_OK)) {
            return processSearchResult(st);
        }
        return null;
    }

    protected String processLeave(StringTokenizer st) throws Exception {
        String reply = Constants.COMMAND_LEAVE_OK + " ";

        final String ip = st.nextToken();
        final int port = Integer.parseInt(st.nextToken());

        knownNodes.removeIf(new Predicate<Node>() {
            @Override
            public boolean test(Node p) {
                return p.port == port && p.ip == ip;
            }
        });
        return reply;
    }

    protected String processSearch(StringTokenizer st) throws Exception {

        boolean isOkay;
        Node sender;

        String uuid = st.nextToken();
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


        //If the particular query is already passed. Leave it.
        Query q = new Query(new Node(ip, port), searchQuery, uuid);
        Long millis = System.currentTimeMillis();
        for (Map.Entry m : passedQueries.entrySet()) {
            if ((Long) m.getValue() < millis - 10000) {
                passedQueries.remove(m.getKey());
            }
        }
        if (passedQueries.containsKey(q.getHash()))
            return null;
        passedQueries.put(q.getHash(), millis);

        List<String> results = search(searchQuery);

        String reply = Constants.COMMAND_SEARCH_OK + " " + q.getHash() + " " + this.ip + " " + this.port_receive + " ";
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
        if (hops < 5) {
            for (Node node : knownNodes) {
                String search_msg = "SER " + uuid + " " + ip + " " + port + " " + "\"" + searchQuery + "\"" + " " + hops;
                //String search_msg = "SER " + ip + " " + port_receive + " " + "\"of Tintin\"";
                send(search_msg, node);

            }
        }
        return null;
    }

    protected String processJoin(StringTokenizer st) throws IOException {
        boolean isOkay = true;
        String reply = Constants.COMMAND_JOIN_OK + " ";
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

    protected String processSearchResult(StringTokenizer st) {

        String query = st.nextToken();
        if (!queryResults.containsKey(query)) {
            queryResults.put(query, new ArrayList<String>());
        }
        String result = "";
        while (st.hasMoreTokens()) {
            result += " " + st.nextToken();
        }
        queryResults.get(query).add(result);
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


    // Send and receive on the same port
    protected String sendAndReceiveUdp(String msg, Node node) throws IOException {
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

    // only send do not wait for a response
    protected void sendUdp(String msg, Node node) throws IOException {

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
        if (msg == null)
            return new ArrayList<>();
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
