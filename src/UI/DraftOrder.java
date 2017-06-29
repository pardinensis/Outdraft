package UI;

public class DraftOrder {
    public enum State {
        PREPARE,
        PICK_ALLY,
        PICK_ENEMY,
        BAN_ALLY,
        BAN_ENEMY,
        FINISHED
    }

    private enum InternalState {
        PREPARE,
        PICK_1,
        PICK_2,
        BAN_1,
        BAN_2,
        FINISHED
    }

    private final InternalState[] ALL_PICK = {
            InternalState.PICK_1,
            InternalState.PICK_2,
            InternalState.PICK_1,
            InternalState.PICK_2,
            InternalState.PICK_1,
            InternalState.PICK_2,
            InternalState.PICK_1,
            InternalState.PICK_2,
            InternalState.PICK_1,
            InternalState.PICK_2,
            InternalState.FINISHED,
    };

    private final InternalState[] CAPTAINS_MODE = {
            InternalState.BAN_1,
            InternalState.BAN_2,
            InternalState.BAN_1,
            InternalState.BAN_2,
            InternalState.PICK_1,
            InternalState.PICK_2,
            InternalState.PICK_2,
            InternalState.PICK_1,
            InternalState.BAN_2,
            InternalState.BAN_1,
            InternalState.BAN_2,
            InternalState.BAN_1,
            InternalState.PICK_2,
            InternalState.PICK_1,
            InternalState.PICK_2,
            InternalState.PICK_1,
            InternalState.BAN_2,
            InternalState.BAN_1,
            InternalState.PICK_1,
            InternalState.PICK_2,
            InternalState.FINISHED,
    };

    private InternalState[] order;
    private int stateCounter;
    private boolean allyFirstPick;

    public DraftOrder(String gameMode, boolean allyFirstPick) {
        setGameMode(gameMode);
        this.allyFirstPick = allyFirstPick;
        reset();
    }

    private State toPublicState(InternalState internalState) {
        switch (internalState) {
            case PICK_1:
                return allyFirstPick ? State.PICK_ALLY : State.PICK_ENEMY;
            case PICK_2:
                return allyFirstPick ? State.PICK_ENEMY : State.PICK_ALLY;
            case BAN_1:
                return allyFirstPick ? State.BAN_ALLY : State.BAN_ENEMY;
            case BAN_2:
                return allyFirstPick ? State.BAN_ENEMY : State.BAN_ALLY;
            case PREPARE:
                return State.PREPARE;
            case FINISHED:
                return State.FINISHED;
        }
        throw new RuntimeException("Illegal State " + internalState.toString());
    }

    public State getCurrentState() {
        return toPublicState(order[stateCounter]);
    }

    public State proceed() {
        if (stateCounter < order.length - 1) {
            ++stateCounter;
        }
        return toPublicState(order[stateCounter]);
    }

    public State backtrack() { return toPublicState(order[--stateCounter]); }

    public void reset() {
        stateCounter = 0;
    }

    public void setGameMode(String gameMode) {
        switch (gameMode) {
            case "All Pick":
                order = ALL_PICK;
                break;
            case "Captains Mode":
                order = CAPTAINS_MODE;
                break;
            default:
                order = new InternalState[]{ InternalState.PREPARE };
        }
    }
}
