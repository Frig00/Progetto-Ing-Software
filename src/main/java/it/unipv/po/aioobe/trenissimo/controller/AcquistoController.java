package it.unipv.po.aioobe.trenissimo.controller;

import it.unipv.po.aioobe.trenissimo.model.Utils;
import it.unipv.po.aioobe.trenissimo.model.acquisto.Acquisto;
import it.unipv.po.aioobe.trenissimo.model.persistence.entity.TitoloViaggioEntity;
import it.unipv.po.aioobe.trenissimo.model.persistence.service.TitoloViaggioService;
import it.unipv.po.aioobe.trenissimo.model.persistence.service.VoucherService;
import it.unipv.po.aioobe.trenissimo.model.titolodiviaggio.CorsaSingola;
import it.unipv.po.aioobe.trenissimo.model.titolodiviaggio.enumeration.TipoTitoloViaggio;
import it.unipv.po.aioobe.trenissimo.model.titolodiviaggio.utils.TicketBuilder;
import it.unipv.po.aioobe.trenissimo.model.user.Account;
import it.unipv.po.aioobe.trenissimo.model.viaggio.Viaggio;
import it.unipv.po.aioobe.trenissimo.view.HomePage;
import it.unipv.po.aioobe.trenissimo.view.ViaggioControl;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.net.URL;
import java.util.*;

/**
 * Controller class per acquistoView.fxml
 *
 * @author ArrayIndexOutOfBoundsException
 * @see it.unipv.po.aioobe.trenissimo.view.acquistoView
 * @see javafx.fxml.Initializable
 */

public class AcquistoController implements Initializable {

    @FXML
    private BorderPane root;

    @FXML
    private VBox boxViaggi;

    @FXML
    private Button btnAcquisto;
    @FXML
    private TextField txtNumCarta;
    @FXML
    private TextField txtDataScadenza;
    @FXML
    private TextField txtCVV;

    @FXML
    private Label lblRiscattoOK;
    @FXML
    private Label lblErroreRiscatto;
    @FXML
    private Button btnRiscatta;
    @FXML
    private TextField txtVoucher;

    @FXML
    private Label lblSubtotale;
    @FXML
    private Label lblIVA;
    @FXML
    private Label lblSconto;
    @FXML
    private Label lblTotale;

    @FXML
    private TextField txtNome;
    @FXML
    private TextField txtCognome;
    @FXML
    private DatePicker dtpDataNascita;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextField txtVia;
    @FXML
    private TextField txtCivico;
    @FXML
    private TextField txtCitta;
    @FXML
    private TextField txtCAP;

    @FXML
    private Button btnAggiungiPagamento;
    @FXML
    private Label lblErroreNumCarta;
    @FXML
    private Label lblErroreData;
    @FXML
    private Label lblErroreCVV;

    @FXML
    private Label lblErroreCAP;
    @FXML
    private Label lblErroreEmail;
    @FXML
    private Label lblErroreDataNascita;

    @FXML
    private Label lblErroreNome;
    @FXML
    private Label lblErroreCognome;
    @FXML
    private Label lblErroreVia;
    @FXML
    private Label lblErroreCivico;
    @FXML
    private Label lblErroreCitta;

    @FXML
    private Label lblCartaOK;
    @FXML
    private Button btnConferma;
    @FXML
    private Label lblDatiOK;

    private ObservableList<Viaggio> _viaggi;
    private TicketBuilder titoloViaggio;

    private boolean isIdVoucherOK;
    private Double subtotale;
    private Double iva;
    private boolean acquistoSoloVoucher;
    private boolean isRiscattoUsed;
    private File biglietto;

