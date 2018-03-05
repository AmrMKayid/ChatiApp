package Client;

import ChatMessage.ChatMassage;

public interface Listener {

    void sendMessage(ChatMassage msg);
    
}
