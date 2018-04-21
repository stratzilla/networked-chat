package chat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Connection class for Socket Programmed Chat
 *
 * @author Robert Scott - 2018
 */
public class Connection {

    Socket sock; // socket for the connection
    static String username; // username tied to connection
    ObjectInputStream inStream;
    ObjectOutputStream outStream;

    /**
     * method which connects socket and socket streams to appropriate address
     * and port
     *
     * @return boolean value on successful connection
     */
    public boolean connect() {
        try {
            sock = new Socket("localhost", 5000); // sample connection
            inStream = new ObjectInputStream(sock.getInputStream());
            outStream = new ObjectOutputStream(sock.getOutputStream());
            new ServerListener().start(); // add server listener
            /**
             * send username to stream so server can read it
             */
            outStream.writeObject(username);
        } catch (IOException e) {
            System.out.println(" * Error connecting to the server.");
            System.out.println(e);
            return false; // connection failed
        }
        return true; // otherwise give it the green light
    }

    /**
     * method which disconnects user connection from server
     */
    public void disconnect() {
        try {
            inStream.close(); // close input socket stream
            outStream.close(); // likewise for output
            sock.close(); // close socket
        } catch (IOException e) {
            System.out.println(" * Error disconnecting from server.");
            System.out.println(e);
        }
    }

    /**
     * listener to gleam information about server
     */
    class ServerListener extends Thread {

        @Override
        public void run() {
            /**
             * as long as server is alive, prompt for input
             */
            while (true) {
                try {
                    System.out.println((String) inStream.readObject());
                    System.out.print(" > ");
                } catch (IOException | ClassNotFoundException e) {
                    break;
                }
            }
        }
    }

}
