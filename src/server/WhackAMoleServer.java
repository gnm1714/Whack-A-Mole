package server;

import client.gui.WhackAMoleException;
import common.WAMProtocol;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * @author Gabe Megna <gnm1714@rit.edu>
 * @author Nick Piwko <nap2828@rit.edu>
 * The {@link WhackAMoleServer} waits for incoming client connections and
 * pairs them off to play {@link WhackAMoleGame games}.
 */

public class WhackAMoleServer implements WAMProtocol, Runnable {
    private ServerSocket server;
    private int row;
    private int col;
    private int numPlayers;
    private int time;
    private WhackAMolePlayer[] players;

    /**
     * Creates a new {@link WhackAMoleServer} that listens for incoming
     * connections on the specified port.
     * @param port The port on which the server should listen for incoming connections.
     * @throws WhackAMoleException If there is an error creating the {@link ServerSocket}
     */
    public WhackAMoleServer(int port, int row, int col, int numPlayers, int time) throws WhackAMoleException {
        try {
            server = new ServerSocket(port);
        } catch (IOException e) {
            throw new WhackAMoleException(e);
        }
        this.row = row;
        this.col = col;
        this.numPlayers = numPlayers;
        this.time = time;
        players = new WhackAMolePlayer[numPlayers];
    }

    /**
     * Starts a new {@link WhackAMoleServer}. Simply creates the server and
     * calls {@link #run()} in the main thread.
     * @param args Used to specify the port on which the server should listen for incoming client connections.
     * @throws WhackAMoleException If there is an error starting the server.
     */
    public static void main(String[] args) throws WhackAMoleException {
        if (args.length != 5) {
            System.out.println("Usage: java WhackAMoleServer <port>");
            System.exit(1);
        }
        int port = Integer.parseInt(args[0]);
        int row = Integer.parseInt(args[1]);
        int col = Integer.parseInt(args[2]);
        int numPlayers = Integer.parseInt(args[3]);
        int time = Integer.parseInt(args[4]);
        WhackAMoleServer server = new WhackAMoleServer(port, row, col, numPlayers, time);
        server.run();

    }

    public int winnerNum(){
        int highScore = 0;
        int winnerNumber = 0;
        for(int i = 0; i < numPlayers; i++){
            int[] temp = new int[numPlayers];
            temp[i] = players[i].getScore();
            if(temp[i] > highScore){
                highScore = temp[i];
                winnerNumber = i;
            }
        }
        return winnerNumber;
    }

    public void winner(){
        for(int i = 0; i < numPlayers; i++){
            if(players[i]==players[winnerNum()]){
                players[winnerNum()].gameWon();
            }else{
                players[i].gameLost();
            }
        }

    }

    /**
     * Waits for multiple clients to connect. Creates a {@link WhackAMolePlayer}
     * for each and then pairs them off in a {@link WhackAMoleGame}.<P>
     */
    @Override
    public void run() {
        try {
            for(int i = 0; i < numPlayers; i ++){
                System.out.println("Waiting for player...");
                Socket playerOneSocket = server.accept();
                WhackAMolePlayer playerOne =
                        new WhackAMolePlayer(playerOneSocket, i);
                players[i] = playerOne;
                playerOne.connect(row, col, numPlayers);
                System.out.println("Player connected!");
            }
            System.out.println("Starting game!");
            WhackAMoleGame game = new WhackAMoleGame(row, col, numPlayers, time, players);
            for(int j = 0; j < numPlayers; j++){
                players[j].getGame(game);
            }
            new Thread(game).start();
        } catch (IOException e) {
            System.err.println("Something has gone horribly wrong!");
            e.printStackTrace();
        } catch (WhackAMoleException e) {
            System.err.println("Failed to create players!");
            e.printStackTrace();
        }
        System.out.println(winnerNum());
    }
}
