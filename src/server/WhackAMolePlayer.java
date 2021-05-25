package server;

import client.gui.WhackAMoleException;
import common.WAMProtocol;
import java.io.Closeable;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * @author Gabe Megna <gnm1714@rit.edu>
 * @author Nick Piwko <nap2828@rit.edu>
 */

/**
 * A class that manages the requests and responses to a single client.
 */
public class WhackAMolePlayer implements WAMProtocol, Closeable, Runnable{
    /**
     * The {@link Socket} used to communicate with the client.
     */
    private Socket sock;

    /**
     * The {@link Scanner} used to read responses from the client.
     */
    private Scanner scanner;

    /**
     * The {@link PrintStream} used to send requests to the client.
     */
    private PrintStream printer;


    private WhackAMoleGame game;

    private int playerId;

    private int score;

    /**
     * Creates a new {@link WhackAMolePlayer} that will use the specified
     * {@link Socket} to communicate with the client.
     *
     * @param sock The {@link Socket} used to communicate with the client.
     *
     * @throws WhackAMoleException If there is a problem establishing
     * communication with the client.
     */
    public WhackAMolePlayer(Socket sock, int id) throws WhackAMoleException {
        this.sock = sock;
        this.playerId = id;
        try {
            scanner = new Scanner(sock.getInputStream());
            printer = new PrintStream(sock.getOutputStream());
        }
        catch (IOException e) {
            throw new WhackAMoleException(e);
        }
    }

    /**
     * Sends the initial {@link #WELCOME} request to the client.
     */
    public void connect(int row, int col, int NumPlayers) {
        printer.println(WELCOME + " " + row + " " + col + " " + NumPlayers + " " + playerId);
    }


    public void setScore(int amount){
        score = score + amount;
    }


    @Override
    public void run(){
        while (true) {
            String response = scanner.next();
            if(response.startsWith(WHACK)) {
                String[] tokens = response.strip().split(" "); // moleid playerid
                if (tokens.length == 3) {
                    if(game.getIsUp(Integer.parseInt(tokens[1]))==true){
                        game.setDown(Integer.parseInt(tokens[1]));
                        setScore(2);
                    }
                    else{
                        setScore(-1);
                    }
                    sendScore();
                }
                else {
                    System.out.println("not enough information");
                }
            }
            else {
                System.out.println("Error");
            }
        }
    }
    /**
     * Called to send a {@link #GAME_WON} request to the client because the
     * player's most recent move won the game.
     *
     */
    public void gameWon() {
        printer.println(GAME_WON);

    }

    /**
     * Called to send a {@link #GAME_LOST} request to the client because the
     * other player's most recent move won the game.
     *
     */
    public void gameLost()  {
        printer.println(GAME_LOST);
    }

    /**
     * Called to send a {@link #GAME_TIED} request to the client because the
     * game tied.
     */
    public void gameTied()  {
        printer.println(GAME_TIED);
    }

    /**
     * Called to send an {@link #ERROR} to the client. This is called if either
     * client has invalidated themselves with a bad response.
     *
     * @param message The error message.
     */
    public void error(String message) {
        printer.println(ERROR + " " + message);
    }

    /**
     * Called to close the client connection after the game is over.
     */
    @Override
    public void close() {
        try {
            sock.close();
        }
        catch(IOException ioe) {
            // squash
        }
    }

    public void sendScore(){
        printer.println(WAMProtocol.SCORE + " " + score);
    }

    public int getScore() {return score;}

    public void getGame(WhackAMoleGame WAMGame){
        this.game = WAMGame;
    }

    public void moleUp(int id){
        printer.println(WAMProtocol.MOLE_UP + " " + id);
    }

    public void moleDown(int id){
        printer.println(WAMProtocol.MOLE_DOWN + " " + id);
    }
}
