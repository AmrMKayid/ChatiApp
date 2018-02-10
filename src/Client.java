import java.io.*;
import java.net.*;

public class Client {

    public static void main(String argv[]) throws Exception
    {
        String sentence = "";
        String modifiedSentence;

        while(!sentence.equals("bye")) {
            BufferedReader inFromUser =
                    new BufferedReader(new InputStreamReader(System.in));
            Socket clientSocket = new Socket("127.0.0.1", 6000);
            DataOutputStream outToServer =
                    new DataOutputStream(clientSocket.getOutputStream());
            BufferedReader inFromServer =
                    new BufferedReader(new
                            InputStreamReader(clientSocket.getInputStream()));
            sentence = inFromUser.readLine();
            outToServer.writeBytes(sentence + '\n');
            modifiedSentence = inFromServer.readLine();
            System.out.println("FROM SERVER: " + modifiedSentence);
            clientSocket.close();
        }

    }
}
