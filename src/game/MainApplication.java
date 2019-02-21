package game;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.image.Image ;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import csse2002.block.world.*;
import java.io.File;
import java.util.*;

/**
 * This application holds all the front end code for the assignment in it
 * as well as logic and some added functionality to the GUI I have created
 */
public class MainApplication extends javafx.application.Application {

    /* Create Error Box for when an exception is encountered */
    private Alert errorAlert = new Alert(Alert.AlertType.ERROR);

    /* File Chooser to open files */
    FileChooser fileChooser = new FileChooser();

    /* The Button Width for the action buttons */
    public final int BUTTON_WIDTH = 90;

    /* The Button Height for the action buttons */
    public final int BUTTON_HEIGHT = 45;

    /* Sets the background colour to a hex colour */
    private Color BACKGROUND_COLOUR = Color.web("" +
            "#e2e2e2",1.0);

    /* The WorldMap to be used and displayed in the GUI */
    private WorldMap worldMap;

    /* File to save and load files from */
    private File worldFile = null;

    /* Map to Map Tiles to Positions for creating tiles on the main gridPane */
    Map<Tile,Position> tilePositionMap;

    /* ComboBox to put the Move Builder and Move Block actions into */
    private ComboBox moveComboBox;

    /* GridPane representing where the game is */
    GridPane gameBox;

    /* Circle representing the builder */
    Circle builderCircle;

    /* Value representing the center column */
    private int centerColumn = 4;

    /* Value representing the center row */
    private int centerRow = 4;

    /* Button to move north */
    private Button northButton;

    /* Button to move east */
    private Button eastButton;

    /* Button to move south */
    private Button southButton;

    /* Button to move west */
    private Button westButton;

    /* Button so the builder can dig on the top tile */
    private Button digButton;

    /* Button so a builder can drop blocks from their inventory */
    private Button dropButton;

    /* Text Field for a user to input the index they want to drop */
    private TextField dropIndex;

    /* Label to show the inventory of a builder */
    private javafx.scene.control.Label inventoryLabel;

