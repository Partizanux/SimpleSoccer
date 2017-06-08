package common.fsm;

import common.messaging.Telegram;

public interface State<T> {
    void enter(T e);

    void execute(T e);

    void exit(T e);

    boolean onMessage(T e, Telegram t);
}
