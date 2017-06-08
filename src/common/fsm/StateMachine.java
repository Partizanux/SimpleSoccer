package common.fsm;

import common.messaging.Telegram;

public class StateMachine<T> {
    private T owner;

    private State<T> currentState;
    private State<T> previousState;
    private State<T> globalState;

    public StateMachine(T owner) {
        this.owner = owner;
    }

    public void SetCurrentState(State<T> s) {
        currentState = s;
    }

    public void SetGlobalState(State<T> s) {
        globalState = s;
    }

    public void SetPreviousState(State<T> s) {
        previousState = s;
    }

    public void Update() {
        if (globalState != null) {
            globalState.execute(owner);
        }

        if (currentState != null) {
            currentState.execute(owner);
        }
    }

    public boolean handleMessage (Telegram msg) {
        if (currentState != null && currentState.onMessage(owner, msg)) {
            return true;
        }

        if (globalState != null && globalState.onMessage(owner, msg)) {
            return true;
        }

        return false;
    }

    public void ChangeState(State<T> newState) {
        assert newState != null : "<StateMachine::ChangeState>: trying to change to NULL state";

        previousState = currentState;
        currentState.exit(owner);
        currentState = newState;
        currentState.enter(owner);
    }

    public void RevertToPreviousState() {
        ChangeState(previousState);
    }

    //returns true if the current state's type is equal to the type of the
    //class passed as a parameter. 
    public boolean inState(State<T> s) {
        return currentState.getClass() == s.getClass();
    }

    public State<T> currentState() {
        return currentState;
    }

    public State<T> globalState() {
        return globalState;
    }

    public State<T> previousState() {
        return previousState;
    }

    public String getNameOfCurrentState() {
        String [] s = currentState.getClass().getName().split("\\.");
        if(s.length > 0) {
            return s[s.length-1];
        }
        return currentState.getClass().getName();
    }
}