package server;
import java.util.Random;

/**
 * creates the mole threads so the game can be run
 * @author Gabe Megna <gnm1714@rit.edu>
 * @author Nick Piwko <nap2828@rit.edu>
 */

public class WhackAMole_MoleThread implements Runnable{
    private int mole;
    private WhackAMoleGame game;
    Random rand = new Random();

    /**
     *
     * @param id the mole in use's id
     * @param game the game being run
     */
    public WhackAMole_MoleThread(int id, WhackAMoleGame game){
        this.mole = id;
        this.game = game;
    }

    /**
     * starts the mole threads so the game can be played
     */
    @Override
    public synchronized void run(){
        int minUp = 2;
        int minDown = 3;
        int maxUp = 5;
        int maxDown = 8;
        while(game.getCurrentTime() - game.getStartTime()  < game.getTime()*1000) {
            try {
                int upTime = rand.nextInt(maxUp - minUp) + minUp;
                int downTime = rand.nextInt(maxDown - minDown) + minDown;
                game.setDown(mole);
                wait(downTime * 1000);
                game.setUp(mole);
                wait(upTime * 1000);
            }
            catch (InterruptedException ie){
                game.setDown(mole);
            }
        }
    }
}
