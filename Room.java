package chat;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Room class for Socket Programmed Chat
 *
 * @author Robert Scott - 2018
 */
public class Room {

    PrintWriter output; // for logging purposes

    /**
     * constructor method for Room class
     *
     * @param l - the filepath of the log file
     */
    public Room(String l) {
        try {
            output = new PrintWriter(new BufferedWriter(new FileWriter(l, true)));
        } catch (IOException e) {
            System.out.println(" * Error opening log file.");
        }
    }

    /**
     * method to append text to log file
     *
     * @param s - the string to append to log
     */
    public void writeLog(String s) {
        output.println(s);
        output.flush();
    }
}
