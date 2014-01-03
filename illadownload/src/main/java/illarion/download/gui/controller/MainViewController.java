package illarion.download.gui.controller;

import illarion.common.config.Config;
import illarion.common.util.ProgressMonitor;
import illarion.common.util.ProgressMonitorCallback;
import illarion.download.cleanup.Cleaner;
import illarion.download.launcher.JavaLauncher;
import illarion.download.maven.MavenDownloader;
import illarion.download.maven.MavenDownloaderCallback;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.apache.log4j.Logger;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Martin Karing &lt;nitram@illarion.org&gt;
 */
public class MainViewController extends AbstractController implements MavenDownloaderCallback, ProgressMonitorCallback {
    @FXML
    public AnchorPane newsPane;
    @FXML
    public AnchorPane questsPane;
    @FXML
    public ProgressBar progress;
    @FXML
    public Label progressDescription;
    @FXML
    public Button launchEasyNpcButton;
    @FXML
    public Button launchEasyQuestButton;
    @FXML
    public Button launchMapEditButton;
    @FXML
    public Button launchClientButton;
    @FXML
    public Button uninstallButton;

    private ResourceBundle resourceBundle;

    private static final Logger LOGGER = Logger.getLogger(MainViewController.class);

    @Override
    public void initialize(URL url, @Nonnull ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
        progress.setProgress(0.0);
        progressDescription.setText(resourceBundle.getString("selectStartApp"));

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    readNewsAndQuests();
                } catch (@Nonnull final XmlPullParserException | IOException | ParseException e) {
                    LOGGER.error("Failed reading news and quests.", e);
                }
            }
        }).start();
    }

    @Nullable
    private String launchClass;
    private boolean useSnapshots;

    private static class NewsQuestEntry {
        @Nonnull
        public final String title;
        public final String timeStamp;
        public final URL linkTarget;

        NewsQuestEntry(@Nonnull final String title, @Nonnull final String timeStamp, @Nonnull final URL linkTarget) {
            this.title = title;
            this.timeStamp = timeStamp;
            this.linkTarget = linkTarget;
        }
    }

    private void readNewsAndQuests() throws XmlPullParserException, IOException, ParseException {
        final XmlPullParserFactory parserFactory = XmlPullParserFactory.newInstance();
        parserFactory.setValidating(false);
        parserFactory.setNamespaceAware(false);

        final XmlPullParser parser = parserFactory.newPullParser();
        final URL src = new URL("http://illarion.org/data/xml_launcher.php");
        parser.setInput(new BufferedInputStream(src.openStream()), "UTF-8");

        final Collection<NewsQuestEntry> newsList = new ArrayList<>();
        final Collection<NewsQuestEntry> questList = new ArrayList<>();

        int current = parser.nextTag();
        while (current != XmlPullParser.START_TAG || !"launcher".equals(parser.getName())) {
            current = parser.nextTag();
        }
        parseLauncherXml(parser, newsList, questList);

        showNewsQuestInList(newsList, newsPane);
        showNewsQuestInList(questList, questsPane);
    }

    private static void parseLauncherXml(@Nonnull final XmlPullParser parser,
                                         @Nonnull final Collection<NewsQuestEntry> newsList,
                                         @Nonnull final Collection<NewsQuestEntry> questList)
            throws IOException, XmlPullParserException, ParseException {
        while (true) {
            int current = parser.nextToken();
            switch (current) {
                case XmlPullParser.END_DOCUMENT:
                    return;
                case XmlPullParser.END_TAG:
                    if ("launcher".equals(parser.getName())) {
                        return;
                    }
                    break;
                case XmlPullParser.START_TAG:
                    switch (parser.getName()) {
                        case "news":
                            parserNewsXml(parser, newsList);
                            break;
                        case "quests":
                            parserQuestXml(parser, questList);
                            break;
                    }
                    break;
            }
        }
    }

    private static void parserNewsXml(@Nonnull final XmlPullParser parser,
                                      @Nonnull final Collection<NewsQuestEntry> newsList)
            throws IOException, XmlPullParserException, ParseException {
        final boolean useGerman = Locale.getDefault().getLanguage().equals(Locale.GERMAN.getLanguage());

        String title = null;
        String timestamp = null;
        URL linkTarget = null;

        final DateFormat parsingFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
        final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);

        while (true) {
            int current = parser.nextTag();
            switch (current) {
                case XmlPullParser.END_DOCUMENT:
                    return;
                case XmlPullParser.END_TAG:
                    switch (parser.getName()) {
                        case "news":
                            return;
                        case "item":
                            if (title != null && timestamp != null && linkTarget != null) {
                                newsList.add(new NewsQuestEntry(title, timestamp, linkTarget));
                            }
                            break;
                    }
                    break;
                case XmlPullParser.START_TAG:
                    switch (parser.getName()) {
                        case "id":
                            parser.nextText();
                            break;
                        case "title":
                            final boolean german = "de".equals(parser.getAttributeValue(null, "lang"));
                            final String text = parser.nextText();
                            if (german == useGerman) {
                                title = text;
                            }
                            break;
                        case "link":
                            linkTarget = new URL(parser.nextText());
                            break;
                        case "date":
                            final Date date = parsingFormat.parse(parser.nextText());
                            timestamp = dateFormat.format(date);
                            break;
                    }
                    break;
            }
        }
    }

    private static void parserQuestXml(@Nonnull final XmlPullParser parser,
                                       @Nonnull final Collection<NewsQuestEntry> newsList)
            throws IOException, XmlPullParserException, ParseException {
        final boolean useGerman = Locale.getDefault().getLanguage().equals(Locale.GERMAN.getLanguage());

        String title = null;
        String timestamp = null;
        URL linkTarget = null;

        final DateFormat parsingFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
        final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);

        while (true) {
            int current = parser.nextTag();
            switch (current) {
                case XmlPullParser.END_DOCUMENT:
                    return;
                case XmlPullParser.END_TAG:
                    switch (parser.getName()) {
                        case "quests":
                            return;
                        case "item":
                            if (title != null && timestamp != null && linkTarget != null) {
                                newsList.add(new NewsQuestEntry(title, timestamp, linkTarget));
                            }
                            break;
                    }
                    break;
                case XmlPullParser.START_TAG:
                    switch (parser.getName()) {
                        case "id":
                            parser.nextText();
                            break;
                        case "title":
                            final boolean german = "de".equals(parser.getAttributeValue(null, "lang"));
                            final String text = parser.nextText();
                            if (german == useGerman) {
                                title = text;
                            }
                            break;
                        case "link":
                            linkTarget = new URL(parser.nextText());
                            break;
                        case "date":
                            final Date date = parsingFormat.parse(parser.nextText());
                            timestamp = dateFormat.format(date);
                            break;
                    }
                    break;
            }
        }
    }

    private void showNewsQuestInList(@Nonnull final Collection<NewsQuestEntry> list, @Nonnull final Pane display) {
        final VBox storage = new VBox();
        storage.setFillWidth(true);
        AnchorPane.setBottomAnchor(storage, 0.0);
        AnchorPane.setTopAnchor(storage, 0.0);
        AnchorPane.setLeftAnchor(storage, 3.0);
        AnchorPane.setRightAnchor(storage, 3.0);

        for (@Nonnull final NewsQuestEntry entry : list) {
            final BorderPane line = new BorderPane();
            line.getStyleClass().add("linkPane");
            line.setLeft(new Label(entry.title));
            line.setRight(new Label(entry.timeStamp));
            line.setCursor(Cursor.HAND);

            line.setMouseTransparent(false);
            line.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if (mouseEvent.getButton() == MouseButton.PRIMARY &&
                            mouseEvent.getEventType() == MouseEvent.MOUSE_CLICKED &&
                            mouseEvent.getClickCount() == 1) {
                        getModel().getHostServices().showDocument(entry.linkTarget.toExternalForm());
                    }
                }
            });
            storage.getChildren().add(line);
        }

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                display.getChildren().add(storage);
            }
        });
    }

    @FXML
    public void goToAccount(@Nonnull final ActionEvent actionEvent) {
        getModel().getHostServices().showDocument("http://illarion.org/community/account/index.php");
    }

    @FXML
    public void startEasyNpc(@Nonnull final ActionEvent actionEvent) {
        updateLaunchButtons(false, false, true, false, false);
        launch("org.illarion", "easynpc", "illarion.easynpc.gui.MainFrame", "channelEasyNpc");
    }

    @FXML
    public void startEasyQuest(@Nonnull final ActionEvent actionEvent) {
        updateLaunchButtons(false, false, false, true, false);
        launch("org.illarion", "easyquest", "illarion.easyquest.gui.MainFrame", "channelEasyQuest");
    }

    @FXML
    public void startMapEdit(@Nonnull final ActionEvent actionEvent) {
        updateLaunchButtons(false, false, false, false, true);
        launch("org.illarion", "mapeditor", "illarion.mapedit.MapEditor", "channelMapEditor");
    }

    @FXML
    public void launchClient(@Nonnull final ActionEvent actionEvent) {
        updateLaunchButtons(false, true, false, false, false);
        launch("org.illarion", "client", "illarion.client.IllaClient", "channelClient");
    }

    private void updateLaunchButtons(final boolean enabled, final boolean client, final boolean easyNpc,
                                     final boolean easyQuest, final boolean mapEdit) {
        if (Platform.isFxApplicationThread()) {
            launchClientButton.setDisable(!enabled);
            launchMapEditButton.setDisable(!enabled);
            launchEasyQuestButton.setDisable(!enabled);
            launchEasyNpcButton.setDisable(!enabled);
            uninstallButton.setDisable(!enabled);
            if (enabled) {
                launchClientButton.setText(resourceBundle.getString("launchClient"));
                launchMapEditButton.setText(resourceBundle.getString("launchMapEdit"));
                launchEasyQuestButton.setText(resourceBundle.getString("launchEasyQuest"));
                launchEasyNpcButton.setText(resourceBundle.getString("launchEasyNpc"));
            } else {
                launchClientButton.setText(resourceBundle.getString(client ? "starting" : "launchClient"));
                launchMapEditButton.setText(resourceBundle.getString(mapEdit ? "starting" : "launchMapEdit"));
                launchEasyQuestButton.setText(resourceBundle.getString(easyQuest ? "starting" : "launchEasyQuest"));
                launchEasyNpcButton.setText(resourceBundle.getString(easyNpc ? "starting" : "launchEasyNpc"));
            }
        } else {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    updateLaunchButtons(enabled, client, easyNpc, easyQuest, mapEdit);
                }
            });
        }
    }

    private void launch(@Nonnull final String groupId, @Nonnull final String artifactId,
                        @Nonnull final String launchClass, @Nonnull final String configKey) {
        final Config cfg = getModel().getConfig();
        if (cfg == null) {
            throw new IllegalStateException("Can't show options without the config system");
        }

        this.launchClass = launchClass;
        this.useSnapshots = cfg.getInteger(configKey) == 1;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final MavenDownloader downloader = new MavenDownloader(useSnapshots);
                    downloader.downloadArtifact(groupId,artifactId, MainViewController.this);
                } catch (@Nonnull final Exception e) {
                    cancelLaunch();
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            progress.setProgress(0.0);
                            progressDescription.setText(e.getLocalizedMessage());
                        }
                    });
                }
            }
        }).start();
    }

    @Override
    public void reportNewState(@Nonnull final State state, @Nullable final ProgressMonitor progress) {
        if (Platform.isFxApplicationThread()) {
            switch (state) {
                case SearchingNewVersion:
                    progressDescription.setText(resourceBundle.getString("searchingNewVersion"));
                    break;
                case ResolvingDependencies:
                    progressDescription.setText(resourceBundle.getString("resolvingDependencies"));
                    break;
                case ResolvingArtifacts:
                    progressDescription.setText(resourceBundle.getString("resolvingArtifacts"));
                    break;
            }
            if (progress == null) {
                this.progress.setProgress(-1.0);
            } else {
                progress.setCallback(this);
                this.progress.setProgress(progress.getProgress());
            }
        } else {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    reportNewState(state, progress);
                }
            });
        }
    }

    @Override
    public void resolvingDone(@Nullable final Collection<File> classpath) {
        if (classpath == null || launchClass == null) {
            cancelLaunch();
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    progress.setProgress(1.0);
                    progressDescription.setText(resourceBundle.getString("errorClasspathNull"));
                }
            });
        } else {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    progress.setProgress(1.0);
                    progressDescription.setText(resourceBundle.getString("launchApplication"));
                }
            });
            final JavaLauncher launcher = new JavaLauncher(useSnapshots);
            if (launcher.launch(classpath, launchClass)) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        getModel().getStage().close();
                    }
                });
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final Cleaner cleaner = new Cleaner(Cleaner.Mode.Maintenance);
                        cleaner.clean();
                    }
                }).run();
            } else {
                cancelLaunch();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        progress.setProgress(1.0);
                        progressDescription.setText(launcher.getErrorData());
                    }
                });
            }
        }
    }

    @Override
    public void updatedProgress(@Nonnull final ProgressMonitor monitor) {
        progress.setProgress(monitor.getProgress());
    }

    private void cancelLaunch() {
        updateLaunchButtons(true, false, false, false, false);
    }

    @FXML
    public void showOptions(@Nonnull final ActionEvent actionEvent) {
        try {
            getModel().getStoryboard().showOptions();
        } catch (@Nonnull final IOException ignored) {
        }
    }

    @FXML
    public void uninstall(@Nonnull final ActionEvent actionEvent) {
        try {
            getModel().getStoryboard().showUninstall();
        } catch (@Nonnull final IOException ignored) {
        }
    }
}