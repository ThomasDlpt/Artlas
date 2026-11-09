package backend.consultation;

import backend.accesdonnees.LieuRepository;
import backend.recherche.Bbox;
import backend.recherche.FiltreRecherche;
import backend.lieux.Lieu;

import java.util.List;

public class LieuService  {
    private final LieuRepository repo;

    public LieuService(LieuRepository repo){
        if(repo ==null){
            throw new IllegalArgumentException("repository null");
        }
        this.repo = repo;
    }

    public List<Lieu> rechercher(FiltreRecherche filtre){
        return repo.trouverParFiltre(filtre);
    }

    public Lieu selectionner (String idLieu){
        if( idLieu == null || idLieu.isEmpty() ){
            throw new IllegalArgumentException("id est null ou vide");
        }
        return  repo.trouverParId(idLieu);


    }

    public List<Lieu>  rechercherDansZone(Bbox zone, FiltreRecherche filtre){
        if(zone == null){
            throw new IllegalArgumentException("zone null");
        }
        return repo.trouverDansZone(zone,filtre);


    }
}
