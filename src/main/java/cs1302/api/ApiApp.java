
package cs1302.api;



import cs1302.api.HarvardResponse;
import cs1302.api.HarvardResults;
import javafx.scene.layout.HBox;
import java.util.ArrayList;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.http.HttpClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpRequest;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import java.util.Scanner;
import java.nio.charset.StandardCharsets;
import java.lang.Math;
import javafx.geometry.Pos;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;


/**
 * Application displays artifact titles from the harvard art gallery api by prompting the user for
 * two years to look between and querying the response to an api which provides screenshots of each
 * artifact webpage that was recieved.
 */
public class ApiApp extends Application {

    public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();

    public static Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .create();

    private static final String HARVARD_API = "https://api.harvardartmuseums.org/exhibition";
    private static final String HARVARD_API_KEY = "b3589162-81fd-42c1-9737-6c55e13b12ed";
    private static final String SCREENSHOT_API = "https://pro.microlink.io/";
    private Stage stage;
    private Scene scene;
    private VBox root;
    private HBox topBox;
    private VBox primaryVBox;
    private Text text1;
    private Text text2;
    private Text text3;
    private Text text4;
    private Button load;
    private TextField start;
    private TextField end;
    private Image picture;
    private ImageView pictureView;
    private HBox bottomBox;
    private ArrayList<String> urlList;
    private int pageNumber;
    private Button next;
    private Button back;
    private VBox container;
    private Alert a;
    /**
     * Constructs an {@code ApiApp} object. This default (i.e., no argument)
     * constructor is executed in Step 2 of the JavaFX Application Life-Cycle.
     */

    public ApiApp() {
        topBox = new HBox(10);
        primaryVBox = new VBox(10);
        text1 = new Text("Enter lower bound year:");
        text2 = new Text("Enter upper bound year:");
        text3 = new Text();
        String p1 = "Enter year in upper and lower bound to recieve artifacts";
        String p2 = " from the harvard art museum dated from the time.";
        text4 = new Text(p1 + p2);
        load = new Button("load");
        start = new TextField();
        end = new TextField();
        picture = new Image("https://harvardartmuseums.org/assets/icons/fb-og-image-400x400.png");
        pictureView = new ImageView(picture);
        bottomBox = new HBox(8);
        urlList = new ArrayList<String>();
        next = new Button("next");
        back = new Button("back");
        pageNumber = 0;
        container = new VBox(10);
        a = new Alert(AlertType.NONE);
    } // ApiApp

    /** {@inheritDoc} */
    public void init() {
        System.out.println("init called");
        primaryVBox.getChildren().addAll(topBox,container,text3,bottomBox);
        topBox.getChildren().addAll(text1,start,text2,end,load,back,next);
        container.getChildren().add(pictureView);
        pictureView.setPreserveRatio(true);
        pictureView.setFitWidth(700);
        bottomBox.getChildren().addAll(text4);
        this.topBox.setHgrow(this.text1,Priority.ALWAYS);
        this.topBox.setHgrow(this.start,Priority.ALWAYS);
        this.topBox.setHgrow(this.text2,Priority.ALWAYS);
        this.topBox.setHgrow(this.end, Priority.ALWAYS);
        this.primaryVBox.setVgrow(this.text3,Priority.ALWAYS);
        this.bottomBox.setHgrow(this.text4,Priority.ALWAYS);
        this.topBox.setAlignment(Pos.TOP_CENTER);
        this.container.setAlignment(Pos.CENTER);
        primaryVBox.setPrefHeight(800);
        container.setPrefHeight(500);

        EventHandler<ActionEvent> dataLoader = (ActionEvent e) -> {
            if (start.getText() == "" || end.getText() == "") {
                a.setAlertType(AlertType.WARNING);
                a.show();
            } else if (Integer.parseInt(start.getText()) > Integer.parseInt(end.getText())) {
                a.setAlertType(AlertType.WARNING);
                a.show();
            } else {
                this.getData();
            }
        };
        EventHandler<ActionEvent> getNext = (ActionEvent e) -> {
            pageNumber++;
            this.showData();
        };
        EventHandler<ActionEvent> getPrevious = (ActionEvent e) -> {
            pageNumber--;
            this.showData();
        };
        load.setOnAction(dataLoader);
        next.setOnAction(getNext);
        back.setOnAction(getPrevious);
    }

