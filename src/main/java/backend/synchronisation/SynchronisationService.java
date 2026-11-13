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
    private final LieuRepository lieuRepo;

    public SynchronisationService(ImporteurDataTourisme importeurDataTourisme, ImporteurBasilic importeurBasilic, JournalSynchroRepository repo, LieuRepository lieuRepo) {
        this.importeurDataTourisme = importeurDataTourisme;
        this.importeurBasilic = importeurBasilic;
        this.repo = repo;
        this.lieuRepo = lieuRepo;
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
            e.printStackTrace();
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
        List<String> erreurs = new ArrayList<>(resImport.getErrors());
        for(Lieu l : resImport.getLieux()){
            try {
                Lieu existant = trouverExistant(lieuRepo, l);
                if (existant != null) {
                    fusionner(existant, l);
                    nbMisesAJour++;
                }
                else if(lieuRepo.enregistrerLieu(l)){
                        nbAjouts++;
                }
            }    
            catch (Exception e) {
                String msg = e.getMessage();
                if (msg != null && msg.length() > 200) msg = msg.substring(0, 200);
                erreurs.add(l.getId() + " : " + msg);
            }
        }
        int nbLignesLues = resImport.getNbLignesLues();
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
            lieuRepo.save(existant);
        }
    }


    private Lieu trouverExistant(LieuRepository repoSource, Lieu l){
        if (l.getId() == null) {
            return null;
        }   
        return repoSource.trouverParId(l.getId());
    }
}
