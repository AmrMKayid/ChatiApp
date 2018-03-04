package Client;

import ChatMessage.Message;

public interface Listener {

    void sendMessage(Message msg);
    
}
