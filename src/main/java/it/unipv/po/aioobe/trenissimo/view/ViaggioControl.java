package it.unipv.po.aioobe.trenissimo.view;

import it.unipv.po.aioobe.trenissimo.model.Utils;
import it.unipv.po.aioobe.trenissimo.model.viaggio.ViaggioAlt;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ViaggioControl extends VBox {
    @FXML private Label lblCompanyId;
    @FXML private Label lblTrainId;
    @FXML private Label lblDepartureTime;
    @FXML private Label lblDepartureStation;
    @FXML private Label lblArrivalTime;
    @FXML private Label lblArrivalStation;
    @FXML private Label lblTravelTime;
    @FXML private Label lblChanges;
    @FXML private Label lblPrice;

    private ViaggioAlt viaggio;


    public ViaggioControl() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("viaggioControl/viaggioControl.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ViaggioControl(ViaggioAlt viaggio){
        this();
        setViaggio(viaggio);
    }

    public ViaggioAlt getViaggio() {
        return viaggio;
    }

    public void setViaggio(ViaggioAlt viaggio) {
        this.viaggio = viaggio;

        lblCompanyId            .textProperty().setValue("Trenord");
        lblTrainId              .textProperty().setValue("...");
        lblDepartureTime        .textProperty().setValue(Utils.secondsToTime(viaggio.getOrarioPartenza(), false));
        lblDepartureStation     .textProperty().setValue(viaggio.getStazionePartenza().getStopName());
        lblArrivalTime          .textProperty().setValue(Utils.secondsToTime(viaggio.getOrarioArrivo(), false));
        lblArrivalStation       .textProperty().setValue(viaggio.getStazioneArrivo().getStopName());
        lblTravelTime           .textProperty().setValue(((int) viaggio.getDurata() / 60) + " mins");
        lblChanges              .textProperty().setValue(viaggio.getNumeroCambi() + " cambi");
        lblPrice                .textProperty().setValue(viaggio.getPrezzo());

    }





    @FXML
    protected void doSomething() {
        System.out.println("The button was clicked!");
    }

}
