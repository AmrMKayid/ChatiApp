package Client;

import ChatMessage.Message;
import ChatMessage.Type;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;

public class ClientGUI implements Serializable, Listener {

    private JPanel defaultPanel;
    private JPanel CurrentUsers;

    private JList MembersList;
    private JList messagesList;

    private JButton AllMembers;
    private JButton LocalMembers;
    private JButton Send;
    private JButton logout;

    private JTextArea Messages;

    private JScrollPane ChatArea;

    private static DefaultListModel listModel;
    private static ClientGUI gui;
    private static JFrame frame;

    private static Client currentClient;
    private static String username;

    public ClientGUI () {

        CurrentUsers.setBorder(new TitledBorder("Online Members"));

        listModel = new DefaultListModel();

        listModel.addElement("ChatiApp");
        messagesList.setModel(listModel);

        Send.addActionListener(e -> {

            String words[] = Messages.getText().split("\\s", 2);

            if (words.length > 1 && words[1] != null) {
                String to = words[0];
                String content = words[1];
                content = content.trim();
                to = to.substring(1).trim();
                if (content.length() > 0) {
                    Message msg = new Message(username, to, content, Type.MESSAGE);
                    listModel.addElement("To " + msg.to + " : " + msg.data);
                    messagesList.setModel(listModel);
                    messagesList.repaint();
                    messagesList.revalidate();
                    Messages.setText("");

                    currentClient.sendMessage(msg);
                } else {
                    listModel.addElement("Please enter a valid non-empty message!");
                    messagesList.setModel(listModel);
                    Messages.setText("");
                    messagesList.repaint();
                    messagesList.revalidate();
                }
            } else {
                listModel.addElement("Please start the message with @username msg.");
                messagesList.setModel(listModel);
                Messages.setText("");
                messagesList.repaint();
                messagesList.revalidate();
            }

        });

        AllMembers.addActionListener(e -> currentClient.getAllMembers(username));

        LocalMembers.addActionListener(e -> currentClient.getLocalMembers());

        logout.addActionListener(e -> {
            currentClient.removeUserFromServer(username);
            System.exit(0);
        });

        MembersList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JList list = (JList) e.getSource();
                if (e.getClickCount() == 2) {
                    int index = list.locationToIndex(e.getPoint());
                    Messages.setText("@" + list.getModel().getElementAt(index) + " ");
                }
            }
        });

    }

    public static void start() {
        init();
        askForServerIP();
        askAndValidateUsername();
    }

    public static void init() {
        frame = new JFrame("GUI");
        frame.setTitle("ChatiApp");
        gui = new ClientGUI();
        frame.setContentPane(gui.defaultPanel);
        frame.setSize(new Dimension(800, 600));
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private static void askForServerIP() {
        String serverIPPort = (String) JOptionPane.showInputDialog(
                frame,
                "Please Enter the Server IP and Port:\n",
                "Server IP",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                "localhost, 6000");

        String[] ip_port = serverIPPort.split(",");
        currentClient = new Client(ip_port[0], Integer.parseInt(ip_port[1].trim()));
        currentClient.listener = gui;
        currentClient.connectToServer();
    }

    private static void askAndValidateUsername() {
        String username = "";
        username = (String) JOptionPane.showInputDialog(
                frame,
                "Please Enter Your Username:\n"
                ,
                "Login",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                "Untitled");
        currentClient.AddUserToServer(username);
//        changeWelcomeMessage(username);
        gui.username = username;
    }

    private void updateMembersList(String[] data) {
        DefaultListModel model = new DefaultListModel();
        for (String x : data)
            model.addElement(x);
        MembersList.setModel(model);
        messagesList.revalidate();
        messagesList.repaint();
    }

    @Override
    public void sendMessage(Message msg) {
        switch (msg.type) {
            case APPROVED:
                frame.setVisible(true);
                currentClient.getLocalMembers();
                break;
            case ADD:
                break;
            case REMOVE:
                break;
            case USER_EXISTS:
                askAndValidateUsername();
                break;
            case LOCAL_MEMBERS:
                updateMembersList((String[]) msg.data);
                break;
            case ALL_MEMBERS:
                updateMembersList((String[]) msg.data);
                break;
            case MESSAGE:
                listModel.addElement("From " + msg.from + " : " + msg.data);
                messagesList.setModel(listModel);
                messagesList.revalidate();
                messagesList.repaint();
                break;
            case ERROR:
                listModel.addElement("Message wasn't sent due to an error with the server.");
                messagesList.setModel(listModel);
                messagesList.revalidate();
                messagesList.repaint();
                break;
            case USER_NOT_FOUND:
                listModel.addElement("Username not found, please enter a valid name or refresh the member list.");
                messagesList.setModel(listModel);
                messagesList.revalidate();
                messagesList.repaint();
                break;
            default:
                break;

        }
    }

}
