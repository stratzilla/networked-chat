package chat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Server class for Socket Programmed Chat
 *
 * @author Robert Scott - 2018
 */
public class Server {

    int numConnections; // number of connections
    ArrayList<UserThread> listClients; // list of clients/connections
    boolean validConnection; // if server connection is alive ornot
    Room chatLog; // for logging purposes

    /**
     * constructor for Server class
     */
    private Server() {
        validConnection = true; // initially assume connection is valid
        listClients = new ArrayList<>(); // init list connections
        chatLog = new Room("./log.txt"); // create or load log
    }

    /**
     * method to launch server, establishing connection
     */
    private void launch() {
        try {
            sendServer("Setting up server...");
            // accept new connection requests from clients
            try (ServerSocket requests = new ServerSocket(5000)) {
                sendServer("Open for user connections.");
                // if connection is still valid, take clients
                while (validConnection) {
                    Socket sock = requests.accept();
                    addClient(sock); // append to list of clients
                }
            }
        } catch (IOException e) {
            sendServer("Error setting up server.");
            sendServer(e);
        }
        /**
         * if you reach this point, the server is dead anyways, so ensure it's
         * closed properly by closing all of the streams, etc
         */
        kill();
    }

    /**
     * method which kills all server processes
     */
    private void kill() {
        /**
         * for each client, make sure it's closed
         */
        listClients.forEach((c) -> {
            c.disconnect(); // close socket and streams of client
        });
    }

    /**
     * method which adds a client to the list of clients
     *
     * @param s - the client socket
     */
    private void addClient(Socket s) {
        numConnections++; // more connections, increment this
        UserThread newClient = new UserThread(s); // create new
        listClients.add(newClient); // append to list
        newClient.start(); // start client thread
    }

    /**
     * method which sends a message to the server console
     *
     * @param m - the message to send
     */
    private void sendServer(String m) {
        System.out.println(" * " + m);
        /**
         * only write server messages to log. This includes server status
         * messages as well as chats. Does not include private messages.
         */
        chatLog.writeLog(m);
    }

    /**
     * overloaded method for sending to server. This one is meant for exceptions
     * and is not written to log
     *
     * @param e - the exception to send to server
     */
    private void sendServer(Exception e) {
        System.out.println(" * " + e);
    }

    /**
     * method to send a message to all connected clients. Synchronized so
     * multiple clients cannot block each other.
     *
     * @param m - the message to send
     */
    private synchronized void sendAll(String m) {
        // for each client, do this
        listClients.forEach((c) -> {
            try {
                c.outStream.writeObject(m);
            } catch (IOException e) {
                /**
                 * if sending failed, assume user has disconnected and log them
                 * out
                 */
                logOut(c.clientId);
            }
        });
        sendServer(m); // also send to server console
    }

    /**
     * method which sends a message to a particular client/user
     *
     * @param u - the user to send to
     * @param m - the message to send
     * @param s - the sender
     */
    private synchronized void sendClient(String u, String m, UserThread s) {
        boolean foundUser = false; // assume user doesn't exist initially
        for (UserThread c : listClients) {
            if (c.getUsername().equals(u)) { // if found
                foundUser = true;
                try { // try to send them the message
                    c.outStream.writeObject(m);
                } catch (IOException e) { // if failed, assume disconnected
                    logOut(c.clientId); // log them out
                    // let yourself know it failed
                    sendSelf("Unable to send message to " + u + ".", s);
                } finally { // in either case, don't continue searching for user
                    break;
                }
            }
        }
        if (!foundUser) { // if cannot find
            sendSelf("User " + u + " not found.", s);
        } else { // let yourself know it worked
            sendSelf("Message sent to " + u + ".", s);
        }
    }

    /**
     * method to send message to self
     *
     * @param m - the message to send
     * @param c - the client who is to receive it
     */
    private synchronized void sendSelf(String m, UserThread c) {
        try {
            c.outStream.writeObject(" * " + m);
        } catch (IOException e) {
            sendServer("Error sending message to self.");
            sendServer(e);
        }
    }

    /**
     * method which checks which users are online
     *
     * @return the online users in a concatenated string
     */
    private String checkOnline() {
        String toReturn = "";
        int i = 0;
        for (UserThread c : listClients) {
            toReturn += c.getUsername();
            i++;
            if (i != listClients.size()) {
                toReturn += ", ";
            }
        }
        return toReturn;
    }

    /**
     * method to log out a specific client
     *
     * @param id - the client to log out
     */
    private synchronized void logOut(int id) {
        int i = 0;
        for (UserThread c : listClients) {
            if (c.clientId == id) {
                listClients.remove(i);
                c.validClient = false; // invalidate client
                sendAll(c.getUsername() + " has disconnected.");
                break; // don't continue looking for him
            }
            i++;
        }
    }

    /**
     * main driver for Server class
     *
     * @param args - unused
     */
    public static void main(String[] args) {
        Server server = new Server(); // init server
        server.launch(); // launch it
    }

    /**
     * UserThread class
     */
    class UserThread extends Thread {

        int clientId; // id for this class
        Message clientMessage; // used later, hold messages
        boolean validClient; // if client is alive or not
        Socket sock; // socket
        String username; // client's user's username
        ObjectInputStream inStream; // in socket stream
        ObjectOutputStream outStream; // out socket stream

        /**
         * constructor for UserThread class
         *
         * @param s - socket user is connecting from
         */
        UserThread(Socket s) {
            validClient = true; // assume connection is viable
            clientId = numConnections; // easy way to number clients
            sock = s; // init socket
            try {
                // init socket streams
                outStream = new ObjectOutputStream(s.getOutputStream());
                inStream = new ObjectInputStream(s.getInputStream());
                // get username after signed in
                username = (String) inStream.readObject();
                // let everyone know you joined
                sendAll(username + " has joined the chat room.");
            } catch (IOException | ClassNotFoundException e) {
                sendServer("Error connecting client.");
                sendServer(e);
            }
        }

        /**
         * getter method to return client username
         *
         * @return the client's username
         */
        private String getUsername() {
            return username;
        }

        /**
         * main thread method
         */
        @Override
        public void run() {
            while (validClient) { // while client is alive
                try {
                    // get messages
                    clientMessage = (Message) inStream.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    validClient = false; // if unable to, kill client
                    break;
                }
                // interpret message to send it to correct spot
                decipherMessage(clientMessage.getMessage());
            }
            // if you hit here, client is dead and can be logged out
            logOut(clientId);
            disconnect(); // disconnect its socket and streams
        }

        /**
         * method to disconnect client from socket and streams
         */
        void disconnect() {
            try {
                inStream.close();
                outStream.close();
                sock.close();
            } catch (IOException e) {
                System.out.println(" * Error disconnecting from server.");
                System.out.println(e);
            }
        }

        /**
         * method to interpret type of message to send it to correct spot
         *
         * @param m - the message to send
         */
        private void decipherMessage(String m) {
            // find substring of message containing only message contents
            String mS = m.split(" ", 4)[3];
            if (mS.equalsIgnoreCase("/logout")) { // if '/logout' said
                logOut(clientId); // log self out
            } else if (mS.equalsIgnoreCase("/online")) { // if '/online' said
                try {
                    // write to self who is online
                    outStream.writeObject(checkOnline());
                } catch (IOException e) {
                    sendServer("Error reading online list.");
                    sendServer(e);
                }
            } else if (mS.charAt(0) == '@') { // if private message
                sendClient(mS.split(" ", 2)[0].substring(1), m, this);
            } else { // otherwise public
                sendAll(m);
            }
        }
    }
}