    /**
     * Metodo d'Inizializzazione
     *
     * @param location
     * @param resources
     * @see #checkIdRealTime()
     * @see #checkPagamentoRealTime()
     * @see #checkDatiRealTime()
     * @see Account
     * @see ViaggioControl
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        _viaggi = FXCollections.observableArrayList();
        _viaggi.addListener((ListChangeListener<Viaggio>) c -> {
            boxViaggi.getChildren().setAll(_viaggi.stream().map(x -> new ViaggioControl(x, null)).toList());
        });

        this.subtotale = 0.0;
        this.iva = 0.0;
        this.isRiscattoUsed = false;

        checkIdRealTime();
        checkPagamentoRealTime();
        checkDatiRealTime();

        if (Account.getLoggedIn()) {
            txtNome.setText(Account.getInstance().getDatiPersonali().getNome());
            txtCognome.setText(Account.getInstance().getDatiPersonali().getCognome());
            dtpDataNascita.setValue(Account.getInstance().getDatiPersonali().getDataNascita().toLocalDate());
            txtEmail.setText(Account.getInstance().getDatiPersonali().getMail());
            txtVia.setText(Account.getInstance().getDatiPersonali().getVia());
            txtCivico.setText(Account.getInstance().getDatiPersonali().getCivico());
            txtCitta.setText(Account.getInstance().getDatiPersonali().getCitta());
            txtCAP.setText(Account.getInstance().getDatiPersonali().getCap().toString());
        }
    }

    /**
     * Verifica per abilitazione del tasto acquista dopo la conferma dei dati personali e dei dati di pagamento/riscatto voucher
     */
    private void check() {
        if ((lblDatiOK.isVisible() && lblCartaOK.isVisible())) btnAcquisto.setDisable(false);
        else if ((lblDatiOK.isVisible() && acquistoSoloVoucher)) btnAcquisto.setDisable(false);
    }

    /**
     * Imposta i dati dell'acquisto e la lista dei viaggi
     *
     * @param viaggi
     * @see Viaggio
     */
    public void set_viaggi(@NotNull List<Viaggio> viaggi) {

        for (Viaggio v : viaggi) {
            subtotale = subtotale + v.getPrezzoTot();
            iva = iva + v.getPrezzoIva();
        }

        lblSubtotale.setText("€ " + String.format(Locale.US, "%.2f", subtotale));
        lblIVA.setText("€ " + String.format(Locale.US, "%.2f", iva));
        lblTotale.setText("€ " + String.format(Locale.US, "%.2f", subtotale));

        _viaggi.setAll(viaggi);
    }

    /**
     * Gestisce il pagamento e il download del biglietto PDF
     *
     * @throws Exception
     * @see #onScaricaBigliettoPDF(Acquisto)
     * @see HomePage
     * @see Acquisto
     * @see Viaggio
     * @see CorsaSingola
     * @see TipoTitoloViaggio
     * @see Account
     */
    @FXML
    protected void onPaga() throws Exception {

        onAlert("Acquisto avvenuto con successo!");

        List<Acquisto> biglietti = new ArrayList<>();
        for (Viaggio v : _viaggi) {
            biglietti.add(new CorsaSingola(TipoTitoloViaggio.BIGLIETTOCORSASINGOLA, v));
        }
        biglietti.forEach(x -> x.pagare());
        if (Account.getLoggedIn()) {
            biglietti.forEach(x -> x.puntiFedelta(biglietti));
            //metodi per aggiornare i punti fedeltà dal db all'istanza di Account
            String username = Account.getInstance().getUsername();
            Account.getInstance().setAccount(username);
        }
        for (Acquisto a : biglietti) {
            onScaricaBigliettoPDF(a);
        }

        biglietto.delete();

        HomePage.openScene(root.getScene().getWindow());
    }

    /**
     * Apre il file chooser e permette di scaricare il biglietto in formato PDF
     *
     * @param a
     * @throws Exception
     * @see #fillPDF(Acquisto)
     * @see File
     * @see FileChooser
     * @see TicketBuilder
     */
    @FXML
    protected void onScaricaBigliettoPDF(Acquisto a) throws Exception {
        fillPDF(a);

        this.biglietto = new File(TicketBuilder.DEST); //biglietto in folder temporanea

        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle("Scegli dove salvare il titolo di viaggio");
        fileChooser.setInitialFileName(a.getId());

        File destin = new File(fileChooser.showSaveDialog(new Stage()).getAbsolutePath().concat(".pdf"));
        TicketBuilder.copy(biglietto, destin);

    }

