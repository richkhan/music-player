package sample;

import com.sun.javafx.scene.shape.RectangleHelper;
import com.sun.scenario.effect.impl.prism.PrImage;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.stage.WindowEvent;


import java.net.MalformedURLException;
import java.time.Duration;

public class Main extends Application {

    private MediaPlayer mediaPlayer;
    private Point2D anchorPt;
    private Point2D preLoc;
    private ChangeListener<Duration> progListener;

    private Stage PRIMARY_STAGE;
    private static final String STOP_BUTTON = "stop-button";
    private static final String PLAY_BUTTON = "play-button";
    private static final String PAUSE_BUTTON = "pause-button";
    private static final String CLOSE_BUTTON = "close-button";
    private static final String VIS_CONTAINER = "viz-container";
    private static  final String SEEK_POS_SLIDER = "seek-position-slider";

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        PRIMARY_STAGE = primaryStage;
        PRIMARY_STAGE.initStyle(StageStyle.TRANSPARENT);
        PRIMARY_STAGE.centerOnScreen();

        Group grp = new Group();
        Scene scene = new Scene (grp, 551, 270, Color.rgb(0,0,0,0));

        // load JavaFX CSS Style
        scene.getStylesheets().add(getClass().getResource("play-audio.css").toExternalForm());
        PRIMARY_STAGE.setScene(scene);

        // Initialize stage to be movable via mouse
        initMovabalePlayer();


    }

    private void initMovabalePlayer () {
        Scene scene = PRIMARY_STAGE.getScene();

        scene.setOnMousePressed(mouseEvent -> anchorPt = new Point2D(mouseEvent.getSceneX(),mouseEvent.getSceneY())

        );

        // drag the entire stage
        scene.setOnMouseDragged(mouseEvent -> {
            if(anchorPt != null && preLoc != null) {
                PRIMARY_STAGE.setX(preLoc.getX()+mouseEvent.getSceneX()-anchorPt.getX());

                PRIMARY_STAGE.setY(preLoc.getY()+mouseEvent.getSceneY()-anchorPt.getY());
            }
        });

        // set the current location
        scene.setOnMouseReleased(mouseEvent ->
                preLoc = new Point2D(PRIMARY_STAGE.getX(), PRIMARY_STAGE.getY())
        );

        // Initialize previous locations after sTAGE IS SHOWN
        PRIMARY_STAGE.addEventHandler(WindowEvent.WINDOW_SHOWN,(WindowEvent w) -> {
            preLoc = new Point2D(PRIMARY_STAGE.getX(), PRIMARY_STAGE.getY());
        });
    }

    private Node createApplicationArea() {
        Scene scene = PRIMARY_STAGE.getScene();
        Rectangle applicationArea = new Rectangle();

        //add selector to style app-area
        applicationArea.setId("app-area");

        // make the app area rectangle the size of the scene.
        applicationArea.widthProperty().bind(scene.widthProperty());
        applicationArea.heightProperty().bind(scene.heightProperty());

        return applicationArea;
    }

    private void initFileDragNDrop() {

        Scene scene = PRIMARY_STAGE.getScene();
        scene.setOnDragOver(dragEvent -> {
            Dragboard db = dragEvent.getDragboard();
            if (db.hasFiles() || db.hasUrl()) {
                dragEvent.acceptTransferModes(TransferMode.LINK);
            } else {
                dragEvent.consume();
            }
        });

        //Dropping over surface
        scene.setOnDragDropped(dragEvent -> {
            Dragboard db = dragEvent.getDragboard();
            boolean success = false;
            String filePath = null;
            if (db.hasFiles()) {
                success = true;
                if (db.getFiles().size() > 0) {
                    try {
                        filePath = db.getFiles().get(0).toURI().toURL().toString();
                        playMedia(filePath);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                // audio file from some host or jar
                playMedia(db.getUrl());
                success = true;
            }
            dragEvent.setDropCompleted(success);
            dragEvent.consume();
        });

    }

    private Node createButtonPanel () {
        Scene scene = PRIMARY_STAGE.setScene();;
        //create button control panel
        Group btnGroup = new Group();

        // Button area
        Rectangle buttonArea = new Rectangle(60, 30);
        buttonArea.setId("button-area");

        btnGroup.getChildren().add(buttonArea);

        // stop button control
        Node stopbutton = new Rectangle(10, 10);
        stopbutton.setId(STOP_BUTTON);
        stopbutton.setOnMousePressed(mouseEvent -> {
            if (mediaPlayer !=null) {
                updatePlayAndPauseButtons(true);
                if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                    mediaPlayer.stop();
                }
            }
        });

        // play button
        Arc playButton = new Arc(12,    // center X
                16,     // center Y
                15,     // radius x
                15,     // radius Y
                150,    // start angle
                60);    // length

        playButton.setId(PLAY_BUTTON);
        playButton.setType(ArcType.ROUND);
        playButton.setOnMousePressed(mouseEvent -> mediaPlayer.play());

        // pause button
        Group pauseBtn = new Group();
        pauseBtn.setId(PAUSE_BUTTON);
        Node pauseBackground = new Circle(12, 16,10);
        pauseBackground.getStyleClass().add("pause-circle");

        Node firstLine = new Line(6,    // start x
                6,      // start y
                6,      // end x
                14);    // end y
        firstLine.getStyleClass().add("pause-line");
        firstLine.setStyle("-fx-translate-x: 34;");

        Node secondLine = new Line (6, 6, 6, 14);
        secondLine.getStyleClass().add("pause-line");
        secondLine.setStyle("-fx-translate-x:38;");

        pauseBtn.getChildren().addAll(pauseBackground, firstLine, secondLine);

        pauseBtn.setOnMousePressed(mouseEvent -> {
            if (mediaPlayer != null) {
                updatePlayAndPauseButtons(false);
                mediaPlayer.play();
            }
        });

        btnGroup.getChildren().addAll(stopbutton, playButton,pauseBtn);

        // move button group when scene is resized
        btnGroup.translateXProperty().bind(scene.widthProperty().subtract(buttonArea.getWidth() + 6));
        btnGroup.translateYProperty().bind(scene.heightProperty().subtract(buttonArea.getHeight() + 6));

        return btnGroup;


    }


    public static void main(String[] args) {
        launch(args);
    }
}
