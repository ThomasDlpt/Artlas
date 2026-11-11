package backend.accesdonnees;

import backend.lieu.Lieu;
import backend.lieu.Source;
import backend.lieu.TypeLieu;
import backend.recherche.Bbox;
import backend.recherche.FiltreRecherche;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository 
public interface LieuRepository extends JpaRepository<Lieu, String>{


    default boolean enregistrerLieu(Lieu lieu) {
        if (lieu == null || lieu.getId() == null) {
            return false;
        }
        // un lieu avec le meme id est deja enregistre
        if (trouverParId(lieu.getId()) != null) {
            return false;
        }
        save(lieu);
        return true;
    }

    default Lieu trouverParId(String id){
        return findById(id).orElse(null);
    }
    default List<Lieu> trouverParFiltre( FiltreRecherche filtre){
        return findAll().stream().filter(l -> filtreOk(l, filtre)).toList();
    }


    default List<Lieu> trouverParDepartement(String code){
        return findAll().stream().filter(l -> code.equals(codeDepartement(l))).toList();
    }

    default List<Lieu> trouverParType(TypeLieu type){
        return findAll().stream().filter(l -> l.getType() == type).toList();
    }

    default List<Lieu> trouverDansZone(Bbox zone, FiltreRecherche filtre){
        return findAll().stream()
            .filter(l -> l.getAdresse() != null)
            .filter(l -> zone.contient(l.getAdresse().getLatitude(), l.getAdresse().getLongitude()) && filtreOk(l, filtre))
            .toList();
    }

    default boolean filtreOk(Lieu lieu, FiltreRecherche filtre){
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

    default List<Lieu> trouverProche(Double latitude, Double longitude, Double rayon, FiltreRecherche filtre){
        return findAll().stream()
            .filter(l -> l.getAdresse() != null)
            .filter(l -> calculDistance(latitude, longitude, l.getAdresse().getLatitude(), l.getAdresse().getLongitude()) <= rayon && filtreOk(l, filtre))
            .toList();
    }

    static double calculDistance (double lat1, double lon1, double lat2, double lon2){
        double r = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return r*c;
    }

}
