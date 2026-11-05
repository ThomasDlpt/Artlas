package backend.circuits;

import java.util.List;

public class Circuit {
    private String id;
    private String nom;
    private String codePartage;
    private double distanceKm;
    private int dureeMinutes;
    private List<EtapeCircuit> etapes;
    
    public Circuit(){

    }

    public String getId(){
        return id;
    }

    public String getNom(){
        return nom;
    }

    public String getCodePartage(){
        return codePartage;
    }

    public double getDistanceKm(){
        return distanceKm;
    }

    public int getDureeMinutes(){
        return dureeMinutes;
    }
}
