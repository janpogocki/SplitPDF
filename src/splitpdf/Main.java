package splitpdf;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("MainLayout.fxml"));
        primaryStage.setTitle("SplitPDF - podziel strony!");
        primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("pdf_icon.png")));

        Button buttonBrowse = (Button) root.lookup("#buttonBrowse");
        Button buttonDo = (Button) root.lookup("#buttonDo");
        Slider slider = (Slider) root.lookup("#slider");
        TextField sliderTextField = (TextField) root.lookup("#sliderTextField");
        Label labelBrowse = (Label) root.lookup("#labelBrowse");
        ImageView imageView = (ImageView) root.lookup("#imageView");
        ProgressBar progressBar = (ProgressBar) root.lookup("#progressBar");
        final File[] filePDF = {null};

        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            slider.setValue(newValue.intValue());
            sliderTextField.setText(String.valueOf(((int) slider.getValue())));
        });

        imageView.setOnMouseClicked(me -> {
            URI u = null;
            try {
                u = new URI("https://www.janpogocki.pl");
                try {
                    java.awt.Desktop.getDesktop().browse(u);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        });

        buttonBrowse.setOnAction((event -> {
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Pliki PDF", "*.pdf");

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Szukaj pliku PDF");
            fileChooser.getExtensionFilters().add(extFilter);

            filePDF[0] = fileChooser.showOpenDialog(primaryStage);

            if (filePDF[0] != null) {
                labelBrowse.setText(filePDF[0].getName());
                buttonDo.setDisable(false);
            }
        }));

        buttonDo.setOnAction((event -> {
            buttonBrowse.setDisable(true);
            buttonDo.setDisable(true);
            slider.setDisable(true);


            SplitPDF spdf = new SplitPDF(filePDF[0].getParent(), filePDF[0].getName());


            Task task = new Task<Void>() {
                @Override public Void call() {
                    while (spdf.progressStatus() <= 1) {
                        updateProgress(spdf.progressStatus(), 1);
                    }
                    Thread.currentThread().interrupt();
                    return null;
                }
            };

            progressBar.progressProperty().bind(task.progressProperty());
            new Thread(task).start();

            Task task2 = new Task<Void>() {
                @Override public Void call() {
                    progressBar.progressProperty().unbind();
                    progressBar.setProgress(0);
                    buttonBrowse.setDisable(false);
                    buttonDo.setDisable(false);
                    slider.setDisable(false);

                    return null;
                }
            };

            Task taskAlert = new Task<Void>() {
                @Override
                public Void call() {

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Informacja");
                            alert.setHeaderText(null);
                            alert.setContentText("Dokument PDF został podzielony poprawnie!");

                            alert.initOwner(primaryStage);

                            alert.showAndWait();
                        }
                    });
                    return null;
                }
            };

            Thread mainThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    spdf.execute(filePDF[0].getParent(), filePDF[0].getName(), slider.getValue()/100);
                    new Thread(task2).run();
                    new Thread(taskAlert).start();
                }
            });

            mainThread.start();

        }));

        primaryStage.setScene(new Scene(root, 500, 210));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
