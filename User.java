package chat;

import java.io.IOException;
import java.util.Scanner;

/**
 * User class for Socket Programmed Chat
 *
 * @author Robert Scott - 2018
 */
public class User extends Connection {

    /**
     * constructor for User class
     *
     * @param u - the username to init
     */
    private User(String u) {
        username = u;
    }

    /**
     * method to send message to everyone
     *
     * @param m - the message to send
     */
    private void sendAll(Message m) {
        try {
            outStream.writeObject(m); // write it to the stream
        } catch (IOException e) {
            sendSelf("Error sending message.");
            sendSelf(e);
        }
    }

    /**
     * method to send message to self
     *
     * @param m - the message to send
     */
    private static void sendSelf(String m) {
        System.out.println(" * " + m);
    }

    /**
     * overloaded method to send message to self, this one for exceptions
     *
     * @param e - the exception to send
     */
    private static void sendSelf(Exception e) {
        System.out.println(" * " + e);
    }

    /**
     * method to produce introductory message on server connect
     *
     * @return the tutorial message
     */
    private static String introMessage() {
        String toReturn = "";
        toReturn += "Welcome to the server.\n\n";
        toReturn += "Send a message prepended by @username to send "
                + "a private message.\n";
        toReturn += "Type /logout to disconnect from the server.\n";
        toReturn += "Type /online to see currently online users.\n";
        return toReturn;
    }

    /**
     * main driver for User class
     *
     * @param args - unused
     */
    public static void main(String[] args) {
        User theUser; // init user
        try (Scanner scan = new Scanner(System.in)) {
            sendSelf("What's your name? "); // prompt for chatting name
            username = scan.nextLine(); // get name
            theUser = new User(username); // instantiate user
            theUser.connect(); // connect
            sendSelf(introMessage()); // get welcome message
            while (true) {
                System.out.print(" > "); // prompt for input
                String message = scan.nextLine(); // gather input
                if (message.equals("/logout")) { // stop prompting if logged out
                    break;
                } else { // send all messages to server to be rerouted
                    theUser.sendAll(new Message(username, message));
                }
            }
        }
        // if you get here, user has disconnected
        theUser.disconnect();
    }
}