    /**
     * Launches the stage
     * @param args - main input to start the program
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Starts the initialisation of the javafx GUI
     * @param stage the stage to display to the
     *              user when the program is initialised
     */
    public void start(Stage stage) {

        // Create Directions Buttons
        northButton = new Button("north");
        eastButton = new Button("east");
        southButton = new Button("south");
        westButton = new Button("west");
        digButton = new Button("DIG");
        dropButton = new Button("DROP");
        dropIndex = new TextField();
        dropIndex.setPromptText("Drop Index");
        dropIndex.setPrefColumnCount(2);

        // Set the Buttons to be a certain size
        northButton.setMinSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        eastButton.setMinSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        southButton.setMinSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        westButton.setMinSize(BUTTON_WIDTH, BUTTON_HEIGHT);

        // Create Label to display Builder name and Inventory
        javafx.scene.control.Label builderName = new javafx.scene.control.Label("Inventory:");
        inventoryLabel = new javafx.scene.control.Label("[]");

        // Create Combo Box
        moveComboBox = new ComboBox();
        moveComboBox.setMinWidth(BUTTON_WIDTH);
        moveComboBox.getItems().add("Move Builder");
        moveComboBox.getItems().add("Move Block");
        moveComboBox.getSelectionModel().selectFirst();

        // Create Menu Items
        MenuBar menuBar = new MenuBar();
        menuBar.setMaxWidth(500);
        Menu fileMenu = new Menu("File");
        MenuItem loadGameMenu = new MenuItem("Load Game World");
        MenuItem saveGameMenu = new MenuItem("Save World Map");

        // Add the MenuItems options to the main Menu bar
        fileMenu.getItems().addAll(loadGameMenu, saveGameMenu);
        menuBar.getMenus().addAll(fileMenu);

        // Create GridPanes
        GridPane mainPane = new GridPane();

        mainPane.setVgap(10);
        mainPane.setHgap(5);

        GridPane buttonPane = new GridPane();
        buttonPane.setVgap(10);
        buttonPane.setHgap(5);

        gameBox = new GridPane();

        // Populating the gameBox GridPane with Squares that create the main
        // grid to be used and interacted with via the direction buttons
        createGrid();

        // Adding buttons to buttonPane
        buttonPane.add(northButton, 1, 0);
        buttonPane.add(westButton, 0, 1);
        buttonPane.add(eastButton, 2, 1);
        buttonPane.add(southButton, 1, 2);
        buttonPane.add(digButton, 0, 4);
        buttonPane.add(moveComboBox, 1, 3);
        buttonPane.add(dropButton, 0, 5);
        buttonPane.add(dropIndex, 1, 5);

        // Changing the Alignment of all the buttons
        GridPane.setHalignment(northButton, HPos.CENTER);
        GridPane.setHalignment(southButton, HPos.CENTER);
        GridPane.setHalignment(dropButton, HPos.RIGHT);
        GridPane.setHalignment(digButton, HPos.RIGHT);

        // Setting all the buttons to being disabled
        northButton.setDisable(true);
        westButton.setDisable(true);
        eastButton.setDisable(true);
        southButton.setDisable(true);
        digButton.setDisable(true);
        moveComboBox.setDisable(true);
        dropButton.setDisable(true);
        dropIndex.setDisable(true);

        // Add Items to the Main GridPane
        mainPane.add(gameBox, 0, 1);
        mainPane.add(builderName, 0, 2);
        mainPane.add(inventoryLabel, 0, 3);
        mainPane.add(menuBar, 0, 0);
        mainPane.add(buttonPane, 1, 1);

        // Setting Scene
        Scene scene = new Scene(mainPane, 900, 600);
        stage.setScene(scene);
        stage.setTitle("not!(~(Minecraft))");
        stage.show();


        // Button handlers from here

        /**
         * Loads a file from a pop-up dialogue box.
         * Once the file is loaded it will add tiles
         * to the main gameBox (see moveTiles() & drawTile())
         */
        loadGameMenu.setOnAction(e -> {
            try {
                worldFile = fileChooser.showOpenDialog(stage);
                String fileName = worldFile.getAbsolutePath();
                worldMap = new WorldMap(fileName);

                // Resetting the column and row values
                centerColumn = 4;
                centerRow = 4;

                // Disabling the buttons
                northButton.setDisable(false);
                westButton.setDisable(false);
                eastButton.setDisable(false);
                southButton.setDisable(false);
                digButton.setDisable(false);
                moveComboBox.setDisable(false);
                dropButton.setDisable(false);
                dropIndex.setDisable(false);

                builderName.setText(
                        worldMap.getBuilder().getName() + "'s Inventory:");
                inventoryLabel.setText(blockListIterator(
                        worldMap.getBuilder().getInventory()));
                moveTiles();
            } catch (Exception exception) {
                if (exception instanceof NullPointerException) {
                    // Don't do anything if the dialog box is
                    // cancelled aka NullPointer Exception
                } else {
                    errorDisplay(exception.getMessage());
                }
            }
        });

        /**
         * Saves the current world map by calling worldMap.saveMap() before
         * saving the file in a fileChooser
         */
        saveGameMenu.setOnAction(e -> {
            try {
                worldFile = fileChooser.showSaveDialog(stage);
                String fileName = worldFile.getAbsolutePath();
                worldMap.saveMap(fileName);
            } catch (Exception exception) {
                if (exception instanceof NullPointerException) {
                    // Don't do anything if the dialog box is cancelled
                } else {
                   errorDisplay("There was some error saving the File");
                }
            }
        });

        /**
         * Sets the DIG button to dig on the current tile.
         * If an error is caught, an error window shows with the error being
         * displayed in the window
         */
        digButton.setOnAction(e -> {
            try {
                worldMap.getBuilder().digOnCurrentTile();
                inventoryLabel.setText(blockListIterator(
                        worldMap.getBuilder().getInventory()));
                System.out.println("Tile Dug on Current Tile");
                moveTiles();
            } catch(InvalidBlockException exception) {
                errorDisplay("Invalid Block");
            } catch(TooLowException exception) {
                errorDisplay("Too Low");
            }
        });

        /**
         * Sets the DROP button to drop from the inventory of builder
         * using the dropIndex textfield.
         * If an error is caught, an error window shows with the error being
         * displayed in the window
         */
        dropButton.setOnAction(e -> {
            try {
                int inventoryIndex = Integer.parseInt(dropIndex.getText());
                worldMap.getBuilder().dropFromInventory(inventoryIndex);
                inventoryLabel.setText(blockListIterator(
                        worldMap.getBuilder().getInventory()));
                System.out.println("Dropped a block from inventory");
                moveTiles();
            } catch(InvalidBlockException exception) {
                    errorDisplay("Invalid Block");
            } catch(TooHighException exception) {
                errorDisplay("Too High");
            } catch(NumberFormatException exception) {
                    errorDisplay("Please Enter a Valid Number");
            }
        });


        // Setting each direction button to do the button thing
        northButton.setOnAction(new DirectionButton());
        eastButton.setOnAction(new DirectionButton());
        westButton.setOnAction(new DirectionButton());
        southButton.setOnAction(new DirectionButton());
    }

