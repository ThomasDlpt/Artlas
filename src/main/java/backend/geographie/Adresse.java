package backend.geographie;

import jakarta.persistence.*;

/**
 * Adresse postale et position GPS d'un lieu, rattachée à une commune.
 */
@Embeddable
public class Adresse {
    private static final double RAYON_TERRE_KM = 6371.0;

    private String rue;          // peut être null (certains lieux n'ont pas de rue précise)
    private String codePostal;
    @ManyToOne 
    private Commune commune;
    private double latitude;
    private double longitude;

    protected Adresse() {
    }

    public Adresse(String rue, String codePostal, Commune commune, double latitude, double longitude) {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("latitude hors de [-90, 90] : " + latitude);
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("longitude hors de [-180, 180] : " + longitude);
        }
        if (commune == null) {
            throw new IllegalArgumentException("une adresse doit être rattachée à une commune");
        }
        this.rue = rue;
        this.codePostal = codePostal;
        this.commune = commune;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    

    public String getRue() {
        return rue;
    }

    public String getCodePostal() {
        return codePostal;
    }

    public Commune getCommune() {
        return commune;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    /**
     * Distance à vol d'oiseau (formule de Haversine) vers une autre adresse, en kilomètres.
     */
    public double distanceKm(Adresse autre) {
        if (autre == null) {
            throw new IllegalArgumentException("adresse de destination null");
        }
        double dLat = Math.toRadians(autre.latitude - this.latitude);
        double dLon = Math.toRadians(autre.longitude - this.longitude);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(autre.latitude))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return RAYON_TERRE_KM * c;
    }

    @Override
    public String toString() {
        return (rue != null ? rue + ", " : "") + (codePostal != null ? codePostal + " " : "") + commune.getNom();
    }
}
