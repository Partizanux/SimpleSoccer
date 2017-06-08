package common.messaging;

import simpleSoccer.MessageTypes;
import simpleSoccer.agents.BaseGameEntity;
import static simpleSoccer.DEFINE.*;
import java.util.TreeSet;
import static common.game.EntityManager.EntityMgr;
import static common.misc.FrameCounter.TickCounter;
import static common.debug.DbgConsole.debug_con;

/**
 * A message dispatcher. Manages messages of the type Telegram.
 *         Instantiated as a singleton.
 */
public class MessageDispatcher {

    static {
        //uncomment below to send message info to the debug window
        //define(SHOW_MESSAGING_INFO);
    }
    //to make life easier...
    final public static MessageDispatcher Dispatcher = new MessageDispatcher();
    //to make code easier to read
    public static final double SEND_MSG_IMMEDIATELY = 0.0;
    public static final int NO_ADDITIONAL_INFO = 0;
    public static final int SENDER_ID_IRRELEVANT = -1;
    //a std::set is used as the container for the delayed messages
    //because of the benefit of automatic sorting and avoidance
    //of duplicates. Messages are sorted by their dispatch time.
    private TreeSet<Telegram> PriorityQ = new TreeSet<Telegram>();

    /** 
     * this method is utilized by DispatchMsg or DispatchDelayedMessages.
     * This method calls the message handling member function of the receiving
     * entity, pReceiver, with the newly created telegram
     */
    private void Discharge(BaseGameEntity pReceiver, Telegram telegram) {
        if (!pReceiver.handleMessage(telegram)) {
            //telegram could not be handled
            if (def(SHOW_MESSAGING_INFO)) {
                debug_con.print("Message not handled").print("");
            }
        }
    }

    private MessageDispatcher() {
    }

    //copy ctor and assignment should be private
    private MessageDispatcher(MessageDispatcher d) {
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Cloning not allowed");
    }

    //this class is a singleton
    public static MessageDispatcher Instance() {
        return Dispatcher;
    }

    /**
     * given a message, a receiver, a sender and any time delay, this function
     * routes the message to the correct agent (if no delay) or stores
     * in the message queue to be dispatched at the correct time
     */
    public void DispatchMsg(double delay,
            int sender,
            int receiver,
            MessageTypes msg) {
        DispatchMsg(delay, sender, receiver, msg, null);
    }

    public void DispatchMsg(double delay,
            int sender,
            int receiver,
            MessageTypes msg,
            Object AdditionalInfo) {

        //get a pointer to the receiver
        BaseGameEntity pReceiver = EntityMgr.GetEntityFromID(receiver);

        //make sure the receiver is valid
        if (pReceiver == null) {
            if (def(SHOW_MESSAGING_INFO)) {
                debug_con.print("\nWarning! No receiver with getId of ").print(receiver).print(" found").print("");
            }

            return;
        }

        //create the telegram
        Telegram telegram = new Telegram(0, sender, receiver, msg, AdditionalInfo);

        //if there is no delay, route telegram immediately                       
        if (delay <= 0.0) {
            if (def(SHOW_MESSAGING_INFO)) {
                debug_con.print("\nTelegram dispatched at time: ").print(TickCounter.GetCurrentFrame()).print(" by ").print(sender).print(" for ").print(receiver).print(". Msg is ").print(msg).print("");
            }
            //send the telegram to the recipient
            Discharge(pReceiver, telegram);
        } //else calculate the time when the telegram should be dispatched
        else {
            double CurrentTime = TickCounter.GetCurrentFrame();

            telegram.DispatchTime = CurrentTime + delay;

            //and put it in the queue
            PriorityQ.add(telegram);

            if (def(SHOW_MESSAGING_INFO)) {
                debug_con.print("\nDelayed telegram from ").print(sender).print(" recorded at time ").print(TickCounter.GetCurrentFrame()).print(" for ").print(receiver).print(". Msg is ").print(msg).print("");
            }
        }
    }

    /**
     *  This function dispatches any telegrams with a timestamp that has
     * expired. Any dispatched telegrams are removed from the queue
     */
    public void DispatchDelayedMessages() {
        //first get current time
        double CurrentTime = TickCounter.GetCurrentFrame();

        //now peek at the queue to see if any telegrams need dispatching.
        //remove all telegrams from the front of the queue that have gone
        //past their sell by date
        while (!PriorityQ.isEmpty()
                && (PriorityQ.first().DispatchTime < CurrentTime)
                && (PriorityQ.first().DispatchTime > 0)) {
            //read the telegram from the front of the queue
            final Telegram telegram = PriorityQ.first();

            //find the recipient
            BaseGameEntity pReceiver = EntityMgr.GetEntityFromID(telegram.Receiver);

            if (def(SHOW_MESSAGING_INFO)) {
                debug_con.print("\nQueued telegram ready for dispatch: Sent to ").print(pReceiver.getId()).print(". Msg is ").print(telegram.Msg).print("");
            }

            //send the telegram to the recipient
            Discharge(pReceiver, telegram);

            //remove it from the queue
            PriorityQ.remove(PriorityQ.first());
        }
    }
    /**
     * Count of messages in the queue.
     * @return 
     */
    public int size() {
        return PriorityQ.size();
    }

    /**
     * Clear dispatcher messages.
     */
    public void clear() {
        PriorityQ.clear();
    }
}