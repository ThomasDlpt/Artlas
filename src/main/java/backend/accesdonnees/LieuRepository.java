package backend.accesdonnees;

import backend.lieux.Lieu;
import backend.lieux.Source;
import backend.lieux.TypeLieu;
import backend.recherche.Bbox;
import backend.recherche.FiltreRecherche;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LieuRepository {

    private ArrayList<Lieu> lieux = new ArrayList<>();

    public void enregistrerLieu(Lieu lieu) {
        if (lieu != null && !lieux.contains(lieu)) {
            lieux.add(lieu);
        }
    }

    public Lieu trouverParId(String id){
        for (Lieu lieu : lieux) {
            if (lieu.getId().equals(id)) {
                return lieu;
            }
        }
        return null;
    }

    public Lieu trouverParSource(Source src, String idExterne){
        for (Lieu lieu : lieux) {
            for (SourceLieu source : lieu.getSource()){
                if (source.getSource() == src && source.getIdExterne().equals(idExterne)){
                    return lieu;
                }
            }
        }
        return null;
    }

    public List<Lieu> trouverParDepartement(String code){
        List<Lieu> res = new ArrayList<>();
        for (Lieu lieu : lieux) {
            if (lieu.getAdresse().getCommune().getDepartement().getCode().equals(code)){
                res.add(lieu);
            }
        }
        return res;
    }

    public  List<Lieu> trouverParType(TypeLieu type){
        List<Lieu> res = new ArrayList<>();
        for (Lieu lieu : lieux) {
            if (lieu.getType() == type){
                res.add(lieu);
            }
        }
        return res;
    }

    public List<Lieu> trouverDansZone(Bbox zone, FiltreRecherche filtre){
        List<Lieu> res = new ArrayList<>();
        for (Lieu lieu : lieux) {
            double lat = lieu.getAdresse().getLatitude();
            double lon = lieu.getAdresse().getLongitude();
            if (zone.contient(lat,lon) && filtreOk(lieu, filtre)) {
                res.add(lieu);
            }
        }
        return res;
    }

    public boolean filtreOk(Lieu lieu, FiltreRecherche filtre){
        if (filtre == null) return true;

        boolean typeOk = filtre.getTypes().isEmpty() || filtre.getTypes().contains(lieu.getType());
        boolean codeDepOk = filtre.getCodeDepartment() == null || lieu.getAdresse().getCommune().getDepartement().getCode().equals(filtre.getCodeDepartment());
        boolean labelOk =  filtre.getLabel() == null || lieu.getLabels().contains(filtre.getLabel());
        boolean texteOk =  filtre.getTexte() == null || lieu.getNom().toLowerCase().contains(filtre.getTexte().toLowerCase());

        return typeOk && codeDepOk && labelOk && texteOk;
    }

    public List<Lieu> trouverProche(Double latitude, Double longitude, Double rayon, FiltreRecherche filtre){
        List<Lieu> res = new ArrayList<>();
        for (Lieu lieu : lieux) {
            double lat = lieu.getAdresse().getLatitude();
            double lon = lieu.getAdresse().getLongitude();
            if (calculDistance(latitude, longitude, lat, lon) <= rayon) {
                res.add(lieu);
            }
        }
        return res;
    }

    public double calculDistance (double lat1, double lon1, double lat2, double lon2){
        double r = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return r*c;
    }

}
