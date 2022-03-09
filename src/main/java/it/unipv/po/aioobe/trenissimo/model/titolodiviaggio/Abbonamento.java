package it.unipv.po.aioobe.trenissimo.model.titolodiviaggio;

import it.unipv.po.aioobe.trenissimo.model.titolodiviaggio.enumeration.*;

public class Abbonamento implements ITitoloViaggio{
    private TipoTitoloViaggio tipo;
    private DurataAbbonamento  durata;
    private double prezzo;
    private String id;
    private String stazionePartenza;
    private String stazioneArrivo;

    public Abbonamento(DurataAbbonamento  durata, String stazionePartenza, String stazioneArrivo) {
        this.tipo = TipoTitoloViaggio.ABBONAMENTO;
        this.durata = durata;
        this.id = "AB" + System.currentTimeMillis();
        this.stazionePartenza = stazionePartenza;
        this.stazioneArrivo = stazioneArrivo;
    }

    //getter e setter
    public String getStazionePartenza() {
        return stazionePartenza;
    }

    public void setStazionePartenza(String stazionePartenza) {
        this.stazionePartenza = stazionePartenza;
    }

    public String getStazioneArrivo() {
        return stazioneArrivo;
    }

    public void setStazioneArrivo(String stazioneArrivo) {
        this.stazioneArrivo = stazioneArrivo;
    }

    @Override
    public TipoTitoloViaggio getTipo() {
        return this.tipo;
    }

    public DurataAbbonamento  getDurata() {
        return this.durata;
    }

    @Override
    public double getPrezzo() {
        return this.prezzo; //da calcolare?
    }

    @Override
    public String getId() {
        return this.id;
    }

    public void setDurata(DurataAbbonamento  durata) {
        this.durata = durata;
    }

    @Override
    public void setPrezzo(double prezzo) {
        // todo
    }
}
