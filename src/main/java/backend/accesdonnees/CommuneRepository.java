package backend.accesdonnees;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import backend.geographie.Commune;
import org.springframework.stereotype.Repository;

@Repository
public interface CommuneRepository extends JpaRepository<Commune, String> {

    default Commune trouverParCodeInsee(String codeInsee){
        if (codeInsee == null){
            return null;
        }
        return findById(codeInsee).orElse(null);
    }

    default Commune trouverParCodePostalEtNom(String codePostal, String nom){
        for(Commune c : findAll()){
            if (c.getNom().equals(nom)) {
                return c;
            }
        }
        return null;
    }

    default Commune renvoieOuCreeCommune(String codeInsee, String nom){
        
        if (codeInsee == null || codeInsee.equals("")|| nom == null || nom.equals("")) {
            throw new IllegalArgumentException("paramètre null ou invalide");
        }
        
        Commune c = trouverParCodeInsee(codeInsee);
        if (c != null) {
            return c;
        }
        else{
            Commune newC = new Commune(codeInsee, nom);
            return save(newC);
        }
    }

    default int tailleRepo(){
        return  (int) count();
    }

    /*
    public static void main(String[] args) {
        CommuneRepository repo = new CommuneRepository();
        Commune c1 = repo.renvoieOuCreeCommune("79126", "Les Fosses");
        System.err.println(c1.getCodeInsee() + " - " + c1.getNom());
    }
    */
}
