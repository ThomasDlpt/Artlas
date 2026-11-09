package backend.geographie;

public class Adresse {
    private String rue;
    private String codePostal;
    private Commune commune;
    private double latitude;
    private double longitude;


    public Adresse(String rue, String codePostal, Commune commune, double latitude, double longitude){
        this.rue = rue;
        this.codePostal = codePostal;
        this.commune = commune;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getRue(){
        return rue;
    }

    public String getCodePostal(){
        return codePostal;
    }

    public Commune getCommune(){
        return commune;
    }

    public double getLatitude(){
        return latitude;
    }

    public double getLongitude(){
        return longitude;
    }
}