    /**
     * Compila i campi del biglietto PDF
     *
     * @param a acquisto da cui prendere informazioni
     * @throws Exception
     * @see TitoloViaggioService
     * @see TitoloViaggioEntity
     * @see Acquisto
     * @see TicketBuilder
     */
    private void fillPDF(@NotNull Acquisto a) throws Exception {
        TitoloViaggioService titoloViaggioService = new TitoloViaggioService();
        TitoloViaggioEntity titoloViaggioEntity;
        titoloViaggioEntity = titoloViaggioService.findById(a.getId());

        if (a.getId().startsWith("CS"))
            titoloViaggio = new TicketBuilder(titoloViaggioEntity.getStazionePartenza(), titoloViaggioEntity.getStazioneArrivo(), titoloViaggioEntity.getDataPartenza().toString(), titoloViaggioEntity.getDataArrivo().toString(), titoloViaggioEntity.getOraPartenza().toString(), titoloViaggioEntity.getOraArrivo().toString(), txtNome.getText(), txtCognome.getText(), dtpDataNascita.getValue().toString(), a.getId(), String.valueOf(a.getPrezzo()), String.valueOf(_viaggi.get(0).getNumAdulti()), String.valueOf(_viaggi.get(0).getNumRagazzi()), String.valueOf(_viaggi.get(0).getNumBambini()), String.valueOf(_viaggi.get(0).getNumAnimali()));

        titoloViaggio.createPdf(a.getId());
    }

    /**
     * Gestisce il riscatto del voucher
     *
     * @throws Exception
     * @see VoucherService
     * @see Utils
     * @see Task
     * @see Thread
     */
    @FXML
    protected void onRiscatta() throws Exception {

        VoucherService voucherService = new VoucherService();

        if (!(Utils.checkIdVoucher(txtVoucher.getText()))) {
            lblErroreRiscatto.setVisible(true);
            txtVoucher.setStyle("-fx-border-color: #d70000");
            isIdVoucherOK = false;
        }

        if (isIdVoucherOK) {
            this.isRiscattoUsed = true;
            String[] totaleSplit = lblTotale.getText().split("(?<=€)");

            if (Double.parseDouble(totaleSplit[1]) <= voucherService.findById(txtVoucher.getText()).getPrezzo())
                lblSconto.setText("-€ " + totaleSplit[1]);
            else lblSconto.setText("-€" + (voucherService.findById(txtVoucher.getText()).getPrezzo()));

            String[] scontoSplit = lblSconto.getText().split("(?<=€)");

            double calcoloTot = (Double.parseDouble(totaleSplit[1]) - Double.parseDouble(scontoSplit[1]));
            if (calcoloTot <= 0.0) {
                var v = voucherService.findById(txtVoucher.getText());
                v.setPrezzo(Double.parseDouble(String.format(Locale.US, "%.2f", (v.getPrezzo() - Double.parseDouble(totaleSplit[1])))));
                voucherService.update(v);
                acquistoSoloVoucher = true;
                lblTotale.setText("€ " + 0.0);
            } else {
                lblTotale.setText("€" + Double.valueOf(String.format(Locale.US, "%.2f", calcoloTot)));
                acquistoSoloVoucher = false;
                voucherService.deleteById(txtVoucher.getText());
            }

            lblRiscattoOK.setVisible(true);

            Task<Void> task = new Task<Void>() {

                /**
                 * Aspetta 2.0 secondi
                 * @return sempre null
                 * @throws InterruptedException necessaria per Thread.sleep()
                 */
                @Override
                public @Nullable Void call() throws InterruptedException {
                    Thread.sleep(2000);
                    return null;
                }
            };
            task.setOnSucceeded(e -> {
                lblRiscattoOK.setVisible(false);
                btnRiscatta.setDisable(true);
            });
            new Thread(task).start();

        } else isIdVoucherOK = false;

        check();
    }

    /**
     * Conferma i dati personali
     */
    @FXML
    protected void onConfermaDati() {
        lblDatiOK.setVisible(true);
        txtNome.setDisable(true);
        txtCognome.setDisable(true);
        dtpDataNascita.setDisable(true);
        txtVia.setDisable(true);
        txtCivico.setDisable(true);
        txtCitta.setDisable(true);
        txtCAP.setDisable(true);
        txtEmail.setDisable(true);
        check();
    }

