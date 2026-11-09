package backend.synchronisation;

import java.io.FileInputStream;
import java.io.IOException;
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
        
        
        return new ArrayList<>();
    }

    //JE PENSE QU'IL FAUT FAIRE 2 METHODES SYNCHRONISERSOURCE (1 POUR CHAQUE IMPORTEUR)
    public JournalSynchro synchronsiserDataTourisme(){
        Source source = importeurDataTourisme.getSource();
        JournalSynchro dernier = repo.trouverDernier(source);
        LocalDateTime derniereSynchro = dernier.getDateSynchro();
        
        ResultatImport res = importeurDataTourisme.importer();

        int nbLignesLues = res.getNbLignesLues();
        List<String> erreurs = res.getErrors();
        int nbRejets = erreurs.size();
        int nbAjouts = res.getNbLignesLues() - dernier.getNbLignesLues();


        /* */
        //SYNCHRONISER 1 FOIS PAR MOIS, DONC UTILISER DateTime ET APPELER importer() QUI NOUS DONNE erreurs (ET IMPLICITEMENT nbRejets, nbAjouts)
        //POUR statut, statut = StatutSynchro.SUCCES si nbRejets == 0, statut = StatutSynchro.PARTIEL si nbRejets et (nbAjouts ou nbMisesAJour) != 0, et statut = StatutSynchro.PARTIEL.ECHEC si nbAjouts et nbMisesAJour == 0
        return new JournalSynchro(null, source, null, nbAjouts, nbRejets, nbLignesLues, null, erreurs);

    }

    public JournalSynchro synchronsiserBasilic(){
        Source source = importeurBasilic.getSource();
        JournalSynchro dernier = repo.trouverDernier(source);

        return new JournalSynchro(null, source, null, 0, 0, 0, null, null);
    }
    private boolean valider(Lieu lieu){
        return false;
    }

    private void fusionner(Lieu existant, Lieu nouveau){
        
    }
  

}
