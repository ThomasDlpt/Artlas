package backend.synchronisation;

import java.time.LocalDateTime;
import java.util.List;

import backend.lieux.Source;

public class JournalSynchro {
    private String id;
    private Source source;
    private LocalDateTime dateSynchro;
    private int nbAjouts;
    private int nbRejets;
    private int nbLignesLues;
    private StatutSynchro statut;
    private List<String> erreurs;


    public JournalSynchro(
        String id,
        Source source,
        LocalDateTime dateSynchro,
        int nbAjouts,
        int nbRejets,
        int nbLignesLues,
        StatutSynchro statut,
        List<String> erreurs
    ){
        this.id = id;
        this.source = source;
        this.dateSynchro = dateSynchro;
        this.nbAjouts = nbAjouts;
        this.nbRejets = nbRejets;
        this.nbLignesLues = nbLignesLues;
        this.statut = statut;
        this.erreurs = erreurs;
    }

    public String getId(){
        return id;
    }

    public Source getSource(){
        return source;
    }

    public LocalDateTime getDateSynchro(){
        return dateSynchro;
    }

    public int getNbAjouts(){
        return nbAjouts;
    }

    public int getNbRejets(){
        return nbRejets;
    }

    public int getNbLignesLues(){
        return nbLignesLues;
    }

    public StatutSynchro getStatut(){
        return statut;
    }

    public List<String> getErreurs(){
        return erreurs;
    }
}
