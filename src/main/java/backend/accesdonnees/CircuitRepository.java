package backend.accesdonnees;

import java.util.List;

import backend.lieux.Source;
import backend.synchronisation.JournalSynchro;

public class CircuitRepository {
    private List<JournalSynchro> journaux;
    
    public void enregister(JournalSynchro journal){
        journaux.add(journal);
    }

    public JournalSynchro trouverDernier(Source source){
        return journaux.getLast();
    }
}