    /**
     * Gestisce il controllo e la scelta della carta di credito
     *
     * @see Utils
     */
    @FXML
    protected void onAggiungiCarta() {
        if (Utils.checkNumCarta(txtNumCarta.getText()) && Utils.checkDataScadenza(txtDataScadenza.getText()) && Utils.checkCVV(txtCVV.getText())) {
            lblCartaOK.setVisible(true);
            txtNumCarta.setDisable(true);
            txtDataScadenza.setDisable(true);
            txtCVV.setDisable(true);
        }
        check();
    }

    /**
     * Controlla la validità del voucher
     *
     * @see Utils
     */
    private void checkIdRealTime() {

        txtVoucher.textProperty().addListener((observable, oldValue, newValue) -> {
            if (Utils.checkDatiGenerico(txtVoucher.getText())) {
                lblErroreRiscatto.setVisible(false);
                btnRiscatta.setDisable(false);
                txtVoucher.setStyle("-fx-border-color: #cccccc");
            } else {
                lblErroreRiscatto.setVisible(false);
                txtVoucher.setStyle("-fx-border-color: #d70000");
            }
            try {
                if (!(Utils.checkIdVoucher(txtVoucher.getText()))) {
                    lblErroreRiscatto.setVisible(true);
                    txtVoucher.setStyle("-fx-border-color: #d70000");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        });

        btnRiscatta.setOnMouseMoved(c -> {
            if (lblErroreRiscatto.isVisible() || isRiscattoUsed) {
                btnRiscatta.setDisable(true);
                isIdVoucherOK = false;
            } else isIdVoucherOK = true;

        });
    }

    /**
     * Controlla la validità della carta di credito
     *
     * @see Utils
     */
    private void checkPagamentoRealTime() {

        txtNumCarta.textProperty().addListener((observable, oldValue, newValue) -> {
            if (Utils.checkNumCarta(txtNumCarta.getText())) {
                lblErroreNumCarta.setVisible(false);
                btnAggiungiPagamento.setDisable(false);
                txtNumCarta.setStyle("-fx-border-color: #cccccc");
            } else {
                lblErroreNumCarta.setVisible(true);
                txtNumCarta.setStyle("-fx-border-color: #d70000");
            }
        });

        txtCVV.textProperty().addListener((observable, oldValue, newValue) -> {
            if (Utils.checkCVV(txtCVV.getText())) {
                lblErroreCVV.setVisible(false);
                btnAggiungiPagamento.setDisable(false);
                txtCVV.setStyle("-fx-border-color: #cccccc");
            } else {
                lblErroreCVV.setVisible(true);
                txtCVV.setStyle("-fx-border-color: #d70000");
            }
        });

        txtDataScadenza.textProperty().addListener((observable, oldValue, newValue) -> {
            if (txtDataScadenza.getText().length() == 7) {
                if (Utils.checkDataScadenza(txtDataScadenza.getText())) {
                    lblErroreData.setVisible(false);
                    btnAggiungiPagamento.setDisable(false);
                    txtDataScadenza.setStyle("-fx-border-color: #cccccc");
                } else {
                    lblErroreData.setVisible(true);
                    txtDataScadenza.setStyle("-fx-border-color: #d70000");
                }
            }
        });

        btnAggiungiPagamento.setOnMouseMoved(c -> {
            if (lblErroreNumCarta.isVisible() || lblErroreCVV.isVisible() || lblErroreData.isVisible() || txtNumCarta.getText().length() == 0 || txtCVV.getText().length() == 0 || txtDataScadenza.getText().length() != 7)
                btnAggiungiPagamento.setDisable(true);
        });

    }

    /**
     * Controlla la validità dei dati personali
     *
     * @see Account
     * @see Utils
     */
    private void checkDatiRealTime() {

        lblErroreCAP.setVisible(true);
        lblErroreEmail.setVisible(true);
        lblErroreDataNascita.setVisible(true);
        lblErroreNome.setVisible(true);
        lblErroreCognome.setVisible(true);
        lblErroreVia.setVisible(true);
        lblErroreCivico.setVisible(true);
        lblErroreCitta.setVisible(true);

        btnConferma.setDisable(true);

        txtCAP.textProperty().addListener((observable, oldValue, newValue) -> {
            if (Account.getInstance().checkCAP(txtCAP.getText())) {
                lblErroreCAP.setVisible(false);
                btnConferma.setDisable(false);
                txtCAP.setStyle("-fx-border-color: #cccccc");
            } else {
                lblErroreCAP.setVisible(true);
                txtCAP.setStyle("-fx-border-color: #d70000");
            }
        });

        txtEmail.textProperty().addListener((observable, oldValue, newValue) -> {
            if (Account.getInstance().checkEmail(txtEmail.getText())) {
                lblErroreEmail.setVisible(false);
                btnConferma.setDisable(false);
                txtEmail.setStyle("-fx-border-color: #cccccc");
            } else {
                lblErroreEmail.setVisible(true);
                txtEmail.setStyle("-fx-border-color: #d70000");

            }
        });

        dtpDataNascita.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (Account.getInstance().checkDataNascita(dtpDataNascita.getValue())) {
                lblErroreDataNascita.setVisible(false);
                btnConferma.setDisable(false);
                dtpDataNascita.setStyle("-fx-border-color: #cccccc");
            } else {
                lblErroreDataNascita.setVisible(true);
                dtpDataNascita.setStyle("-fx-border-color: #d70000");
            }
        });

        txtNome.textProperty().addListener((observable, oldValue, newValue) -> {
            if (Utils.checkDatiGenerico(txtNome.getText())) {
                lblErroreNome.setVisible(false);
                btnConferma.setDisable(false);
                txtNome.setStyle("-fx-border-color: #cccccc");
            } else {
                lblErroreNome.setVisible(true);
                txtNome.setStyle("-fx-border-color: #d70000");
            }
        });

        txtCognome.textProperty().addListener((observable, oldValue, newValue) -> {
            if (Utils.checkDatiGenerico(txtCognome.getText())) {
                lblErroreCognome.setVisible(false);
                btnConferma.setDisable(false);
                txtCognome.setStyle("-fx-border-color: #cccccc");
            } else {
                lblErroreCognome.setVisible(true);
                txtCognome.setStyle("-fx-border-color: #d70000");
            }
        });

        txtVia.textProperty().addListener((observable, oldValue, newValue) -> {
            if (Utils.checkDatiGenerico(txtVia.getText())) {
                lblErroreVia.setVisible(false);
                btnConferma.setDisable(false);
                txtVia.setStyle("-fx-border-color: #cccccc");
            } else {
                lblErroreVia.setVisible(true);
                txtVia.setStyle("-fx-border-color: #d70000");
            }
        });

        txtCivico.textProperty().addListener((observable, oldValue, newValue) -> {
            if (Utils.checkDatiGenerico(txtCivico.getText())) {
                lblErroreCivico.setVisible(false);
                btnConferma.setDisable(false);
                txtCivico.setStyle("-fx-border-color: #cccccc");
            } else {
                lblErroreCivico.setVisible(true);
                txtCivico.setStyle("-fx-border-color: #d70000");
            }
        });

        txtCitta.textProperty().addListener((observable, oldValue, newValue) -> {
            if (Utils.checkDatiGenerico(txtCitta.getText())) {
                lblErroreCitta.setVisible(false);
                btnConferma.setDisable(false);
                txtCitta.setStyle("-fx-border-color: #cccccc");
            } else {
                lblErroreCitta.setVisible(true);
                txtCitta.setStyle("-fx-border-color: #d70000");
            }
        });

        btnConferma.setOnMouseMoved(c -> {
            if (lblErroreNome.isVisible() || lblErroreCognome.isVisible() || lblErroreDataNascita.isVisible() || lblErroreEmail.isVisible() || lblErroreVia.isVisible() || lblErroreCivico.isVisible() || lblErroreCitta.isVisible() || lblErroreCAP.isVisible()) {
                btnConferma.setDisable(true);
            }
        });
    }

    /**
     * Mostra una finestra di dialogo contenente informazioni personalizzate
     *
     * @param messaggio messaggio da stampare nell'alert
     * @see Alert
     * @see Stage
     */
    private void onAlert(String messaggio) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Trenissimo");
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(Objects.requireNonNull(HomePage.class.getResourceAsStream("HomePage/LogoIcona.png"))));
        alert.showAndWait();
    }

    /**
     * Ritorna alla Home Page
     *
     * @see HomePage
     */
    @FXML
    protected void onGoToHomepage() {
        HomePage.openScene(root.getScene().getWindow());
    }
}
