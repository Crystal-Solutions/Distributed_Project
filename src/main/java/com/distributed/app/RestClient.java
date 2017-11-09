package com.distributed.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.StringJoiner;

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.port;

import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Created by Janaka on 2017-11-08.
 */
public class RestClient extends Client {

    private final String USER_AGENT = "Mozilla/5.0";

    private RestClient(String[] args) {
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
        startServer(port_receive);
    }

    public void startServer(int portListen){
        port(portListen);
        get("/api/:msg", new Route() {
            @Override
            public Object handle(Request request, Response response) {
                String reply = "";
                try {
                    String msg = request.params(":msg");
                    msg = URLDecoder.decode(msg, "UTF-8");
                    echo("Receive",msg);
                    String r = processMessage(msg);
                    if(r!=null)
                        reply = r;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return reply;
            }
        });

        get("/info", new Route() {
            @Override
            public Object handle(Request request, Response response) {
                String reply = "";

                reply+="Files:\n";
                for(String s:files){
                    reply+=s+"\n";
                }

                reply+= "\nPassed Messages:\n";
                for(Map.Entry m:passedQueries.entrySet()){
                    reply+= m.getKey() +"\n";
                }


                reply += "\nSearch Results:\n";
                for(Map.Entry m:queryResults.entrySet()){

                    reply+= m.getKey() +":\n";
                    for(String s:(ArrayList<String>) m.getValue()){
                        reply+= ">>>"+s+"\n";
                    }
                }

                response.header("Access-Control-Allow-Origin", "*");
                return reply;
            }
        });

        post("/search/:q", new Route() {
            @Override
            public Object handle(Request request, Response response) {
                try {
                    String q = request.params(":q");
                    q = URLDecoder.decode(q, "UTF-8");
                    trigerSearch(q);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                response.header("Access-Control-Allow-Origin", "*");
                return "Message:"+request.params(":q");
            }
        });
    }


    @Override
    protected String sendAndReceive(String msg, Node node) throws Exception {
        echo("Send",msg);
        msg = addLengthToMsg(msg);
        msg = URLEncoder.encode(msg, "UTF-8");
        return sendGet(node.getHttpUrl()+"/api/"+msg);
    }

    @Override
    protected void send(String msg, Node node) throws Exception {
        sendAndReceive(msg,node);
    }


    public static Client fromArgs(String[] args) {
        return new RestClient(args);
    }

    // HTTP GET request
    private String sendGet(String url) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("User-Agent", USER_AGENT);


        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        return response.toString();

    }

}
