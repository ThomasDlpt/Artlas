package backend.synchronisation;

import java.util.ArrayList;
import java.util.List;

import backend.lieux.Lieu;

public class SynchronisationService {
    private double seulDoublonMetres;
    
    public List<JournalSynchro> synchroniser(){
        
        
        return new ArrayList<>();
    }

    public JournalSynchro synchronsiserSource(){

        
        return new JournalSynchro(null, null, null, null, 0, 0, 0, null, null);

    }

    private boolean valider(Lieu lieu){
        return false;
    }

    private void fusionner(Lieu existant, Lieu nouveau){
        
    }
}
