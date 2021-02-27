import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.effect.Bloom;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.Glow;
import javafx.scene.effect.SepiaTone;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;

/**
 * @author Jack Shendrikov
 */

public class Main extends Application {

    private GraphicsContext g;
    private RadioButton rb1, rb2, rb3, rb4, rb5, rb6;
    private ColorPicker fillColor, strokeColor;
    private TextField xPosField, yPosField, zPosField, widthField, heightField, strokeField;
    private double imageOriginalWidth, imageOriginalHeight, fixedImageHeight;

    private final static SepiaTone sepiaEffect = new SepiaTone(0);
    private final static GaussianBlur gaussianEffect = new GaussianBlur(0);
    private final static Glow glowEffect = new Glow(0);
    private final static Bloom bloomEffect = new Bloom(1);

    private final static int CANVAS_WIDTH = 1520;
    private final static int CANVAS_HEIGHT = 1080;
    private double fixedImageWidth = 1200;

    private LinkedList<GraphicShape> shapeList = new LinkedList<>();

    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        // =================================================== MENU BAR ====================================================
        MenuBar menuBar = new MenuBar();
        SeparatorMenuItem divider = new SeparatorMenuItem();

        StackPane drawingArea = new StackPane();
        drawingArea.setStyle("-fx-background-color: #eee");

        // Create menus
        Menu fileMenu = new Menu("File");

        // Create FileMenu items
        MenuItem newCanvasItem = new MenuItem("New Canvas");
        MenuItem openImage = new MenuItem("Open Image");
        MenuItem saveImageItem = new MenuItem("Save Image");
        MenuItem exitItem = new MenuItem("Exit");


        // Add menu items to Menus
        fileMenu.getItems().addAll(newCanvasItem, openImage, saveImageItem, divider, exitItem);

        menuBar.getMenus().addAll(fileMenu);

