import java.io.*;
import java.net.*;
import java.util.*;

public class Server {

    // Server socket.
    private static ServerSocket serverSocket = null;

    // Client socket.
    private static Socket clientSocket = null;

    public static ArrayList<clientThread> clients = new ArrayList<clientThread>();

    public static void main(String args[]) {

        int portNumber = 6000;

        System.out.println("Server is running on port: " + portNumber);

        try {
            serverSocket = new ServerSocket(portNumber);
        } catch (IOException e) {
            System.out.println("Server Socket cannot be created");
        }

        int clientNum = 1;
        while (true) {
            try {
                clientSocket = serverSocket.accept();
                clientThread currClient =  new clientThread(clientSocket, clients);
                clients.add(currClient);
                currClient.start();
                System.out.println("Client <"  + clientNum + "> is connected!");
                clientNum++;

            } catch (IOException e) {
                System.out.println("Client could not be connected");
            }
        }

    }
}

class clientThread extends Thread {

    private String clientName = null;
    private ObjectInputStream is = null;
    private ObjectOutputStream os = null;
    private Socket clientSocket;
    private final ArrayList<clientThread> clients;

    public clientThread(Socket clientSocket, ArrayList<clientThread> clients) {

        this.clientSocket = clientSocket;
        this.clients = clients;

    }

    public void run() {

        try {

            is = new ObjectInputStream(clientSocket.getInputStream());
            os = new ObjectOutputStream(clientSocket.getOutputStream());

            String name;
            while (true) {

                boolean userFound = false;

                synchronized(this) {
                    this.os.writeObject("Please enter your name: ");
                    this.os.flush();
                    name = ((String) this.is.readObject()).trim(); // deleting spacing to get the name only

                    // Check if the username is already taken!
                    if(clients != null) {
                        for (clientThread c:
                                clients) {
                            if(c.clientName != null && c.clientName.equals("@"+name)) {
                                userFound = true;
                            }
                        }
                    }

                    if(userFound) {
                        this.os.writeObject("Username is taken ... Please choose another UNIQUE username");
                        continue;
                    }

                    if ((name.indexOf('@') == -1) || (name.indexOf('!') == -1)) {
                        break;
                    } else {
                        this.os.writeObject("Username should not contain '@' or '!' characters.");
                        this.os.flush();
                    }

                }

            }


            System.out.println("Client Name is " + name);

            this.os.writeObject("*** Welcome " + name + " to ChatiApp ***");
            this.os.writeObject("To broadcast: write your message");
            this.os.writeObject("To send to specific user: @username: <Your message>");
            this.os.writeObject("To view all members: $");
            this.os.writeObject("To leave: type bye or exit");
            this.os.flush();

            synchronized(this) {

                for (clientThread currClient : clients) {
                    if (currClient != null && currClient == this) {
                        clientName = "@" + name;
                        break;
                    }
                }


                /* inform other users that a new client has joined the pool :D */
                for (clientThread currClient : clients) {
                    if (currClient != null && currClient != this) {
                        currClient.os.writeObject(name + " has joined");
                        currClient.os.flush();
                    }
                }
            }


            /* Start the conversation. */
            while (true) {

                this.os.writeObject("Type your command:");
                this.os.flush();

                String line = (String) is.readObject();


                if (line.toLowerCase().equals("bye")
                        || line.toLowerCase().equals("exit"))
                    break;

                if (line.startsWith("$"))
                    viewAll();

                else if (line.startsWith("@"))
                    unicast(line,name);

                else
                    broadcast(line,name);

            }

            /* close the Session for user */

            this.os.writeObject("*** Bye Bye  " + name + "! 👋 ***");
            this.os.flush();
            System.out.println(name + " disconnected.");
            clients.remove(this);


            /* inform other users that a new client has left the pool */
            synchronized(this) {

                if (!clients.isEmpty()) {

                    for (clientThread curClient : clients) {

                        if (curClient != null && curClient != this && curClient.clientName != null) {
                            curClient.os.writeObject("*** User <" + name + "> disconnected ***");
                            curClient.os.flush();
                        }
                    }
                }
            }

            this.is.close();
            this.os.close();
            clientSocket.close();

        } catch (IOException e) {

            System.out.println(e);

        } catch (ClassNotFoundException e) {

            System.out.println(e);
        }
    }

    void viewAll() {

        try {
            this.os.writeObject("\nAll available people: ");
        } catch (Exception e) {
            System.out.println(e);
        }
        for (clientThread c:
             clients) {
            try {
                this.os.writeObject(c.clientName);
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    /**** Send message to all the client connected to the server ***/

    void broadcast(String line, String name) throws IOException, ClassNotFoundException {

        synchronized(this){

            for (clientThread currClient : clients) {

                if (currClient != null && currClient.clientName != null
                        && currClient.clientName!=this.clientName) {
                    currClient.os.writeObject("<" + name + "> " + line);
                    currClient.os.flush();
                }
            }
            this.os.writeObject("message sent successfully.");
            this.os.flush();
            System.out.println("message sent by " + this.clientName.substring(1));
        }

    }


    /**** Send message to a particular client connected to the server ***/

    void unicast(String line, String name) throws IOException, ClassNotFoundException {

        String[] words = line.split(":", 2);

        if (words.length > 1 && words[1] != null) {

            words[1] = words[1].trim();

            if (!words[1].isEmpty()) {

                for (clientThread currClient : clients) {
                    if (currClient != null && currClient != this && currClient.clientName != null
                            && currClient.clientName.equals(words[0])) {
                        currClient.os.writeObject("<" + name + "> " + words[1]);
                        currClient.os.flush();

                        System.out.println(this.clientName.substring(1) + " send a private message to client "+ currClient.clientName.substring(1));

                        this.os.writeObject("Private Message sent to " + currClient.clientName.substring(1));
                        this.os.flush();
                        break;
                    }
                }
            }
        }
    }
}
