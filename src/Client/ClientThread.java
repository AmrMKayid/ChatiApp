package Client;

import ChatMessage.Message;
import ChatMessage.Type;
import Server.SecondServer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class ClientThread extends Thread {

    public String clientName;
    private ArrayList<ClientThread> currentClients;

    public ObjectOutputStream sendToUser;
    public ObjectInputStream readFromUser;

    private SecondServer server;
    public Socket clientSocket;

    public ClientThread (SecondServer server, Socket clientSocket,
                        ArrayList<ClientThread> currentClients) {
        this.server = server;
        this.clientSocket = clientSocket;
        this.currentClients = currentClients;
    }

    @Override
    public void run() {
        try {
            sendToUser = new ObjectOutputStream(clientSocket.getOutputStream());
            readFromUser = new ObjectInputStream(clientSocket.getInputStream());
            Object data;
            while (((data = readFromUser.readObject()) != null)) {
                Message msg = (Message) data;
                if (msg == null || !msg.isAlive()) {
                    continue;
                }
                switch (msg.type) {
                    case ADD:
                        String username = (String) msg.data;
                        server.checkUserExists(username, server.SocketsIDs.get(clientSocket));
                        break;
                    case REMOVE:
                        username = (String) msg.data;
                        server.removeUserFromServer(username);
                        readFromUser.close();
                        sendToUser.close();
                        break;
                    case APPROVED:
                        username = (String) msg.data;
                        this.clientName = username;
                        break;
                    case LOCAL_MEMBERS:
                        sendToUser.writeObject(new Message(Type.LOCAL_MEMBERS, getMembers()));
                        break;
                    case ALL_MEMBERS:
                        server.getAllMembers(msg.from);
                        break;
                    case MESSAGE:
                        if (msg.isAlive()) {
                            if (!isUserInSameServer(msg)) {
                                server.sendMessageToUser(msg);
                            } else {
                                sendMessageToLocalClient(msg);
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String[] getMembers() {
        String Members[] = new String[currentClients.size()];
        for (int i = 0; i < currentClients.size(); i++) {
            if (currentClients.get(i) != null && currentClients.get(i).clientName != null)
                Members[i] = currentClients.get(i).clientName;
        }
        return Members;
    }

    public boolean isUserInSameServer(Message msg) {
        if (msg == null) return false;
        String username = msg.to;
        for (String user : getMembers())
            if (user.equals(username))
                return true;
        return false;
    }

    private void sendMessageToLocalClient(Message msg) {
        String username = msg.to;
        synchronized (this){
            for (ClientThread ct : currentClients) {
                if (ct.clientName.equals(username)) {
                    try {
                        ct.sendToUser.writeObject(msg);
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

}
