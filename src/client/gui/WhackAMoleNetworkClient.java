package client.gui;

import client.gui.WhackAMoleException;
import common.WAMProtocol;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;
import static common.WAMProtocol.*;

/**
 * The client side network interface to a Whack-A-Mole game server.
 * Each of the players in a game gets its own connection to the server.
 * This class represents the controller part of a model-view-controller
 * triumvirate, in that part of its purpose is to forward user actions
 * to the remote server.
 *
 * @author Gabe Megna <gnm1714@rit.edu>
 * @author Nick Piwko <nap2828@rit.edu>
 */
public class WhackAMoleNetworkClient {
    /** Turn on if standard output debug messages are desired. */
    private static final boolean DEBUG = true;

    /**
     * Print method that does something only if DEBUG is true
     *
     * @param logMsg the message to log
     */
    private static void dPrint( Object logMsg ) {
        if ( WhackAMoleNetworkClient.DEBUG ) {
            System.out.println( logMsg );
        }
    }

    /** client socket to communicate with server */
    private Socket clientSocket;
    /** used to read requests from the server */
    private Scanner networkIn;
    /** Used to write responses to the server. */
    private PrintStream networkOut;
    /** the model which keeps track of the game */
    private WhackAMoleBoard board;
    /** sentinel loop used to control the main loop */
    private boolean go;
    /** number of rows */
    private int numRow;
    /** number of columns */
    private int numCol;
    /** the player that will connect to the server */
    private int playerId;
    /** the score of the player(based on their id) */
    private int score;


    /**
     * Accessor that takes multithreaded access into account
     *
     * @return whether it ok to continue or not
     */
    private synchronized boolean goodToGo() {
        return this.go;
    }

    /**
     * Multithread-safe mutator
     */
    private synchronized void stop() {
        this.go = false;
    }

    /**
     * Called when the server sends a message saying that
     * gameplay is damaged. Ends the game.
     *
     * @param arguments The error message sent from the reversi.server.
     */
    public void error( String arguments ) {
        WhackAMoleNetworkClient.dPrint( '!' + ERROR + ',' + arguments );
        dPrint( "Fatal error: " + arguments );
        this.board.error( arguments );
        this.stop();
    }

    /**
     * Hook up with a Whack-A-Mole game server already running and waiting for
     * players to connect. Because of the nature of the server
     * protocol, this constructor actually blocks waiting for the first
     * message (connect) from the server.  Afterwards a thread that listens for
     * server messages and forwards them to the game object is started.
     *
     * @param host  the name of the host running the server program
     * @param port  the port of the server socket on which the server is listening
     * @throws WhackAMoleException If there is a problem opening the connection
     */
    public WhackAMoleNetworkClient(String host, int port)
            throws WhackAMoleException {
        try {
            this.clientSocket = new Socket(host, port);
            this.networkIn = new Scanner(clientSocket.getInputStream());
            this.networkOut = new PrintStream(clientSocket.getOutputStream());
            this.go = true;
            String request = this.networkIn.next();
            String[] arguments = this.networkIn.nextLine().strip().split(" ");
            if (!request.equals(WAMProtocol.WELCOME )) {
                throw new WhackAMoleException("Expected WELCOME from server");
            }
            WhackAMoleNetworkClient.dPrint("Connected to server " + this.clientSocket);
            this.numRow = Integer.parseInt(arguments[0]);
            this.numCol = Integer.parseInt(arguments[1]);
            int timeLeft = Integer.parseInt(arguments[3]);
            this.playerId = Integer.parseInt(arguments[2]);
            this.board = new WhackAMoleBoard(numRow, numCol, timeLeft);
        }
        catch(IOException e) {
            throw new WhackAMoleException(e);
        }
    }

    /**
     * Called from the GUI when it is ready to start receiving messages
     * from the server.
     */
    public void startListener() {
        new Thread(() -> this.run()).start();
    }

    /**
     * @return the number of rows for outside use
     */
    public int getRow(){
        return numRow;
    }

    /**
     * @return the number of columns for outside use
     */
    public int getCol(){
        return numCol;
    }

    /**
     * tells that a mole was whacked
     * @param id the id of the mole whacked
     */
    public void whack(int id){
        networkOut.println(WHACK + " " + id + " " + playerId);
        networkOut.flush();
    }

    /**
     * Called when the server sends a message saying that the
     * board has been won by this player. Ends the game.
     */
    public void gameWon() {
        WhackAMoleNetworkClient.dPrint( '!' + GAME_WON );

        dPrint( "You won! Yay!" );
        this.board.gameWon();
        this.stop();
    }

    /**
     * Called when the server sends a message saying that the
     * game has been won by the other player. Ends the game.
     */
    public void gameLost() {
        WhackAMoleNetworkClient.dPrint( '!' + GAME_LOST );
        dPrint( "You lost! Boo!" );
        this.board.gameLost();
        this.stop();
    }

    /**
     * Called when the server sends a message saying that the
     * game is a tie. Ends the game.
     */
    public void gameTied() {
        WhackAMoleNetworkClient.dPrint( '!' + GAME_TIED );
        dPrint( "You tied! Meh!" );
        this.board.gameTied();
        this.stop();
    }

    /**
     * @param score the score of a player to be updated
     */
    public void setScore(int score){
        this.score = score;
    }

    public int getScore() {return score;}

    /**
     * This method should be called at the end of the game to
     * close the client connection.
     */
    public void close() {
        try {
            this.clientSocket.close();
        }
        catch( IOException ioe ) {
            // squash
        }
        this.board.close();
    }

    /**
     * @return the board for outside use
     */
    public WhackAMoleBoard getBoard() {
        return board;
    }

    /**
     * Run the main client loop. Intended to be started as a separate
     * thread internally. This method is made private so that no one
     * outside will call it or try to start a thread on it.
     */
    private void run() {
        while (this.goodToGo()) {
            try {
                String request = this.networkIn.next();
                System.out.println(request);
                String[] arguments = this.networkIn.nextLine().trim().split(" ");
                WhackAMoleNetworkClient.dPrint( "Net message in = \"" + request + '"' + arguments[0] );

                switch ( request ) {
                    case MOLE_UP:
                        board.setUp(Integer.parseInt(arguments[0]));
                        break;
                    case MOLE_DOWN:
                        board.setDown(Integer.parseInt(arguments[0]));
                        break;
                    case GAME_WON:
                        gameWon();
                        break;
                    case GAME_LOST:
                        gameLost();
                        break;
                    case GAME_TIED:
                        gameTied();
                        break;
                    case ERROR:
                        error( arguments[0] );
                        break;
                    case SCORE:
                        setScore(Integer.parseInt(arguments[0]));
                        break;
                    default:
                        System.err.println("Unrecognized request: " + request);
                        this.stop();
                        break;
                }
            }
            catch( NoSuchElementException nse ) {
                // Looks like the connection shut down.
                this.error( "Lost connection to server." );
                this.stop();
            }
            catch( Exception e ) {
                this.error( e.getMessage() + '?' );
                this.stop();
            }
        }
        this.close();
    }
}
