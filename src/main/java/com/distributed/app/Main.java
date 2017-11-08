package com.distributed.app;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;


public class Main {

    public static void main(String[] args) {

        // Normal
        if (args.length != 7)
        {
            echo("Invalid arguments");
            return;
        }
        Client client;
        String clientType = args[6];
        if(args[6].equals("REST")){
            echo("Start with Rest Client");
            client = RestClient.fromArgs(args);
        }
        else{
            echo("Start with UDP Client");
            client = UdpClient.fromArgs(args);
        }

        String[] files = getFiles("FileNames.txt");
        client.setFiles(files);

        String[] queries = getQueries("Queries.txt");
        client.setQueries(queries);


        JFrame frame =new JFrame("DisplayDialog");
        frame.setTitle(clientType+" Client");
        DisplayDialog dialog = new DisplayDialog();
        dialog.clientType.setText(clientType);
        dialog.logText.setText("jldkafj\nldlldd");
        frame.setContentPane(dialog.getContentPane());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        client.start();

    }

    //simple function to echo data to terminal
    public static void echo(String msg) {
        System.out.println(msg);
    }


    private static String[] getFiles(String fileName){
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        ArrayList<String> files = new ArrayList<>();
        try {
            fileReader = new FileReader(fileName);
            bufferedReader = new BufferedReader(fileReader);
            ArrayList<String> allFiles = new ArrayList<String>();

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                allFiles.add(line);
            }

            Random r = new Random();
            int filesLow = 3;
            int filesHigh = 5;
            int noOfFilesInNode = r.nextInt(filesHigh - filesLow) + filesLow;

            int noOfAllFiles = allFiles.size();
            for (int i = 0; i < noOfFilesInNode; i++){
                int random = r.nextInt(noOfAllFiles);
                files.add(allFiles.get(random));
                allFiles.remove(random);
                noOfAllFiles--;
            }
            echo("Files in the com.distributed.app.Node:");
            files.forEach(Main::echo);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileReader != null)
                    fileReader.close();
                if (bufferedReader != null)
                    bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return  files.toArray(new String[files.size()]);
        }
    }

    private static String[] getQueries(String fileName){
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        ArrayList<String> allQueries = new ArrayList<>();
        try {
            fileReader = new FileReader(fileName);
            bufferedReader = new BufferedReader(fileReader);
            allQueries = new ArrayList<String>();

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                allQueries.add(line);
            }
            echo("Queries in the com.distributed.app.Node:");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileReader != null)
                    fileReader.close();
                if (bufferedReader != null)
                    bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return  allQueries.toArray(new String[allQueries.size()]);
        }
    }
}
