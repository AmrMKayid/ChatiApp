package ChatMessage;

import java.io.Serializable;

public class ChatMassage implements Serializable {

    public String from, to;
    public Object data;
    public Type type;
    public int TTL = 4;
    public int loginUser;

    public ChatMassage(String from, String to, Object data, Type type) {
        this.from = from;   this.to = to;
        this.data = data;   this.type = type;
    }

    public ChatMassage(Type type, Object data) {
        this.type = type;   this.data = data;
    }

    public boolean isAlive (){
        return TTL > 0;
    }

    public void decreaseTTL () {
        TTL--;
    }

}
