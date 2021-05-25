package client.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import server.WhackAMolePlayer;

import java.util.List;

/**
 * @author Gabe Megna <gnm1714@rit.edu>
 * @author Nick Piwko <nap2828@rit.edu>
 */

public class WhackAMoleGUI extends Application implements Observer<WhackAMoleBoard> {
    /** the model */
    private WhackAMoleBoard board;
    /** connection to network interface to server */
    private WhackAMoleNetworkClient serverConn;
    /** The win/lose message*/
    private Text message;
    /** number of rows */
    private int rows;
    /** number of columns */
    private int cols;
    /** The score of the current player*/
    private int score;
    /** The score label*/
    private Label scoring;
    /** sets the grid pane to use */
    private GridPane gridPane = new GridPane();
    /** sets up the border pane to use*/
    private BorderPane borderPane = new BorderPane();
    /** creates the buttons so the user can press the mole down */
    private Button[][] buttons;
    private String statuss;
    private Label status;

    /**
     * sets the initial conditions of the game
     */
    @Override
    public synchronized void init() {
        try {
            // get the command line args
            List<String> args = getParameters().getRaw();

            // get host info and port from command line
            String host = args.get(0);
            int port = Integer.parseInt(args.get(1));

            // create the network connection
            this.serverConn = new WhackAMoleNetworkClient(host, port);

            this.board = serverConn.getBoard();

            this.board.addObserver(this);

            this.rows = serverConn.getRow();
            this.cols = serverConn.getCol();

            this.score = serverConn.getScore();

            this.scoring = new Label(Integer.toString(this.score));

            this.buttons = new Button[cols][rows];

            this.message = new Text();

        } catch ( WhackAMoleException | ArrayIndexOutOfBoundsException | NumberFormatException e ) {
            System.err.println(e);
            throw new RuntimeException(e);
        }
    }

    /**
     * sets the stage  and scene so that the GUI works and displays the image
     * @param stage the stage to be set
     * @throws Exception
     */
    @Override
    public synchronized void start(Stage stage) throws Exception {
        try {
            for (int row = 0; row < this.rows; row++) {
                for (int col = 0; col < this.cols; col++) {
                    Image initImage = new Image("file:moledown.png");
                    Button initButton = new Button();
                    initButton.setGraphic(new ImageView(initImage));
                    gridPane.add(initButton, col, row);
                    buttons[col][row] = initButton;
                    int id = row*cols + col;
                    initButton.setOnAction(actionEvent -> serverConn.whack(id));
                }
            }

            scoring.setAlignment(Pos.CENTER);
            borderPane.setCenter(gridPane);
            borderPane.setTop(scoring);

            Scene scene = new Scene(borderPane);
            stage.setTitle("Whack-A-Mole");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            throw e;
        }

        this.serverConn.startListener();
    }

    /**
     * GUI updates.
     */
    private synchronized void refresh() {
        for (int row = 0; row < this.rows; row++) {
            for (int col = 0; col < this.cols; col++) {
                if (board.getContents(row, col) == WhackAMoleBoard.Move.UP) {
                    buttons[col][row].setGraphic(new ImageView(new Image("file:moleup.png")));
                    this.score = serverConn.getScore();
                    this.scoring = new Label(Integer.toString(serverConn.getScore()));
            } else if (board.getContents(row, col) == WhackAMoleBoard.Move.DOWN) {
                    buttons[col][row].setGraphic(new ImageView(new Image("file:moledown.png")));
                    this.score = serverConn.getScore();
                    this.scoring = new Label(Integer.toString(serverConn.getScore()));
                }
            }
        }
        this.score = serverConn.getScore();
        this.scoring = new Label(Integer.toString(serverConn.getScore()));
        this.borderPane.setTop(this.scoring);
        this.statuss = board.getStatus().toString();
        this.status = new Label(statuss);
        this.borderPane.setBottom(this.status);
    }


    /**
     * Called by the model, client.WhackAMoleBoard, whenever there is a state change
     * that needs to be updated by the GUI.
     *
     * @param whackAMoleBoard
     */
    public synchronized void update(WhackAMoleBoard whackAMoleBoard) {
        if ( Platform.isFxApplicationThread() ) {
            this.refresh();
        }
        else {
            Platform.runLater( () -> this.refresh() );
        }
    }

    /**
     *
     * @param args
     */
    public static synchronized void main(String args[]) {
        if (args.length != 2) {
            System.out.println("Usage: java WhackAMoleGUI host port");
            System.exit(-1);
        } else {
            Application.launch(args);
        }
    }
}
