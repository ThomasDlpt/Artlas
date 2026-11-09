package backend.consultation;

import backend.recherche.Bbox;
import backend.recherche.FiltreRecherche;
import backend.lieux.Lieu;

import java.util.List;

public class LieuService  {

    public List<Lieu> rechercher(FiltreRecherche filtre){
        throw new UnsupportedOperationException("À implémenter");

    }

    public Lieu selectionner (String idLieu){
        throw new UnsupportedOperationException("À implémenter");


    }

    public List<Lieu>  rechercherDansZone(Bbox zone, FiltreRecherche filtre){
        throw new UnsupportedOperationException("À implémenter");


    }
}
