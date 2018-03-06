package Client;

import ChatMessage.ChatMassage;
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
    private JLabel Welcome;

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

            String words[] = Messages.getText().split(": ", 2);

            if (words.length > 1 && words[1] != null) {
                String to = words[0].substring(1).trim();
                String newMsg = words[1].trim();
                if (newMsg.length() > 0) {
                    ChatMassage msg = new ChatMassage(username, to, newMsg, Type.MESSAGE);
                    listModel.addElement("To " + msg.to + " : " + msg.data);
                    messagesList.setModel(listModel);
                    messagesList.repaint(); messagesList.revalidate();
                    Messages.setText("");
                    currentClient.sendMessage(msg);
                } else {
                    listModel.addElement("Enter a valid message!");
                    messagesList.setModel(listModel);
                    Messages.setText("");
                    messagesList.repaint(); messagesList.revalidate();
                }
            } else {
                listModel.addElement("messages start with @username");
                messagesList.setModel(listModel);
                Messages.setText(""); messagesList.repaint(); messagesList.revalidate();
            }

        });

        AllMembers.addActionListener(e -> currentClient.getAllMembers(username));

        LocalMembers.addActionListener(e -> currentClient.getLocalMembers());

        logout.addActionListener(e -> {
            currentClient.removeUserFromServer(username);
//            try {
//                currentClient.getSendToServer().close();
//                currentClient.getInFromServer().close();
//            } catch (IOException e1) {
//                e1.printStackTrace();
//            }

            System.exit(0);
        });

        MembersList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JList l = (JList) e.getSource();
                if (e.getClickCount() == 2) {
                    int index = l.locationToIndex(e.getPoint());
                    Messages.setText("@" + l.getModel().getElementAt(index) + ": ");
                }
            }
        });

    }

    public static void start() {
        init();
        getServerIP();
        getUserName();
    }

    public static void init() {
        frame = new JFrame("GUI");
        frame.setTitle("ChatiApp");
        frame.setSize(new Dimension(600, 600));
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gui = new ClientGUI();
        frame.setContentPane(gui.defaultPanel);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> currentClient.removeUserFromServer(username)));

    }

    private static void getServerIP() {
        String serverIPAndPort = (String) JOptionPane.showInputDialog(frame, "the Server IP and Port:\n", "Server IP & Port Number",
                JOptionPane.PLAIN_MESSAGE, null, null, "localhost, 6000");

        String[] s = serverIPAndPort.split(",");
        currentClient = new Client(s[0], Integer.parseInt(s[1].trim()));
        currentClient.listener = gui;
        currentClient.connectToServer();
    }

    private static void getUserName() {
        String username = "";
        username = (String) JOptionPane.showInputDialog(frame, "Username: \n",
                "Login", JOptionPane.PLAIN_MESSAGE, null, null, "");
        gui.username = username;
        currentClient.AddUserToServer(username);
        WelcomeMessage(username);
    }

    private static void WelcomeMessage(String username) {
        gui.Welcome.setText(" *** Welcome, " + username + "! ***");
        gui.Welcome.updateUI();
    }

    private void updateMembers(String[] data) {
        DefaultListModel defaultModel = new DefaultListModel();
        for (String m : data)
            defaultModel.addElement(m);
        MembersList.setModel(defaultModel);
        messagesList.revalidate(); messagesList.repaint();
    }

    @Override
    public void sendMessage(ChatMassage msg) {
        switch (msg.type) {
            case APPROVED: frame.setVisible(true); currentClient.getLocalMembers(); break;
            case ADD: break;
            case REMOVE: break;
            case USER_EXISTS: getUserName(); break;
            case LOCAL_MEMBERS: updateMembers((String[]) msg.data); break;
            case ALL_MEMBERS: updateMembers((String[]) msg.data); break;
            case MESSAGE:
                listModel.addElement("From " + msg.from + " : " + msg.data);
                messagesList.setModel(listModel); messagesList.revalidate(); messagesList.repaint();
                break;
            case ERROR:
                listModel.addElement("ChatMassage wasn't sent... Server Error!");
                messagesList.setModel(listModel); messagesList.revalidate(); messagesList.repaint();
                break;
            case USER_NOT_FOUND:
                listModel.addElement("Username not found!");
                messagesList.setModel(listModel); messagesList.revalidate(); messagesList.repaint();
                break;
            default: break;
        }
    }

}
