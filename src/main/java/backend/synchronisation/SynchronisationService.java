package backend.synchronisation;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import backend.accesdonnees.CommuneRepository;
import backend.lieux.Lieu;
import backend.lieux.Source;
import backend.accesdonnees.JournalSynchroRepository;

public class SynchronisationService {
    private double seulDoublonMetres;
    private Importeur importeurDataTourisme;
    private Importeur importeurBasilic;
    private JournalSynchroRepository repo;

    public List<JournalSynchro> synchroniser(){
        List<JournalSynchro> res = new ArrayList<>();
        JournalSynchro jDataTourisme = synchronsiserSource(importeurDataTourisme);
        JournalSynchro jBasilic = synchronsiserSource(importeurBasilic);
        if (jDataTourisme != null) {
            res.add(jDataTourisme);
        }
        if (jBasilic != null) {
            res.add(jBasilic);
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
                "SYNC_DATA_T: " + now.toString(), source, now, nbAjouts, nbRejets, nbLignesLues, statut, erreurs
        );
        repo.enregistrer(res);
        return res;
    
        /* */
        //SYNCHRONISER 1 FOIS PAR MOIS, DONC UTILISER DateTime ET APPELER importer() QUI NOUS DONNE erreurs (ET IMPLICITEMENT nbRejets, nbAjouts)
        //POUR statut, statut = StatutSynchro.SUCCES si nbRejets == 0, statut = StatutSynchro.PARTIEL si nbRejets et (nbAjouts ou nbMisesAJour) != 0, et statut = StatutSynchro.PARTIEL.ECHEC si nbAjouts et nbMisesAJour == 0
        //COMMENTAIRE YOURI: PAS SCALABLE NI OPTI D'AVOIR DES IDENTIFIANTS STRING.
    }

    
    private boolean valider(Lieu lieu){
        return false;
    }

    private void fusionner(Lieu existant, Lieu nouveau){
        
    }
  

}
