package backend.accesdonnees;

import backend.lieux.Lieu;
import backend.lieux.Source;
import backend.lieux.TypeLieu;

import java.util.ArrayList;
import java.util.List;

public class LieuRepository {

    private ArrayList<Lieu> lieux = new ArrayList<>();

    public void enregistrerLieu(Lieu lieu) {
        if (lieu != null && !lieux.contains(lieu)) {
            lieux.add(lieu);
        }
    }

    public Lieu trouverParSource(Source src, String idExterne){
        return null;
    }

    public List<Lieu> trouverParDepartement(String code){
        return null;
    }

    public  List<Lieu> trouverParType(TypeLieu type){
        return null;
    }

    public List<Lieu> trouverDansZone(BBox zone, FiltreRecherche filtre){
        return null;
    }

    public List<Lieu> trouverProche(Double latitude, Double longitude, Double rayon, FiltreRecherche filtre){
        return null;
    }
}
