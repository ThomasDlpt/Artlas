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
    public final LieuRepository repoDataTourism =  new LieuRepository();
    public final LieuRepository repoBasilic = new LieuRepository();

    public SynchronisationService(Importeur importeurDataTourisme, Importeur importeurBasilic, JournalSynchroRepository repo) {
        this.importeurDataTourisme = importeurDataTourisme;
        this.importeurBasilic = importeurBasilic;
        this.repo = repo;
    }

    @Scheduled(fixedRate = 86_400_000)
    public List<JournalSynchro> synchroniser(){
        List<JournalSynchro> res = new ArrayList<>();
        try {
            JournalSynchro jDataTourisme = synchronsiserSource(importeurDataTourisme);
            if (jDataTourisme != null) {
                res.add(jDataTourisme);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        try {


            JournalSynchro jBasilic = synchronsiserSource(importeurBasilic);
            if (jBasilic != null) {
                res.add(jBasilic);
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        return res;
    }

    //JE PENSE QU'IL FAUT FAIRE 2 METHODES SYNCHRONISERSOURCE (1 POUR CHAQUE IMPORTEUR)
 
    public JournalSynchro synchronsiserSource(Importeur importeur) {
        Source source = importeur.getSource();
        JournalSynchro dernier = repo.trouverDernier(source);
        LocalDateTime now = LocalDateTime.now();

        // On bloque seulement si une synchro réussie (non ECHEC) a moins d'1 mois
        if (dernier != null && dernier.getStatut() != StatutSynchro.ECHEC
                && now.isBefore(dernier.getDateSynchro().plusMonths(1))) {
            return null;
        }

        ResultatImport resImport = importeur.importer();

        int nbLignesLues = resImport.getNbLignesLues();
        List<String> erreurs = resImport.getErrors();
        int nbRejets = erreurs.size();

        int anciennesLignes = 0;
        if (dernier != null) {
            anciennesLignes = dernier.getNbLignesLues();
        }
        int nbAjouts = nbLignesLues - anciennesLignes;

        StatutSynchro statut;
        if (nbRejets == 0) {
            statut = StatutSynchro.SUCCES;
        } else if (nbAjouts != 0) {
            statut = StatutSynchro.PARTIEL;
        } else {
            statut = StatutSynchro.ECHEC;
        }

        JournalSynchro res = new JournalSynchro(
                "SYNC_" + source + ": " + now.toString(), source, now, nbAjouts, nbRejets, nbLignesLues, statut, erreurs
        );
        repo.enregistrer(res);
        return res;
    
        /* */
        //SYNCHRONISER 1 FOIS PAR MOIS, DONC UTILISER DateTime ET APPELER importer() QUI NOUS DONNE erreurs (ET IMPLICITEMENT nbRejets, nbAjouts)
        //POUR statut, statut = StatutSynchro.SUCCES si nbRejets == 0, statut = StatutSynchro.PARTIEL si nbRejets et (nbAjouts ou nbMisesAJour) != 0, et statut = StatutSynchro.PARTIEL.ECHEC si nbAjouts et nbMisesAJour == 0
        //COMMENTAIRE YOURI: PAS SCALABLE NI OPTI D'AVOIR DES IDENTIFIANTS STRING.
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
}
