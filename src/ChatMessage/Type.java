package ChatMessage;

import java.io.Serializable;

public enum Type implements Serializable {
    ADD, REMOVE, USER_EXISTS, APPROVED, USER_NOT_FOUND,
    LOCAL_MEMBERS, ALL_MEMBERS, MESSAGE, ERROR
}