    /** {@inheritDoc} */
    @Override
    public void start(Stage stage) {

        this.stage = stage;
        scene = new Scene(primaryVBox);
        stage.setTitle("ArtMuseumApp");
        stage.setScene(scene);
        stage.sizeToScene();
        stage.setOnCloseRequest(event -> Platform.exit());
        stage.sizeToScene();
        stage.show();
        stage.setResizable(false);
    } // start

    /**
     * Gets list of artifacts from Harvard Art Gallery api and parses them into a list of
     * urls to the webpages.
     */

    public void getData() {
        try {
            load.setDisable(true);
            back.setDisable(true);
            next.setDisable(true);
            pageNumber = 0;
            text4.setText("loading your results");
            String startYear = URLEncoder.encode(start.getText(),StandardCharsets.UTF_8);
            String endYear = URLEncoder.encode(end.getText(),StandardCharsets.UTF_8);
            String hKey = URLEncoder.encode(HARVARD_API_KEY,StandardCharsets.UTF_8);
            String query = String.format("?after=%s&before=%s&fields=url",startYear,endYear);
            String query2 = String.format("&apikey=%s",hKey);
            String uri = HARVARD_API + query + query2;
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .build();
            HttpResponse<String> response = HTTP_CLIENT
                .send(request, BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IOException(response.toString());
            }


            String jsonString = response.body();
            HarvardResponse harvardResponse = GSON.fromJson(jsonString,HarvardResponse.class);
            HarvardData harvardData = GSON.fromJson(jsonString,HarvardData.class);
            urlList.clear();

            if (harvardData.info.totalrecordsperquery > harvardData.info.totalrecords) {
                for (int i = 0; i < harvardData.info.totalrecords; i++) {
                    urlList.add(harvardResponse.records[i].url);
                }
            } else {
                for (int i = 0; i < harvardData.info.totalrecordsperquery; i++) {
                    urlList.add(harvardResponse.records[i].url);
                }
            }
            if (urlList.size() == 0 ) {
                text3.setText("No artifacts available, please try another time period.");
                load.setDisable(false);
                back.setDisable(false);
                next.setDisable(false);
            } else {
                this.showData();
                this.text4.setText("results displayed");
                load.setDisable(false);
                if (urlList.size() == 1) {
                    back.setDisable(true);
                    next.setDisable(true);
                } else {
                    next.setDisable(false);
                }
            }
        } catch (IOException | InterruptedException e) {
            a.setAlertType(AlertType.ERROR);
            a.show();
        }
    }
/**
 * Querys each link provided to a screenshot api which displays a picture of the webpage
 * along with the original link to it.
 */

    public void showData() {
        try {
            load.setDisable(true);
            next.setDisable(true);
            back.setDisable(true);
            String query = "?url=" + urlList.get(pageNumber) + "&screenshot";
            String uri = SCREENSHOT_API + query;
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .header("x-api-key", "jY8vMUKzlRC8Spx5pHQy845FJlkyDrk4t3qrBtjf")
                .build();

            HttpResponse<String>response = HTTP_CLIENT
                .send(request,BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IOException(response.toString());
            }
            String json = response.body();

            LinkPreviewOverall previewResponse = GSON.fromJson(json, LinkPreviewOverall.class);
            String imageUrl = previewResponse.data.screenshot.url;
            picture = new Image(imageUrl);
            pictureView.setImage(picture);
            text3.setText(urlList.get(pageNumber));
            if (pageNumber == urlList.size() - 1) {
                next.setDisable(true);
                back.setDisable(false);
                load.setDisable(false);
            } else if (pageNumber == 0) {
                back.setDisable(true);
                next.setDisable(false);
                load.setDisable(false);
            } else {
                next.setDisable(false);
                back.setDisable(false);
                load.setDisable(false);
            }
        } catch (IOException | InterruptedException e) {
            a.setAlertType(AlertType.ERROR);
            a.show();
        }
    }

} // ApiApp
