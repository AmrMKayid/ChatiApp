package ChatMessage;

import java.io.Serializable;

public class Message implements Serializable {

    public String from, to;
    public Object data;
    public Type type;
    public int TTL;
    public int loginUser;

    public Message (String from, String to, Object data, Type type) {
        this.from = from;   this.to = to;
        this.data = data;
        this.type = type;
        this.TTL = 4;
    }

    public Message (Type type, Object data) {
        this.type = type;
        this.data = data;
        this.TTL = 4;
    }

    public boolean isAlive (){
        return TTL > 0;
    }

    public void decreaseTTL () {
        TTL--;
    }

}
