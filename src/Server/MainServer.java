package Server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;

public class MainServer {

    public static ServerSocket serverSocket;
    private static Socket clientSocket;
    private static ArrayList<ServerThread> currentServers;
    private static HashSet<String> currentUsers;

    public MainServer () {
        try {
            serverSocket = new ServerSocket(6000);
            System.out.println("Your Current Server IP Address: " + InetAddress.getLocalHost().getHostAddress());
            currentServers = new ArrayList<>();
            currentUsers = new HashSet<>();
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            try {
                clientSocket = serverSocket.accept();
                currentServers.add(new ServerThread(this,
                        clientSocket, currentServers, currentUsers));

                currentServers.get(currentServers.size() - 1).start();
                System.out.println("New server is connected.");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main (String[] args) {
        new MainServer();
    }

}
