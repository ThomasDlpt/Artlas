package backend.accesdonnees;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import backend.circuits.Circuit;
import backend.geographie.Adresse;
import backend.lieu.Lieu;

@Repository 
public interface CircuitRepository extends JpaRepository<Circuit, String>{
    
    default boolean enregister(Circuit circuit){
        if (circuit == null || circuit.getId() == null) {
            return false;
        }
        if (trouverParCodePartage(circuit.getCodePartage()) != null) {
            return false;
        }
        save(circuit);
        return true;
    }
    

    default boolean supprimer(Circuit circuit){
        if (circuit == null || circuit.getId() == null || trouverParCodePartage(circuit.getCodePartage()) == null) {
            return false;
        }
        // findAll() renvoie une copie : la retirer ne supprimait rien en base. On supprime l'entité elle-même.
        delete(trouverParCodePartage(circuit.getCodePartage()));
        return true;
    }

    default Circuit trouverParCodePartage(String code){
        if (code == null || code.equals("")) {
            throw new IllegalArgumentException("code null ou invalide");
        }
        return findAll().stream().filter(c -> code.equals(c.getCodePartage())).findFirst().orElse(null);
    }

    /*default List<Circuit> trouverParRegion(String code){
        if (code == null || code.equals("")) {
            throw new IllegalArgumentException("code null ou invalide");
        }
        return findAll().stream().filter(c -> code.equals(c.getEtapes().forEach(e -> e.getLieu().getAdresse().getRe);))
    }
    MIS EN COMMENTAIRE CAR ADRESSE NE CONTIENT PAS D'ATTRIBUT REGION*/

    default List<Circuit> trouverParDepartement(String code){
       if (code == null || code.equals("")) {
            throw new IllegalArgumentException("code null ou invalide");
        } 
        return findAll().stream()
            .filter(c -> c.getEtapes().stream().anyMatch(e -> lieuDansDepartement(e.getLieu(), code)))
            .toList();
    }

    // vrai si le departement de la commune du lieu a ce code (faux si l'adresse est incomplete)
    private boolean lieuDansDepartement(Lieu lieu, String code){
        Adresse adresse = lieu.getAdresse();
        if (adresse == null || adresse.getCommune() == null || adresse.getCommune().getDepartement() == null) {
            return false;
        }
        return code.equals(adresse.getCommune().getDepartement().getCode());
    }


}
