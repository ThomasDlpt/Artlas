package backend.recherche;

public class Bbox {

    private Double latMin;
    private Double latMax;
    private Double lonMin;
    private Double lonMax;

    public Bbox(Double latMin, Double latMax, Double lonMin, Double lonMax) {
        this.latMin = latMin;
        this.latMax = latMax;
        this.lonMin = lonMin;
        this.lonMax = lonMax;
    }

    public Double getLatMin() {
        return latMin;
    }

    public Double getLatMax() {
        return latMax;
    }

    public Double getLonMin() {
        return lonMin;
    }

    public Double getLonMax() {
        return lonMax;
    }

    public Boolean contient(double lat, double lon){
        return lat >= latMin && lat <= latMax && lon >= lonMin && lon <= lonMax;
    }
}
