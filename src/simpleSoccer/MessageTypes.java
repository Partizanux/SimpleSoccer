package simpleSoccer;

public enum MessageTypes {

    Msg_ReceiveBall,
    Msg_PassToMe,
    Msg_SupportAttacker,
    Msg_GoHome,
    Msg_Wait;

    MessageTypes() {
    }

    @Override
    public String toString() {
        return MessageToString(this);
    }

    public static String MessageToString(MessageTypes msg) {
        switch (msg) {
            case Msg_ReceiveBall:
                return "Msg_ReceiveBall";

            case Msg_PassToMe:
                return "Msg_PassToMe";

            case Msg_SupportAttacker:
                return "Msg_SupportAttacker";

            case Msg_GoHome:
                return "Msg_GoHome";

            case Msg_Wait:
                return "Msg_Wait";

            default:
                return "INVALID MESSAGE!!";
        }
    }
}