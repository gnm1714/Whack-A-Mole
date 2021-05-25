package server;

import client.gui.WhackAMoleException;
import java.util.ArrayList;

/**
 * @author Gabe Megna <gnm1714@rit.edu>
 * @author Nick Piwko <nap2828@rit.edu>
 */

public class WhackAMoleGame implements Runnable {
    private WhackAMolePlayer[] players;
    //private WhackAMole game;
    private int row;
    private int col;
    private int numPlayers;
    private int time;
    private WhackAMole_MoleThread[] mole_moleThreads;
    private boolean isUp;
    private Thread[] MoleThread;
    private Thread[] PlayerThread;
    private long startTime;
    private Move[][] board;

    /**
     * Initialize the game.
     */
    public WhackAMoleGame(int row, int col, int numPlayers, int time, WhackAMolePlayer[] players) {
        this.players = players;
        //game = new WhackAMole();
        this.row = row;
        this.col = col;
        this.numPlayers = numPlayers;
        this.time = time;
        board = new Move[row][col];
        for (int r = 0; r < row; r++){
            for (int c = 0; c < col; c++){
                board[r][c] = Move.DOWN;
            }
        }
    }

    /**
     * sets the enum so UP and DOWN can be accessed
     */
    public enum Move {
        UP, DOWN
    }

    /**
     *
     * @param id
     * @return
     */
    public boolean getIsUp(int id){
        return board[id/col][id%col] == Move.UP;
    }

    /**
     * sets the mole up for all the players
     * @param id the mole being put up
     */
    public void setUp(int id) {
        board[id/col][id%col]=Move.UP;
        for(int i = 0; i<numPlayers; i++){
            players[i].moleUp(id);
        }
    }

    /**
     * sets the mole down for all players
     * @param id the id of the mole being put down
     */
    public void setDown(int id){
        board[id/col][id%col]=Move.DOWN;
        for(int i = 0; i<numPlayers; i++){
            players[i].moleDown(id);
        }
    }

    /**
     * @return the row being used
     */
    public int getRow(){
        return row;
    }

    /**
     * @return the column being used
     */
    public int getCol(){
        return col;
    }

    /**
     * @return the number of players
     */
    public int getNumPlayers(){
        return numPlayers;
    }

    /**
     * @return the time of the game
     */
    public int getTime(){
        return time;
    }

    /**
     * @return the time the game was started
     */
    public long getStartTime(){
        return startTime;
    }

    /**
     * @return the current time of the computer
     */
    public long getCurrentTime(){
        return System.currentTimeMillis();
    }

    /**
     * runs the threads, players and moles, so that the game can be played
     */
    @Override
    public void run() {
        MoleThread = new Thread[row*col];
        PlayerThread = new Thread[numPlayers];
        for (int i = 0; i < numPlayers; i++){
            Thread thread = new Thread(players[i]);
            PlayerThread[i] = thread;
            thread.start();
        }
        startTime = System.currentTimeMillis();
        mole_moleThreads = new WhackAMole_MoleThread[row*col];
        for (int i = 0; i < row*col; i++){
            WhackAMole_MoleThread mole = new WhackAMole_MoleThread(i, this);
            Thread thread = new Thread(mole);
            MoleThread[i] = thread;
            thread.start();
        }
        while(getCurrentTime() - startTime  < time*1000){ }
        for (Thread m: MoleThread){
            try {
                m.interrupt();
                m.join();
            }
            catch (Exception e){
            }
        }
        for (Thread t: PlayerThread){
            try {
                t.join();
            }
            catch (Exception e){
            }
        }
    }
}