package backend.accesdonnees;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import backend.circuits.Circuit;

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
        return findAll().removeIf(
            c -> c.getCodePartage().equals(circuit.getCodePartage())
        );
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
            .filter(c -> c.getEtapes().stream()
                    .anyMatch(e -> code.equals(
                            e.getLieu().getAdresse().getCodePostal()
                    )))
            .toList();
    }


}
