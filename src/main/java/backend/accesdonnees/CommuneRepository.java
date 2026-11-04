package backend.accesdonnees;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import backend.geographie.Commune;

@Component 
public class CommuneRepository {    
    
    private Map<String, Commune> m;
    
    public CommuneRepository(){
        this.m = new LinkedHashMap<>();
    }

    public Commune trouverParCodeInsee(String codeInsee){
        /*for(String c : m.keySet()){
            if (c.equals(code)) {
                return m.get(c);
            }
        }
        return null;*/
        return m.get(codeInsee);
    }

    public Commune trouverParCodePostalEtNom(String codePostal, String nom){
        for(String c : m.keySet()){
            Commune commune = m.get(c);
            if (commune.getNom().equals(nom)) {
                return commune;
            }
        }
        return null;
    }

    public Commune renvoieOuCreeCommune(String codeInsee, String nom){
        
        if (codeInsee == null || codeInsee.equals("")|| nom == null || nom.equals("")) {
            throw new IllegalArgumentException("paramètre null ou invalide");
        }
        
        Commune c = m.get(codeInsee);
        if (c != null) {
            return c;
        }
        else{
            Commune newC = new Commune(codeInsee, nom);
            m.put(codeInsee, newC);
            return newC;
        }
    }

    public int tailleRepo(){
        return m.size();
    }
    public static void main(String[] args) {
        CommuneRepository repo = new CommuneRepository();
        Commune c1 = repo.renvoieOuCreeCommune("79126", "Les Fosses");
        System.err.println(c1.getCodeInsee() + " - " + c1.getNom());
    }
}
