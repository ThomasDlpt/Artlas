package backend.synchronisation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import backend.accesdonnees.LieuRepository;
import backend.lieu.Lieu;
import backend.lieu.Source;
import backend.accesdonnees.JournalSynchroRepository;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


@Service
@EnableScheduling
public class SynchronisationService {
    private final Importeur importeurDataTourisme;
    private final Importeur importeurBasilic;
    private JournalSynchroRepository repo;
    private final LieuRepository repoDataTourism =  new LieuRepository();
    private final LieuRepository repoBasilic = new LieuRepository();

    public SynchronisationService(Importeur importeurDataTourisme, Importeur importeurBasilic, JournalSynchroRepository repo) {
        this.importeurDataTourisme = importeurDataTourisme;
        this.importeurBasilic = importeurBasilic;
        this.repo = repo;
    }

    @Scheduled(fixedRate = 86_400_000)
    public List<JournalSynchro> synchroniser(){
        List<JournalSynchro> res = new ArrayList<>();
        try {
            JournalSynchro jDataTourisme = synchroniserSource(importeurDataTourisme);
            if (jDataTourisme != null) {
                res.add(jDataTourisme);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        try {


            JournalSynchro jBasilic = synchroniserSource(importeurBasilic);
            if (jBasilic != null) {
                res.add(jBasilic);
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        return res;
    }

    
    public JournalSynchro synchroniserSource(Importeur importeur) {
        Source source = importeur.getSource();
        JournalSynchro dernier = repo.trouverDernier(source);
        LocalDateTime now = LocalDateTime.now();

        // On bloque seulement si une synchro réussie (non ECHEC) a moins d'1 mois
        if (dernier != null && dernier.getStatut() != StatutSynchro.ECHEC
                && now.isBefore(dernier.getDateSynchro().plusMonths(1))) {
            return null;
        }

        int nbAjouts = 0;
        int nbMisesAJour = 0;
        ResultatImport resImport = importeur.importer();
        LieuRepository repoSource = switch(source){
            case BASILIC -> repoBasilic;
            case DATATOURISME -> repoDataTourism;
            default -> throw new IllegalStateException("Source inconnue: " + source);  
        };
        for(Lieu l : resImport.getLieux()){
            Lieu existant = trouverExistant(repoSource, l);
            if (existant != null) {
                fusionner(existant, l);
                nbMisesAJour++;
            }
            else{
                if(repoSource.enregistrerLieu(l)){
                    nbAjouts++;
                }
               
            }
        }
        int nbLignesLues = resImport.getNbLignesLues();
        List<String> erreurs = resImport.getErrors();
        int nbRejets = erreurs.size();


        StatutSynchro statut;
        if (nbRejets == 0) {
            statut = StatutSynchro.SUCCES;
        } else if (nbAjouts != 0 || nbMisesAJour != 0) {
            statut = StatutSynchro.PARTIEL;
        } else {
            statut = StatutSynchro.ECHEC;
        }

        JournalSynchro res = new JournalSynchro(
                "SYNC_" + source + ": " + now.toString(), source, now, nbAjouts, nbRejets, nbLignesLues, statut, erreurs
        );
        repo.enregistrer(res);
        return res;
    
    }

    private void fusionner(Lieu existant, Lieu nouveau){
        if (existant != null && nouveau != null) {
            existant.mettreAJour(nouveau);
        }
    }

    public LieuRepository getRepoDataTourism() {
        return repoDataTourism;
    }

    public LieuRepository getRepoBasilic() {
        return repoBasilic;
    }

    private Lieu trouverExistant(LieuRepository repoSource, Lieu l){
        if (l.getId() == null) {
            return null;
        }   
        return repoSource.trouverParId(l.getId());
    }
}
