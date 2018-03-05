package Client;

import ChatMessage.ChatMassage;
import ChatMessage.Type;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client implements Runnable {

    private int port;
    private String serverIP;

    public static Socket socket;

    private static ObjectOutputStream sendToServer;
    private static ObjectInputStream InFromServer;

    public Listener listener;

    public Client (String serverIP, int port) {
        this.serverIP = serverIP;
        this.port = port;
    }

    @Override
    public void run() {
        Object data;
        try {
            while (((data = InFromServer.readObject()) != null)) {
                ChatMassage msg = (ChatMassage) data;
                switch (msg.type) {
                    case APPROVED:
                        String usr = (String) (msg.data);
                        listener.sendMessage(new ChatMassage(Type.APPROVED, usr));
                        sendToServer.writeObject(new ChatMassage(Type.APPROVED, usr));
                        break;
                    case USER_EXISTS: listener.sendMessage(new ChatMassage(Type.USER_EXISTS, msg.data)); break;
                    case ALL_MEMBERS: listener.sendMessage(new ChatMassage(Type.ALL_MEMBERS, msg.data)); break;
                    case LOCAL_MEMBERS: listener.sendMessage(new ChatMassage(Type.LOCAL_MEMBERS, msg.data)); break;
                    case MESSAGE: listener.sendMessage(msg); break;
                    case ERROR: listener.sendMessage(msg); break;
                    case USER_NOT_FOUND: listener.sendMessage(msg); break;
                    default: break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void connectToServer() {

        try {
            socket = new Socket(serverIP, port);
            sendToServer = new ObjectOutputStream(socket.getOutputStream());
            InFromServer = new ObjectInputStream(socket.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (socket != null &&
                InFromServer != null &&
                sendToServer != null) {
            new Thread(this).start();
        }
    }

    public void AddUserToServer(String username) {
        try {
            sendToServer.writeObject(new ChatMassage(Type.ADD, username));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeUserFromServer(String username) {
        try {
            sendToServer.writeObject(new ChatMassage(Type.REMOVE, username));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getLocalMembers() {
        try {
            sendToServer.writeObject(new ChatMassage(Type.LOCAL_MEMBERS, null));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getAllMembers(String username) {
        ChatMassage requestAll = new ChatMassage(Type.ALL_MEMBERS, null);
        requestAll.from = username;
        try {
            sendToServer.writeObject(requestAll);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(ChatMassage msg) {
        try {
            sendToServer.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ObjectOutputStream getSendToServer() {
        return sendToServer;
    }

    public static ObjectInputStream getInFromServer() {
        return InFromServer;
    }

    public static void main(String[] args) {
        ClientGUI.start();
    }

}
