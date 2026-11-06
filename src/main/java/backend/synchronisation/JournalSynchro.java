package backend.synchronisation;

import java.time.LocalDateTime;
import java.util.List;

import backend.lieux.Source;

public class JournalSynchro {
    private String id;
    private Source source;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private int nbAjouts;
    private int nbMiseAJour;
    private int nbRejets;
    private StatutSynchro statut;
    private List<String> erreurs;


    public JournalSynchro(
        String id,
        Source source,
        LocalDateTime dateDebut,
        LocalDateTime dateFin,
        int nbAjouts,
        int nbMiseAJour,
        int nbRejets,
        StatutSynchro statut,
        List<String> erreurs
    ){
        this.id = id;
        this.source = source;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.nbAjouts = nbAjouts;
        this.nbMiseAJour = nbMiseAJour;
        this.nbRejets = nbRejets;
        this.statut = statut;
        this.erreurs = erreurs;
    }

    public String getId(){
        return id;
    }

    public Source getSource(){
        return source;
    }

    public LocalDateTime getDateDebut(){
        return dateDebut;
    }

    public LocalDateTime getDateFin(){
        return dateFin;
    }

    public int getNbAjouts(){
        return nbAjouts;
    }

    public int getNbMisesAjour(){
        return nbMiseAJour;
    }

    public int getNbRejets(){
        return nbRejets;
    }

    public StatutSynchro getStatut(){
        return statut;
    }

    public List<String> getErreurs(){
        return erreurs;
    }
}
