import java.io.*;
import java.net.*;


public class Client implements Runnable {

    private static Socket clientSocket = null;
    private static ObjectOutputStream os = null;
    private static ObjectInputStream is = null;
    private static BufferedReader inputLine = null;
    private static boolean closed = false;

    public static void main(String[] args) {

        int portNumber = 6000;

        String host = "localhost";

        try {

            clientSocket = new Socket(host, portNumber);
            inputLine = new BufferedReader(new InputStreamReader(System.in));
            os = new ObjectOutputStream(clientSocket.getOutputStream());
            is = new ObjectInputStream(clientSocket.getInputStream());

        } catch (UnknownHostException e) {
            System.err.println("Unknown " + host);
        } catch (IOException e) {
            System.err.println("No Server found. Please check if the Server is running and try again :D");
        }

        if (clientSocket != null && os != null && is != null) {

            try {
                new Thread(new Client()).start();

                while (!closed) {

                    String msg = (String) inputLine.readLine().trim();

                    os.writeObject(msg);
                    os.flush();
                }

                os.close();
                is.close();
                clientSocket.close();

            } catch (IOException e) {
                System.err.println("IOException:  " + e);
            }


        }
    }


    public void run() {

        String responseLine;
        try {
            while ((responseLine = (String) is.readObject()) != null)  {

                System.out.println(responseLine);

                if (responseLine.indexOf("Bye") != -1)
                    break;
            }

            closed = true;
            System.exit(0);

        } catch (IOException | ClassNotFoundException e) {

            System.err.println(e);

        }
    }

}