    /**
     * A class that performs an action depending on the directional
     * button pressed in the buttonPane
     */
    private class DirectionButton implements EventHandler<ActionEvent> {

        /**
         * Handles the event when the button is pressed and calls
         * function blockOrBuilder() with the parameter determined by the
         * button being pressed.
         * @param event - event where the button is pressed
         */
        public void handle(ActionEvent event) {
            if (event.getSource() == northButton) {
                blockOrBuilder("north");
            } else if (event.getSource() == eastButton) {
                blockOrBuilder("east");
            } else if (event.getSource() == southButton) {
                blockOrBuilder("south");
            } else if (event.getSource() == westButton) {
                blockOrBuilder("west");
            }
        }
    }

    /**
     * Iterates through the blocks in the given list parameter
     * @param blockList - The list of blocks to iterate through
     * @return the list of blocks as a string
     */
    private String blockListIterator(List<Block> blockList) {
        List<String> listOfBlockNames = new ArrayList<>();
        for (int i = 0; i < blockList.size(); i++) {
            Block block = blockList.get(i);
            listOfBlockNames.add(block.getBlockType());
        }
        String inventory = listOfBlockNames.toString();
        return inventory;
    }

    /**
     * Makes the builder on the worldMap do a certain action
     * depending on what the comboBox is set to. It also increments the
     * center of the tiles if the comboBox value is Move Builder
     * so it moves according to the direction the builder moves.
     * @param direction - the direction to move the builder or block
     */
    private void blockOrBuilder(String direction) {
        if (moveComboBox.getValue().equals("Move Builder")) {
            try {
                Tile tileToMove = worldMap.getBuilder().
                        getCurrentTile().getExits().get(direction);
                worldMap.getBuilder().moveTo(tileToMove);
                System.out.println("Moved Builder " + direction);
                if(direction.equals("north")) {
                    centerColumn+=1;
                } else if(direction.equals("south")){
                    centerColumn-=1;
                } else if(direction.equals("east")) {
                    centerRow-=1;
                } else {
                    centerRow+=1;
                }
                moveTiles();
            } catch (NoExitException exception) {
                errorDisplay("No Exit This Way");
            }
        } else {
            try {
                worldMap.getBuilder().getCurrentTile().moveBlock(direction);
                System.out.println("Moved Block " + direction);
                moveTiles();
            } catch (TooHighException exception) {
                errorDisplay("Too High");
            } catch (InvalidBlockException exception) {
                errorDisplay("Invalid Block");
            } catch (NoExitException exception) {
                errorDisplay("No Exit This Way");
            }
        }
    }

    /**
     * Creates a popup alert with a custom message
     * @param errorMessage - The string to display in the error alert box
     */
    private void errorDisplay(String errorMessage) {
        errorAlert.setHeaderText("Error Encountered");
        errorAlert.setContentText(errorMessage);
        errorAlert.showAndWait();
    }

    /**
     * This function is called whenever the tiles are moved.
     * It does this by removing all the tiles, creating a new grid
     * then setting tilePositionMap as a new HashMap.
     * The function then iterates through each tile in worldMap.getTiles()
     * which then goes through each of the tile's exits which will then draw
     * a tile according to it's position. See drawTile() for more.
     */
    private void moveTiles() {
        gameBox.getChildren().removeAll(gameBox.getChildren());
        createGrid();
        tilePositionMap = new HashMap<>();
        tilePositionMap.put(worldMap.getTiles().get(0),
                new Position(centerRow,centerColumn));

        for(int i=0;i<worldMap.getTiles().size();i++) {
            Tile currentTile = worldMap.getTiles().get(i);
            int currentX = tilePositionMap.get(currentTile).getX();
            int currentY = tilePositionMap.get(currentTile).getY();

            if (currentTile.getExits().get("north") != null) {
                drawTile(currentTile,currentX,currentY,"north");
            }
            if (currentTile.getExits().get("east") != null) {
                drawTile(currentTile,currentX,currentY,"east");
            }
            if (currentTile.getExits().get("south") != null) {
                drawTile(currentTile,currentX,currentY,"south");
            }
            if (currentTile.getExits().get("west") != null) {
                drawTile(currentTile,currentX,currentY,"west");
            }
            drawBuilder();
        }
    }

