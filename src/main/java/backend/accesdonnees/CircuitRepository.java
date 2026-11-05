package backend.accesdonnees;

import java.util.ArrayList;
import java.util.List;

import backend.circuits.Circuit;
import backend.lieux.Source;
import backend.synchronisation.JournalSynchro;

public class CircuitRepository {
    private List<Circuit> circuits;
    
    public CircuitRepository(){
        this.circuits = new ArrayList<>();
    }
    public void enregister(Circuit circuit){
        circuits.add(circuit);
    }

    public void supprimer(Circuit circuit){
        circuits.remove(circuit);
    }

    public Circuit trouverParCodePartage(String code){
        if (code == null || code.equals("")) {
            throw new IllegalArgumentException("code null ou invalide");
        }
        for(Circuit c : circuits){
            String codeC = c.getCodePartage();
            if (code.equals(codeC)) {
                return c;
            }
        }
        return null;
    }

    public List<Circuit> trouverParRegion(String code){
        //Récupérer les objets Lieu de etapes (qui est une Liste de EtapeCircuit), puis récupérer l'adresse et faire le filtrage sur ça.
        
        return new ArrayList<>();
    }

    public List<Circuit> trouverParDepartement(String code){
        //Récupérer les objets Lieu de etapes (qui est une Liste de EtapeCircuit), puis récupérer l'adresse et faire le filtrage sur ça.

        
        return new ArrayList<>();
    }


}
