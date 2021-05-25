package client.gui;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;

/**
 * The model for the whack a mole game.
 *
 * @author Gabe Megna <gnm1714@rit.edu>
 *          Nick Piwko <nap2828@rit.edu>
 */
public class WhackAMoleBoard {
    /**
     * the number of rows
     */
    public int rows;
    /**
     * the number of columns
     */
    public int cols;

    /**
     * Used to indicate a move that has been made on the board,
     * and to keep track of whose turn it is
     */
    public enum Move {
        UP, DOWN
    }

    /**
     * Possible statuses of game
     */
    public enum Status {
        NOT_OVER, I_WON, I_LOST, TIE, ERROR;

        private String message = null;

        public void setMessage(String msg) {
            this.message = msg;
        }

        @Override
        public String toString() {
            return super.toString() +
                    this.message == null ? "" : ('(' + this.message + ')');
        }
    }

    /**
     * How much time is left before end of game
     */
    private int timeLeft;

    /**
     * current game status
     */
    private Status status;

    /**
     * the board
     */
    private Move[][] board;

    /**
     * the observers of this model
     */
    private List<Observer<WhackAMoleBoard>> observers;

    /**
     * The view calls this method to add themselves as an observer of the model.
     *
     * @param observer the observer
     */
    public void addObserver(Observer<WhackAMoleBoard> observer) {
        this.observers.add(observer);
    }

    /**
     * when the model changes, the observers are notified via their update() method
     */
    private void alertObservers() {
        for (Observer<WhackAMoleBoard> obs : this.observers) {
            obs.update(this);
        }
    }

    public WhackAMoleBoard(int rows, int cols, int timeLeft) {
        this.rows = rows;
        this.cols = cols;
        this.timeLeft = timeLeft;
        this.observers = new LinkedList<>();
        this.status = Status.NOT_OVER;

        this.board = new Move[cols][rows];
        for (int col = 0; col < cols; col++) {
            for (int row = 0; row < rows; row++) {
                board[col][row] = Move.DOWN;
            }
        }

        //Not sure if this is right
        while (this.timeLeft != 0) {
            try
            {
                for (int i = 0; i < timeLeft; i++) {
                    Thread.sleep(1000);
                    this.timeLeft--;
                }
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt(); // restore interrupted status
            }
        }


    }

    /**
     * sets the mole up using its id
     * @param id mole's id
     */
    public void setUp(int id){
        board[id%cols][id/cols] = Move.UP;
        alertObservers();
    }

    /**
     * sets the mole down using its id
     * @param id which mole to set to down
     */
    public void setDown(int id){
        board[id%cols][id/cols] = Move.DOWN;
        alertObservers();
    }

    /**
     * used to throw errors using the WAM ERROR
     * @param arguments
     */
    public void error(String arguments) {
        this.status = Status.ERROR;
        this.status.setMessage(arguments);
        alertObservers();
    }

    /**
     * Information for the UI
     *
     * @return the amount of time until the game ends.
     */
    public int getTimeLeft() {
        return this.timeLeft;
    }

    /**
     * Get game status.
     *
     * @return the Status object for the game
     */
    public Status getStatus() {
        return this.status;
    }

    /**
     * What is at this square?
     *
     * @param row row number of square
     * @param col column number of square
     * @return the player at the given location
     */
    public Move getContents(int row, int col) {
        return this.board[col][row];
    }

    public boolean gameOn() {
        return this.status == Status.NOT_OVER;
    }

    /**
     * Called when the game has been won by this player.
     */
    public void gameWon() {
        this.status = Status.I_WON;
        alertObservers();
    }

    /**
     * Called when the game has been won by the other player.
     */
    public void gameLost() {
        this.status = Status.I_LOST;
        alertObservers();
    }

    /**
     * Called when the game has been tied.
     */
    public void gameTied() {
        this.status = Status.TIE;
        alertObservers();
    }

    /**
     * The user they may close at any time
     */
    public void close() {
        alertObservers();
    }
}