    /**
     * Creates the grid by adding a bunch of squares across the gridPane so it
     * appears as if it is 9x9 while also acting as a background for gameBox
     */
    private void createGrid(){
        gameBox.setMaxSize(500,500);
        gameBox.setStyle("-fx-background-color: #e2e2e2; " +
                "-fx-grid-lines-visible: true");

        for(int i = 0;i<9;i++) {
            gameBox.add(new Rectangle(55.5,55.5,BACKGROUND_COLOUR),i,i);
        }
    }

    /**
     * Draws a circle in the center of the gridPane. This method is mainly
     * called so that the builder circle appears above the tile being drawn
     */
    public void drawBuilder() {
        builderCircle = new Circle(55.5/4,Color.BEIGE);
        builderCircle.setStroke(Color.BLACK);
        gameBox.add(builderCircle, 4, 4);
        gameBox.setHalignment(builderCircle, HPos.CENTER);
    }


    /**
     * Draws the current tile on the gridPane according to its position
     * @param currentTile - The Current Tile to draw into the gridPane
     * @param x - The x value (row on gridPane)
     * @param y - The y value (column on gridPane)
     * @param direction
     */
    public void drawTile(Tile currentTile, int x, int y, String direction) {
        try {
            Tile exitTile;
            switch (direction) {
                case "west":
                    exitTile = currentTile.getExits().get("west");
                    x -= 1;
                    break;
                case "east":
                    exitTile = currentTile.getExits().get("east");
                    x += 1;
                    break;
                case "north":
                    exitTile = currentTile.getExits().get("north");
                    y -= 1;
                    break;
                default:
                    exitTile = currentTile.getExits().get("south");
                    y += 1;
                    break;
            }
            // Puts the position into the tilePositionMap
            tilePositionMap.put(exitTile, new Position(x, y));

            // If the tile is out of bounds, don't draw it, if it is, draw it
            if((x>-1 && x <9)&&(y>-1 && y<9)) {

                // Adds a Rectangle to the gridPane(gameBox) at position x,y
                gameBox.add(new Rectangle(55.5, 55.5,
                        Color.web(exitTile.getTopBlock().getColour())), x, y);

                // Label to show the amount of blocks in a Tile
                javafx.scene.control.Label label =
                        new javafx.scene.control.Label(Integer.toString(
                                exitTile.getBlocks().size()));
                label.setTextFill(Color.web("#ffffff"));
                label.setMaxSize(10, 10);
                drawExits(exitTile);
                gameBox.add(label, x, y);
                gameBox.setHalignment(label, HPos.RIGHT);
                gameBox.setValignment(label, VPos.BOTTOM);
            }

        } catch(TooLowException exception) {
        }
    }

    /**
     * Draws Exits on an individual tile using PNG files
     * from the /image folder
     * @param tile - The Tile go draw the exits for
     */
    private void drawExits(Tile tile) {
        int x = tilePositionMap.get(tile).getX();
        int y = tilePositionMap.get(tile).getY();
        ImageView imageView;
        Image image;

        // Need for repetitive code as a Tile can
        // have multiple exits in it so the PNG files overlap.
        if (tile.getExits().get("north") != null) {
            image = new Image("File:src/images/north.png");
            imageView = new ImageView();
            imageView.setImage(image);
            gameBox.add(imageView,x,y);
        }
        if (tile.getExits().get("east") != null) {
            image = new Image("File:src/images/east.png");
            imageView = new ImageView();
            imageView.setImage(image);
            gameBox.add(imageView,x,y);
        }
        if (tile.getExits().get("south") != null) {
            image = new Image("File:src/images/south.png");
            imageView = new ImageView();
            imageView.setImage(image);
            gameBox.add(imageView,x,y);
        }
        if (tile.getExits().get("west") != null) {
            image = new Image("File:src/images/west.png");
            imageView = new ImageView();
            imageView.setImage(image);
            gameBox.add(imageView,x,y);
        }
    }
}