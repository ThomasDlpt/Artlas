package backend.accesdonnees;

import backend.lieux.Source;
import backend.synchronisation.JournalSynchro;

import java.util.ArrayList;
import java.util.List;

public class JournalSynchroRepository {

    private final List<JournalSynchro> journaux = new ArrayList<>();

    public void enregistrer(JournalSynchro journal) {
        if (journal != null){
            journaux.add(journal);
        }
    }

    public JournalSynchro trouverDernier (Source src){
        JournalSynchro dernier = null;
        for (JournalSynchro j : journaux) {
            if (j.getSource() == src) {
                if (dernier == null || j.getDateSynchro().isAfter(dernier.getDateSynchro())) {
                    dernier = j;
                }
            }
        }
        return dernier;
    }

}
