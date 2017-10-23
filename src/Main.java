import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

public class Main {

    public static void main(String[] args) {
        if (args.length != 6)
            return;

        Client client = Client.fromArgs(args);

        client.start();

    }

    //simple function to echo data to terminal
    public static void echo(String msg) {
        System.out.println(msg);
    }


}
