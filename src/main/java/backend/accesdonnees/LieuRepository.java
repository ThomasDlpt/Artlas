package backend.accesdonnees;

import backend.lieu.Lieu;
import backend.lieu.Source;
import backend.lieu.SourceLieu;
import backend.lieu.TypeLieu;
import backend.recherche.Bbox;
import backend.recherche.FiltreRecherche;

import java.util.ArrayList;
import java.util.List;


public class LieuRepository {

    private ArrayList<Lieu> lieux = new ArrayList<>();

    public boolean enregistrerLieu(Lieu lieu) {
        if (lieu == null || lieu.getId() == null) {
            return false;
        }
        // un lieu avec le meme id est deja enregistre
        if (trouverParId(lieu.getId()) != null) {
            return false;
        }
        lieux.add(lieu);
        return true;
    }

    public Lieu trouverParId(String id){
        for (Lieu lieu : lieux) {
            if (id.equals(lieu.getId())){
                return lieu;
            }
        }
        return null;
    }
    public List<Lieu> trouverParFiltre( FiltreRecherche filtre){
        List<Lieu> res = new ArrayList<>();
        for(Lieu lieu : lieux){
            if(filtreOk(lieu,filtre)){
                res.add(lieu);
            }
        }
        return res;
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
            String codeDep = codeDepartement(lieu);
            if (codeDep != null && codeDep.equals(code)){
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
            if (lieu.getAdresse() == null) continue;
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
        boolean codeDepOk = filtre.getCodeDepartment() == null || filtre.getCodeDepartment().equals(codeDepartement(lieu));
        boolean labelOk =  filtre.getLabel() == null || lieu.getLabels().contains(filtre.getLabel());
        boolean texteOk =  filtre.getTexte() == null || (lieu.getNom() != null && lieu.getNom().toLowerCase().contains(filtre.getTexte().toLowerCase()));

        return typeOk && codeDepOk && labelOk && texteOk;
    }

    // code du departement du lieu, ou null si l'adresse est incomplete
    private String codeDepartement(Lieu lieu){
        if (lieu.getAdresse() == null || lieu.getAdresse().getCommune() == null || lieu.getAdresse().getCommune().getDepartement() == null) {
            return null;
        }
        return lieu.getAdresse().getCommune().getDepartement().getCode();
    }

    public List<Lieu> trouverProche(Double latitude, Double longitude, Double rayon, FiltreRecherche filtre){
        List<Lieu> res = new ArrayList<>();
        for (Lieu lieu : lieux) {
            if (lieu.getAdresse() == null) continue;
            double lat = lieu.getAdresse().getLatitude();
            double lon = lieu.getAdresse().getLongitude();
            if (calculDistance(latitude, longitude, lat, lon) <= rayon && filtreOk(lieu,filtre)) {
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
