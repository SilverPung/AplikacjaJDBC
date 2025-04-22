package com.project.controller;

import com.project.dao.ProjektDAO;
import com.project.dao.ProjektDAOImpl;
import com.project.model.Projekt;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ProjectController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class.getName());

    // Variables for pagination and search
    private String search4;
    private Integer pageNum;
    private Integer pageSize;
    private ProjektDAO projektDAO;
    private ExecutorService executorService;
    private ObservableList<Projekt> projekty;

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter dateTimeFormater = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Automatically injected GUI components
    @FXML
    private ChoiceBox<Integer> cbPageSizes;
    @FXML
    private TableView<Projekt> tblProjekt;
    @FXML
    private TableColumn<Projekt, Integer> colId;
    @FXML
    private TableColumn<Projekt, String> colNazwa;
    @FXML
    private TableColumn<Projekt, String> colOpis;
    @FXML
    private TableColumn<Projekt, LocalDateTime> colDataCzasUtworzenia;
    @FXML
    private TableColumn<Projekt, LocalDate> colDataOddania;
    @FXML
    private TableColumn<Projekt, Void> colEdytuj;
    @FXML
    private TextField txtSzukaj;
    @FXML
    private Button btnDalej;
    @FXML
    private Button btnWstecz;
    @FXML
    private Button btnPierwsza;
    @FXML
    private Button btnOstatnia;
    @FXML
    private Button btnSzukaj;
    @FXML
    private Button btnDodaj;

    /**
     * Konstruktor domyślny.
     */
    public ProjectController() {
    }

    /**
     * Konstruktor z parametrem DAO do obsługi projektów.
     * @param projektDAO obiekt DAO do zarządzania projektami.
     */
    public ProjectController(ProjektDAO projektDAO) {
        this.projektDAO = projektDAO;
        executorService = Executors.newFixedThreadPool(1);
    }

    /**
     * Metoda inicjalizująca kontroler. Ustawia domyślne wartości i ładuje pierwszą stronę danych.
     */
    @FXML
    public void initialize() {
        search4 = "";
        pageNum = 0;
        pageSize = 10;
        initPageSizeChoiceBox();
        initTable();
        projekty = FXCollections.observableArrayList();
        tblProjekt.setItems(projekty);
        executorService.execute(() -> loadPage(search4, pageNum, pageSize));
    }

    /**
     * Inicjalizuje ChoiceBox do wyboru rozmiaru strony.
     */
    private void initPageSizeChoiceBox() {
        cbPageSizes.getItems().addAll(5, 10, 20, 50, 100);
        cbPageSizes.setValue(pageSize);
        cbPageSizes.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            pageSize = newVal;
            pageNum = 0;
            loadPage(search4, pageNum, pageSize);
        });
    }

    /**
     * Inicjalizuje tabelę z danymi projektów, w tym kolumny i ich formatowanie.
     */
    private void initTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("projektId"));
        colNazwa.setCellValueFactory(new PropertyValueFactory<>("nazwa"));
        colOpis.setCellValueFactory(new PropertyValueFactory<>("opis"));
        colDataCzasUtworzenia.setCellValueFactory(new PropertyValueFactory<>("dataCzasUtworzenia"));
        colDataOddania.setCellValueFactory(new PropertyValueFactory<>("dataOddania"));
        colEdytuj.setCellFactory(column -> createEditDeleteCell());
        colDataCzasUtworzenia.setCellFactory(column -> new TableCell<Projekt, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(dateTimeFormater.format(item));
                }
            }
        });
    }

    /**
     * Tworzy komórkę tabeli z przyciskami do edycji i usuwania projektów.
     * @return komórka tabeli z przyciskami.
     */
    private TableCell<Projekt, Void> createEditDeleteCell() {
        return new TableCell<Projekt, Void>() {
            private final Button btnUsun = new Button("usuń");
            private final Button btnEdytuj = new Button("edytuj");

            {
                btnUsun.setOnAction(event -> {
                    Projekt projekt = getTableView().getItems().get(getIndex());
                    projektDAO.deleteProjekt(projekt.getProjektId());
                    tblProjekt.getItems().remove(projekt);
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Projekt usunięty pomyślnie!", ButtonType.OK);
                    alert.showAndWait();
                });

                btnEdytuj.setOnAction(event -> {
                    Projekt projekt = getTableView().getItems().get(getIndex());
                    projektWindow(projekt);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox pane = new HBox(5);
                    pane.getChildren().addAll(btnEdytuj, btnUsun);
                    setGraphic(pane);
                }
            }
        };
    }

    /**
     * Obsługuje akcję przycisku "Szukaj". Wyszukuje projekty na podstawie wprowadzonego tekstu.
     * @param event zdarzenie akcji.
     */
    @FXML
    private void onActionBtnSzukaj(ActionEvent event) {
        search4 = txtSzukaj.getText().trim();
        pageNum = 0;
        loadPage(search4, pageNum, pageSize);
    }

    /**
     * Obsługuje akcję przycisku "Dalej". Ładuje następną stronę danych.
     * @param event zdarzenie akcji.
     */
    @FXML
    private void onActionBtnDalej(ActionEvent event) {
        pageNum++;
        loadPage(search4, pageNum, pageSize);
    }

    /**
     * Obsługuje akcję przycisku "Wstecz". Ładuje poprzednią stronę danych.
     * @param event zdarzenie akcji.
     */
    @FXML
    private void onActionBtnWstecz(ActionEvent event) {
        if (pageNum > 0) {
            pageNum--;
            loadPage(search4, pageNum, pageSize);
        }
    }

    /**
     * Obsługuje akcję przycisku "Pierwsza". Ładuje pierwszą stronę danych.
     * @param event zdarzenie akcji.
     */
    @FXML
    private void onActionBtnPierwsza(ActionEvent event) {
    }

    /**
     * Obsługuje akcję przycisku "Ostatnia". Ładuje ostatnią stronę danych.
     * @param event zdarzenie akcji.
     */
    @FXML
    private void onActionBtnOstatnia(ActionEvent event) {
    }

    /**
     * Obsługuje akcję przycisku "Dodaj". Otwiera okno do dodawania nowego projektu.
     * @param event zdarzenie akcji.
     */
    @FXML
    private void onActionBtnDodaj(ActionEvent event) {
        projektWindow(null);
    }

    /**
     * Otwiera okno do edycji lub dodawania projektu.
     * @param oldProjekt projekt do edycji lub null, jeśli tworzony jest nowy projekt.
     */
    private void projektWindow(Projekt oldProjekt) {
        Stage stage = new Stage();
        VBox layout = new VBox(10);

        Label lblNazwa = new Label("Nazwa:");
        TextField txtNazwa = new TextField();

        Label lblOpis = new Label("Opis:");
        TextField txtOpis = new TextField();

        Label lblData = new Label("Data:");
        DatePicker datePicker = new DatePicker();

        if (oldProjekt != null) {
            txtNazwa.setText(oldProjekt.getNazwa());
            txtOpis.setText(oldProjekt.getOpis());
            datePicker.setValue(oldProjekt.getDataOddania());
        }

        Button btnConfirm = new Button("Zapisz");
        btnConfirm.setOnAction(e -> {
            // process entered values
            if (txtNazwa.getText().isEmpty() || txtOpis.getText().isEmpty() || datePicker.getValue() == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Wszystkie pola muszą być wypełnione!", ButtonType.OK);
                alert.showAndWait();
                return;
            }
            String nazwa = txtNazwa.getText();
            String opis = txtOpis.getText();
            LocalDate dataOddania = datePicker.getValue();
            if (oldProjekt == null) {
                Projekt projekt = new Projekt(nazwa, opis, dataOddania);
                projekt.setDataCzasUtworzenia(LocalDateTime.now());
                projektDAO.setProjekt(projekt);
                projekty.add(projekt);
            } else {
                oldProjekt.setNazwa(nazwa);
                oldProjekt.setOpis(opis);
                oldProjekt.setDataOddania(dataOddania);
                projektDAO.setProjekt(oldProjekt);
                int index = projekty.indexOf(oldProjekt);
                if (index != -1) {
                    projekty.set(index, oldProjekt);
                }
            }

            stage.close();
        });

        layout.getChildren().addAll(lblNazwa, txtNazwa, lblOpis, txtOpis, lblData, datePicker, btnConfirm);

        Scene scene = new Scene(layout);
        stage.setScene(scene);
        stage.sizeToScene();
        stage.setTitle("Stwórz nowy projekt");

        stage.showAndWait();
    }

    /**
     * Ładuje stronę danych projektów na podstawie kryteriów wyszukiwania, numeru strony i rozmiaru strony.
     * @param search4 tekst wyszukiwania.
     * @param pageNo numer strony.
     * @param pageSize rozmiar strony.
     */
    private void loadPage(String search4, Integer pageNo, Integer pageSize) {
        try {
            final List<Projekt> projektList = new ArrayList<>();
            if (search4 != null && !search4.isEmpty()) {
                projektList.addAll(projektDAO.getProjektyWhereNazwaLike(search4, pageNo * pageSize, pageSize));
            } else {
                projektList.addAll(projektDAO.getProjekty(pageNo * pageSize, pageSize));
            }
            Platform.runLater(() -> {
                projekty.clear();
                projekty.addAll(projektList);
            });
        } catch (RuntimeException e) {
            String errMsg = "Błąd podczas pobierania listy projektów.";
            logger.error(errMsg, e);
            String errDetails = e.getCause() != null ?
                    e.getMessage() + "\n" + e.getCause().getMessage()
                    : e.getMessage();
            Platform.runLater(() -> showError(errMsg, errDetails));
        }
    }

    /**
     * Wyświetla okno błędu z podanym nagłówkiem i treścią.
     * @param header nagłówek błędu.
     * @param content treść błędu.
     */
    private void showError(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Błąd");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Zamyka executorService, zapewniając poprawne zakończenie wątków.
     */
    public void shutdown() {
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
        }
    }

}
