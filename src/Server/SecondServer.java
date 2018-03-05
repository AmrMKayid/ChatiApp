package Server;

import ChatMessage.ChatMassage;
import ChatMessage.Type;
import Client.ClientThread;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class SecondServer implements Runnable {

    private static Socket socket;
    private static ObjectOutputStream outToServer;
    private static ObjectInputStream InFromServer;

    private static Scanner UserInput;
    private static String mainServerIP;
    private static int localPort;

    private static ServerSocket serverSocket;
    private static Socket clientSocket;

    private static ArrayList<ClientThread> currentClients;
    public HashMap<Integer, Socket> clientsIDS;
    public HashMap<Socket, Integer> SocketsIDs;
    private int counter;


    public SecondServer () {

        try {
            System.out.println("Your Current Server IP Address: " +
                    InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        UserInput = new Scanner(System.in);
        System.out.println("Please Enter the Main Server IP: ");
        mainServerIP = UserInput.nextLine();

        System.out.println("Please Enter your PORT number: ");
        localPort = UserInput.nextInt();

        try {
            socket = new Socket(mainServerIP, 6000);
            clientsIDS = new HashMap<>(); SocketsIDs = new HashMap<>();
            InFromServer = new ObjectInputStream(socket.getInputStream());
            outToServer = new ObjectOutputStream(socket.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (socket != null &&
              InFromServer != null &&
              outToServer != null) {
            new Thread(this).start();
        }

        try {
            serverSocket = new ServerSocket(localPort);
            currentClients = new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            try {
                clientSocket = serverSocket.accept();
                currentClients.add(new ClientThread(this,
                                        clientSocket, currentClients));
                currentClients.get(currentClients.size() - 1).start();
                clientsIDS.put(counter++, clientSocket);
                SocketsIDs.put(clientSocket, counter - 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("New user is connected!");
        }

    }

    @Override
    public void run() {
        Object data;
        try {
            while ((data = InFromServer.readObject()) != null) {
                ChatMassage msg = (ChatMassage) data;

                switch (msg.type) {
                    case APPROVED:
                        ChatMassage loginMsg = new ChatMassage(Type.APPROVED, msg.data);
                        sendMessageThroughServer(loginMsg, clientsIDS.get(msg.loginUser));
                        break;
                    case USER_EXISTS:
                        loginMsg = new ChatMassage(Type.USER_EXISTS, msg.data);
                        sendMessageThroughServer(loginMsg, clientsIDS.get(msg.loginUser));
                        break;
                    case USER_NOT_FOUND: sendMessageThroughServer(msg, getUserSocket(msg.from)); break;
                    case ALL_MEMBERS: sendMessageThroughServer(msg, getUserSocket(msg.from)); break;
                    case ERROR: sendMessageThroughServer(msg, getUserSocket(msg.from)); break;
                    case MESSAGE:
                        if (msg.isAlive()) {
                            if (getUserSocket(msg.to) != null) {
                                sendMessageThroughServer(msg, getUserSocket(msg.to));
                                break;
                            }
                        } else {
                            msg.type = Type.ERROR;
                            if (getUserSocket(msg.from) != null)
                                sendMessageThroughServer(msg, getUserSocket(msg.from));
                        }
                        break;
                    default: break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMessageThroughServer(ChatMassage msg, Socket socket) {
        for (ClientThread ct : currentClients) {
            if (ct.clientSocket == socket) {
                try {
                    ct.sendToUser.writeObject(msg);
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Socket getUserSocket(String username) {
        for (ClientThread ct : currentClients)
            if (ct.clientName.equals(username))
                return ct.clientSocket;
        return null;
    }

    public void userExists(String username, int newUser) {
        ChatMassage msg = new ChatMassage(Type.ADD, username);
        msg.loginUser = newUser;
        try {
            outToServer.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeUser(String username) {
        synchronized (this) {
            for (ClientThread ct : currentClients) {
                if (ct.clientName.equals(username)) {
                    currentClients.remove(currentClients.indexOf(ct));
                    try {
                        outToServer.writeObject(new ChatMassage(Type.REMOVE, username));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
    }

    public void getAllMembers(String username) {
        ChatMassage msg = new ChatMassage(Type.ALL_MEMBERS, null);
        msg.from = username;
        try {
            outToServer.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessageToUser(ChatMassage msg) {
        msg.decreaseTTL();
        if (msg.isAlive()) {
            try {
                outToServer.writeObject(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            msg.type = Type.ERROR;
            if (getUserSocket(msg.from) != null)
                sendMessageThroughServer(msg, getUserSocket(msg.from));
        }
    }


    public static void main(String[] args) throws UnknownHostException {
        new SecondServer();
    }


}
