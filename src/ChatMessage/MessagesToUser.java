package ChatMessage;

public class MessagesToUser {

    public static final String SERVER = " Your Current Server IP Address : ";



    //    General Messages
    public static final String MEMBERS_LIST = "\\members";
    public static final String QUIT_MESSENGER = "\\quit";
    public static final String USER_JOINED = " joined the chat.";
    public static final String USER_DISCONNECTED = " left the chat.";

    //    Welcome Message
    public static final String WELCOME_MESSAGE = "Welcome to the Messenger.\n To quit type " + QUIT_MESSENGER
            + ".\n To View Current Online Members type " + MEMBERS_LIST
            + ".\n To Send a Message type @username.\n";

    //    Error Messages
    public static final String SERVER_FULL =
            "Server is Currently FULL. Please Try again in a few Seconds.";

    public static final String USERNAME_PROMPT = "Please Enter Your Name :";
    public static final String ILLEGAL_USERNAME = "Your name should not contain the character \"@\".";
    public static final String USERNAME_EXISTS = "Username already in use!.";

    public static final String INVALID_MSG = "Please Enter a Valid Message.";
    public static final String NO_RECIEVER = "Please specify the user with @username.";
    public static final String INVALID_USER = "User not found. Please enter a valid username."
            + "\nTo View Current Online Members type " + MEMBERS_LIST + "\n";
}
