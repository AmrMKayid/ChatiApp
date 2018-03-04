package Client;

import ChatMessage.Message;
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
                Message msg = (Message) data;
                switch (msg.type) {
                    case APPROVED:
                        String username = (String) (msg.data);
                        listener.sendMessage(new Message(Type.APPROVED, username));
                        sendToServer.writeObject(new Message(Type.APPROVED, username));
                        break;
                    case USER_EXISTS:
                        listener.sendMessage(new Message(Type.USER_EXISTS, msg.data));
                        break;
                    case LOCAL_MEMBERS:
                        listener.sendMessage(new Message(Type.LOCAL_MEMBERS, msg.data));
                        break;
                    case ALL_MEMBERS:
                        listener.sendMessage(new Message(Type.ALL_MEMBERS, msg.data));
                        break;
                    case MESSAGE:
                        listener.sendMessage(msg);
                        break;
                    case ERROR:
                        listener.sendMessage(msg);
                        break;
                    case USER_NOT_FOUND:
                        listener.sendMessage(msg);
                        break;
                    default:
                        break;
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

    void AddUserToServer(String username) {
        Message msg = new Message(Type.ADD, username);
        try {
            sendToServer.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeUserFromServer(String username) {
        Message msg = new Message(Type.REMOVE, username);
        try {
            sendToServer.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getLocalMembers() {
        Message msg = new Message(Type.LOCAL_MEMBERS, null);
        try {
            sendToServer.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getAllMembers(String username) {
        Message msg = new Message(Type.ALL_MEMBERS, null);
        msg.from = username;
        try {
            sendToServer.flush();
            sendToServer.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(Message msg) {
        try {
            sendToServer.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        ClientGUI.start();
    }

}
