package Server;

import ChatMessage.ChatMassage;
import ChatMessage.Type;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;

public class ServerThread extends Thread {

    private MainServer server;
    private ArrayList<ServerThread> currentServers;
    private ObjectOutputStream ToServer;
    private ObjectInputStream FromServer;
    private static HashSet<String> currentUsers;
    private Socket connectedServer;

    public ServerThread (MainServer server, Socket connectedServer,
                         ArrayList<ServerThread> currentServers, HashSet<String> currentUsers) {

        this.server = server;
        this.connectedServer = connectedServer;
        this.currentServers = currentServers;
        this.currentUsers = currentUsers;

    }

    @Override
    public void run () {

        try {
            ToServer = new ObjectOutputStream(connectedServer.getOutputStream());
            FromServer = new ObjectInputStream(connectedServer.getInputStream());

            Object data;
            while (((data = FromServer.readObject()) != null)) {
                ChatMassage msg = (ChatMassage) data;
                if (msg == null || !msg.isAlive()) {
                    ToServer.writeObject(new ChatMassage(Type.ERROR, null));
                }
                switch (msg.type) {
                    case ADD:
                        String username = (String) msg.data;
                        ChatMassage loginChatMassage;
                        if (addNewUser(username)) {
                            loginChatMassage = new ChatMassage(Type.APPROVED, username);
                        } else {
                            loginChatMassage = new ChatMassage(Type.USER_EXISTS, username);
                        }
                        loginChatMassage.loginUser = msg.loginUser;
                        ToServer.writeObject(loginChatMassage);
                        break;
                    case REMOVE:
                        username = (String) msg.data;
                        removeUser(username);
                        break;
                    case ALL_MEMBERS:
                        String[] allMembers = AllMembers();
                        ChatMassage allMembersMsg = new ChatMassage(Type.ALL_MEMBERS, allMembers);
                        allMembersMsg.from = msg.from;
                        ToServer.writeObject(allMembersMsg);
                        break;
                    case MESSAGE:
                        if (UserExists(msg)) {
                            send2User(msg);
                        } else {
                            msg.type = Type.USER_NOT_FOUND;
                            ToServer.writeObject(msg);
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


    private boolean addNewUser(String username) {
        for (String user : currentUsers)
            if (user.equals(username))
                return false;
        currentUsers.add(username);
        return true;
    }

    private boolean UserExists(ChatMassage msg) {
        for (String user : currentUsers)
            if (msg.to.equals(user))
                return true;
        return false;
    }

    private void removeUser(String username) {
        currentUsers.remove(username);
    }

    private void send2User(ChatMassage msg) {
        synchronized (this) {
            for (ServerThread st : currentServers) {
                try {
                    msg.decreaseTTL();
                    st.ToServer.writeObject(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String[] AllMembers() {
        String[] allMembers = new String[currentUsers.size()];
        int i = 0;
        for (String x : currentUsers) {
            allMembers[i++] = x;
        }
        return allMembers;
    }

}