        final BufferedImage[] bufferedImage = {null};
        try {
            URL url = new URL("https://i.imgur.com/3ZABkSo.jpg");
            bufferedImage[0] = ImageIO.read(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        final Image[] image = {SwingFXUtils.toFXImage(bufferedImage[0], null)};
        imageOriginalWidth = image[0].getWidth();
        imageOriginalHeight = image[0].getHeight();
        ImageView chosenImage = new ImageView();

        chosenImage.setImage(image[0]);
        chosenImage.preserveRatioProperty().set(true);
        chosenImage.setFitWidth(fixedImageWidth);
        fixedImageHeight = Utils.ComputeRatio(imageOriginalWidth, imageOriginalHeight, fixedImageWidth);
        chosenImage.setFitHeight(fixedImageHeight);

        // Functions
        newCanvasItem.setAccelerator(KeyCombination.keyCombination("Ctrl+N"));
        newCanvasItem.setOnAction(e -> {
            g.clearRect(0, 0, g.getCanvas().getWidth(), g.getCanvas().getHeight());
            shapeList.clear();
            xPosField.clear();
            yPosField.clear();
            zPosField.clear();
            widthField.clear();
            heightField.clear();
            strokeField.clear();
            strokeColor.setValue(Color.BLACK);
            fillColor.setValue(Color.TRANSPARENT);
            chosenImage.setImage(null);
        });

        // open image
        openImage.setAccelerator(KeyCombination.keyCombination("Ctrl+T"));
        openImage.setOnAction(e -> {
            chosenImage.setImage(null);
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("PNG", "*.png"),
                    new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                    new FileChooser.ExtensionFilter("GIF", "*.gif"),
                    new FileChooser.ExtensionFilter("BMP", "*.bmp"));

            //Show open file dialog
            File file = fileChooser.showOpenDialog(null);
            try {
                bufferedImage[0] = ImageIO.read(file);
                image[0] = SwingFXUtils.toFXImage(bufferedImage[0], null);
                imageOriginalWidth = image[0].getWidth();
                imageOriginalHeight = image[0].getHeight();

                chosenImage.setImage(image[0]);
                chosenImage.preserveRatioProperty().set(true);
                chosenImage.setFitWidth(fixedImageWidth);
                fixedImageHeight = Utils.ComputeRatio(imageOriginalWidth, imageOriginalHeight, fixedImageWidth);
                chosenImage.setFitHeight(fixedImageHeight);

            } catch (MalformedURLException ignored) {
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });

        // save image
        saveImageItem.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Result Image");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("PNG", "*.png"),
                new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                new FileChooser.ExtensionFilter("GIF", "*.gif"),
                new FileChooser.ExtensionFilter("BMP", "*.bmp"));
        saveImageItem.setOnAction(e -> {
            File chosenFilePath = fileChooser.showSaveDialog(new Stage());
            if (chosenFilePath != null) {
                String savedFileName = chosenFilePath.getName();
                String savedFileExtension = savedFileName.substring(savedFileName.indexOf(".") + 1, savedFileName.length());
                BufferedImage bImage = SwingFXUtils.fromFXImage(chosenImage.snapshot(null, null), null);
                try {
                    ImageIO.write(bImage, savedFileExtension, chosenFilePath);
                } catch (IOException ex) {
                    AlertBox.warning("Error", "Could not save", "Error. Could not save image to desired location.");
                }
            }
        });


        exitItem.setAccelerator(KeyCombination.keyCombination("Ctrl+Q"));
        exitItem.setOnAction(e -> {
            AlertBox.confirm("Exit Graphic Editor", "Exit Graphic Editor", "Are you sure you want to quit Graphic Editor?");
            System.exit(0);
        });

        // ================================================= END MENU BAR ==================================================
        // ==================================================== PANELS =====================================================
        GridPane shapeChooser = new GridPane();
        shapeChooser.setPadding(new Insets(30, 0, 0, 20));
        shapeChooser.setPrefWidth(180);

        VBox shapeButtons = new VBox();
        ToggleGroup shapesRadioButtons = new ToggleGroup();

        rb1 = new RadioButton("Rectangle");
        rb2 = new RadioButton("Oval");
        rb3 = new RadioButton("Line");
        rb4 = new RadioButton("Triangle");
        rb5 = new RadioButton("Circle");
        rb6 = new RadioButton("Ornament");
        rb1.setToggleGroup(shapesRadioButtons);
        rb2.setToggleGroup(shapesRadioButtons);
        rb3.setToggleGroup(shapesRadioButtons);
        rb4.setToggleGroup(shapesRadioButtons);
        rb5.setToggleGroup(shapesRadioButtons);
        rb6.setToggleGroup(shapesRadioButtons);

        Button cropButton = new Button("Crop Image");
        GridPane.setConstraints(cropButton, 0, 1);
        cropButton.setPrefWidth(150);

        shapeButtons.setSpacing(15);
        shapeButtons.getChildren().addAll(rb1, rb2, rb3, rb4, rb5, rb6, cropButton);
        GridPane.setConstraints(shapeButtons, 0, 0);

        VBox imageProperties = new VBox();
        imageProperties.setPadding(new Insets(30, 0, 0, 0));
        imageProperties.setSpacing(10);

        Slider opacityLevel = new Slider(0, 1, 1);
        Label opacityCaption = new Label("SetOpacity Level:");
        Label opacityValue = new Label(Double.toString(opacityLevel.getValue()));
        GridPane.setConstraints(opacityCaption, 0, 1);

        Slider sepiaTone = new Slider(0, 1, 0);
        Label sepiaCaption = new Label("Sepia Tone:");
        Label sepiaValue = new Label(Double.toString(sepiaTone.getValue()));

        Slider gaussianBlur = new Slider(0, 30, 0);
        Label gaussianCaption = new Label("Gaussian Blur:");
        Label gaussianValue = new Label(Double.toString(gaussianBlur.getValue()));

        Slider glowTone = new Slider(0, 1, 0);
        Label glowCaption = new Label("Glow Tone:");
        Label glowValue = new Label(Double.toString(glowTone.getValue()));

        Slider bloom = new Slider(0, 1, 1);
        Label bloomCaption = new Label("Bloom:");
        Label bloomValue = new Label(Double.toString(bloom.getValue()));

        Slider scaling = new Slider(0.3, 1, 0.75);
        Label scalingCaption = new Label("Scaling Factor:");
        Label scalingValue = new Label(Double.toString(scaling.getValue()));

        imageProperties.getChildren().addAll(opacityCaption, opacityValue, opacityLevel, sepiaCaption, sepiaValue, sepiaTone,
                gaussianCaption, gaussianValue, gaussianBlur, glowCaption, glowValue, glowTone, bloomCaption, bloomValue, bloom,
                scalingCaption, scalingValue, scaling);
        GridPane.setConstraints(imageProperties, 0, 1);


        shapeChooser.getChildren().addAll(shapeButtons, imageProperties);

        // Panel for shape properties ======================================================================================
        GridPane shapeProperties = new GridPane();
        shapeProperties.setPadding(new Insets(30, 20, 10, 20));
        shapeProperties.setVgap(20);
        shapeProperties.setPrefWidth(200);


        VBox posProperties = new VBox();
        posProperties.setSpacing(5);

        Label xPosFieldLabel = new Label("Enter X Position:");
        xPosField = new TextField();

        Label yPosFieldLabel = new Label("Enter Y Position:");
        yPosField = new TextField();

        Label zPosFieldLabel = new Label("Enter Z Position:");
        zPosField = new TextField();

        posProperties.getChildren().addAll(xPosFieldLabel, xPosField, yPosFieldLabel, yPosField, zPosFieldLabel, zPosField);
        GridPane.setConstraints(posProperties, 0, 0);

        VBox sizeProperties = new VBox();
        sizeProperties.setSpacing(5);

        Label widthFieldLabel = new Label("Enter Width:");
        widthField = new TextField();

        Label heightFieldLabel = new Label("Enter Height:");
        heightField = new TextField();

        Label strokeFieldLabel = new Label("Enter Stroke width:");
        strokeField = new TextField();

        sizeProperties.getChildren().addAll(widthFieldLabel, widthField, heightFieldLabel, heightField, strokeFieldLabel, strokeField);
        GridPane.setConstraints(sizeProperties, 0, 1);

        VBox colorProperties = new VBox();

        Label fillColorLabel = new Label("Choose fill color:");
        fillColor = new ColorPicker(Color.TRANSPARENT);
        fillColor.setPrefWidth(200);

        Label strokeColorLabel = new Label("Choose stroke color:");
        strokeColor = new ColorPicker(Color.BLACK);
        strokeColor.setPrefWidth(200);

        // Color picker functionality
        fillColor.setOnAction(e -> g.setFill(fillColor.getValue()));
        strokeColor.setOnAction(e -> g.setStroke(strokeColor.getValue()));

        colorProperties.setSpacing(5);
        colorProperties.getChildren().addAll(fillColorLabel, fillColor, strokeColorLabel, strokeColor);
        GridPane.setConstraints(colorProperties, 0, 2);

        // Button to draw the shape
        Button drawButton = new Button("Draw Shape");
        GridPane.setConstraints(drawButton, 0, 3);
        drawButton.setPrefWidth(200);

        // Button to print the shape list to console
        Button printListButton = new Button("Print List");
        GridPane.setConstraints(printListButton, 0, 4);
        printListButton.setPrefWidth(200);

        // effects
        glowEffect.setInput(bloomEffect);
        gaussianEffect.setInput(glowEffect);
        sepiaEffect.setInput(gaussianEffect);
        chosenImage.setEffect(sepiaEffect);
        drawingArea.getChildren().add(chosenImage);

        new SetOpacity(chosenImage, opacityLevel, opacityValue);

        sepiaTone.valueProperty().addListener((ov, old_val, new_val) -> {
            sepiaEffect.setLevel(new_val.doubleValue());
            sepiaValue.setText(String.format("%.2f", (double) new_val));
        });

        gaussianBlur.valueProperty().addListener((ov, old_val, new_val) -> {
            gaussianEffect.setRadius(new_val.doubleValue());
            gaussianValue.setText(String.format("%.2f", (double) new_val));
        });

        glowTone.valueProperty().addListener((ov, old_val, new_val) -> {
            glowEffect.setLevel(new_val.doubleValue());
            glowValue.setText(String.format("%.2f", (double) new_val));
        });

        bloom.valueProperty().addListener((ov, old_val, new_val) -> {
            bloomEffect.setThreshold(new_val.doubleValue());
            bloomValue.setText(String.format("%.2f", (double) new_val));
        });

        scaling.valueProperty().addListener((ov, old_val, new_val) -> {
            chosenImage.setScaleX(new_val.doubleValue());
            chosenImage.setScaleY(new_val.doubleValue());
            scalingValue.setText(String.format("%.2f", new_val));
        });

        shapesRadioButtons.selectedToggleProperty().addListener((ob, o, n) -> {
            // ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle
            if (rb1.isSelected() || rb2.isSelected() || rb3.isSelected() || rb4.isSelected() || rb5.isSelected()
                    || rb6.isSelected()) {
                shapeList.clear();
                xPosField.clear();
                yPosField.clear();
                zPosField.clear();
                widthField.clear();
                heightField.clear();
                strokeField.clear();
            }

            if (rb1.isSelected() || rb2.isSelected()) {
                shapeList.clear();
                xPosField.clear();
                yPosField.clear();
                zPosField.clear();
                widthField.clear();
                heightField.clear();
                strokeField.clear();
                zPosField.setText("0");
            } else if (rb3.isSelected()) {
                zPosField.setText("0");
                heightField.setText("0");
            } else if (rb4.isSelected()) {
                widthField.setText("0");
                heightField.setText("0");
            } else if (rb5.isSelected()) {
                zPosField.setText("0");
                heightField.setText("0");
            } else if (rb6.isSelected()) {
                xPosField.setText("0");
                yPosField.setText("0");
                zPosField.setText("0");
                widthField.setText("0");
                heightField.setText("0");
                strokeField.setText("0");
            }
        });

        drawButton.setOnAction(e -> {
            int x, y, z, w, h, stroke;
            try {
                x = Integer.parseInt(xPosField.getText());
                y = Integer.parseInt(yPosField.getText());
                z = Integer.parseInt(zPosField.getText());
                w = Integer.parseInt(widthField.getText());
                h = Integer.parseInt(heightField.getText());
                stroke = Integer.parseInt(strokeField.getText());

                if (x >= g.getCanvas().getWidth() || y >= g.getCanvas().getHeight() || w >= g.getCanvas().getWidth() || h >= g.getCanvas().getHeight()) {
                    AlertBox.warning("Too Big Properties", "Too Big Properties", "Please enter more smaller properties for the shape!");
                } else {
                    if (rb1.isSelected()) {
                        shapeList.add(new Rectangle(x, y, w, h, stroke, fillColor.getValue(), strokeColor.getValue()));
                    } else if (rb2.isSelected()) {
                        shapeList.add(new Oval(x, y, w, h, stroke, fillColor.getValue(), strokeColor.getValue()));
                    } else if (rb3.isSelected()) {
                        shapeList.add(new Line(x, y, w, stroke, strokeColor.getValue()));
                    } else if (rb4.isSelected()) {
                        shapeList.add(new Triangle(x, y, z, stroke, fillColor.getValue(), strokeColor.getValue()));
                    } else if (rb5.isSelected()) {
                        shapeList.add(new Circle(x, y, w, stroke, fillColor.getValue(), strokeColor.getValue()));
                    } else if (rb6.isSelected()) {
                        // AlertBox.warning("Warning", "Attention!", "This is an example, you will not be able to draw a figure in this field, you will need to create new canvas!");
                        shapeList.add(new Ornament());
                    }
                }
            } catch (Exception exc) {
                AlertBox.warning("Empty Fields", "Empty Fields", "To continue please fill Position and Size fields!");
            }

            drawShape(g);
        });

        printListButton.setOnAction(e -> {
            if (shapeList.size() == 0) {
                System.out.print("Empty list!");
            }
            for (GraphicShape aShapeList : shapeList) {
                System.out.println(aShapeList.getClass().toString());
            }
        });

        cropButton.setOnAction(e -> {
            int x, y, w, h;
            try {
                x = Integer.parseInt(xPosField.getText());
                y = Integer.parseInt(yPosField.getText());
                w = Integer.parseInt(widthField.getText());
                h = Integer.parseInt(heightField.getText());

                if (x >= g.getCanvas().getWidth() || y >= g.getCanvas().getHeight() || w >= g.getCanvas().getWidth() || h >= g.getCanvas().getHeight()) {
                    AlertBox.warning("Too Big Properties", "Too Big Properties", "Please enter more smaller properties for the shape!");
                } else {
                    PixelReader reader = image[0].getPixelReader();
                    WritableImage newImage = new WritableImage(reader, x, y, w, h);
                    chosenImage.setImage(null);
                    chosenImage.setImage(newImage);
                }
            } catch (Exception exc) {
                AlertBox.warning("Empty Fields", "Empty Fields", "To continue please fill Position and Size fields!");
            }
        });

        // Add all elements to panel
        shapeProperties.getChildren().addAll(posProperties, sizeProperties, colorProperties, drawButton, printListButton);

        // Area for drawing the shapes =====================================================================================
        Canvas area = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        g = area.getGraphicsContext2D();
        drawingArea.getChildren().add(area);

        // ================================================== END PANELS ===================================================
        // ================================================ Layout Settings ================================================
        root.setTop(menuBar);
        root.setLeft(shapeChooser);
        root.setRight(shapeProperties);
        root.setCenter(drawingArea);

        Scene scene = new Scene(root, 1920, 1080);
        scene.getStylesheets().addAll(this.getClass().getResource("Main.css").toExternalForm());
        primaryStage.getIcons().add(new Image("https://i.imgur.com/C4GX4OS.png"));
        primaryStage.setTitle("Image&Shape Editor");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    private void drawShape(GraphicsContext g) {
        for (GraphicShape aShapeList : shapeList) {
            aShapeList.drawShape(g);
        }
    }

    public static void main(String args[]) {
        launch(args);
    }
}
