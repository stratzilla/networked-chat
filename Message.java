package chat;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Message class for Socket Programmed Chat
 *
 * @author Robert Scott - 2018
 */
public class Message implements Serializable {

    private final String message; // message contents
    private String username; // user who sent message
    private final SimpleDateFormat datetime; // date of message

    /**
     * constructor for Message class
     *
     * @param u - the user who sent the message
     * @param m - the message itself
     */
    public Message(String u, String m) {
        // create date to prepend message
        datetime = new SimpleDateFormat("HH:mm");
        // concatenate date, author, and message
        message = datetime.format(new Date()) + " - " + u + ": " + m;
    }

    /**
     * getter method to retrieve message contents
     *
     * @return message contents
     */
    public String getMessage() {
        return message;
    }
}
