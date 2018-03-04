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
                if (msg == null || !msg.isAlive()) continue;
                switch (msg.type) {
                    case ADD: server.userExists((String) msg.data, server.SocketsIDs.get(clientSocket)); break;
                    case APPROVED: this.clientName = (String) msg.data; break;
                    case ALL_MEMBERS: server.getAllMembers(msg.from); break;
                    case LOCAL_MEMBERS: sendToUser.writeObject(new Message(Type.LOCAL_MEMBERS, getMembers())); break;
                    case REMOVE:
                        server.removeUser((String) msg.data);
                        readFromUser.close(); sendToUser.close(); break;
                    case MESSAGE:
                        if (msg.isAlive() && !isUserInSameServer(msg)) server.sendMessageToUser(msg);
                        else sendMessageToLocalUser(msg);
                        break;
                    default: break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String[] getMembers() {
        String Members[] = new String[currentClients.size()]; int i = 0;
        for (ClientThread ct : currentClients)
            if(ct != null && ct.clientName != null)
                Members[i++] = ct.clientName;
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

    private void sendMessageToLocalUser(Message msg) {
        String username = msg.to;
        synchronized (this){
            for (ClientThread ct : currentClients)
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
