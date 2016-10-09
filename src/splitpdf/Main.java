package splitpdf;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("MainLayout.fxml"));
        primaryStage.setTitle("SplitPDF - podziel strony!");

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
            progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);

            // run AsyncTask
            AsyncTaskRunner runner = new AsyncTaskRunner(filePDF[0], slider.getValue()/100, progressBar);
            runner.execute();
        }));

        primaryStage.setScene(new Scene(root, 500, 210));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public class AsyncTaskRunner extends AsyncTask {
        private File file;
        private double percent;
        private SplitPDF spdf = null;
        private ProgressBar progressBar = null;

        public AsyncTaskRunner(File file, double percent, ProgressBar progressBar) {
            this.file = file;
            this.percent = percent;
            this.progressBar = progressBar;
        }

        @Override
        public void onPreExecute() {
            spdf = new SplitPDF(file.getParent(), file.getName());

            Task task = new Task() {
                @Override
                protected Object call() throws Exception {
                    while (spdf.progressStatus() != 1){
                        progressBar.setProgress(spdf.progressStatus());
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    return null;
                }
            };

            //task.run();
        }

        @Override
        public void doInBackground() {
            spdf.execute(file.getParent(), file.getName(), percent);
        }

        @Override
        public void onPostExecute() {
            System.out.println("ukonczono!!!");
        }

        @Override
        public void progressCallback(Object... params) {

        }

    }

}
