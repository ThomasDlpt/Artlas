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
        JournalSynchro JdataTourisme = synchronsiserDataTourisme();
        if(JdataTourisme != null){
            res.add(JdataTourisme);
        }
        JournalSynchro JBasilic = synchronsiserBasilic();
        if(JBasilic != null){
            res.add(JBasilic);
        }
        
        return res;
    }

    //JE PENSE QU'IL FAUT FAIRE 2 METHODES SYNCHRONISERSOURCE (1 POUR CHAQUE IMPORTEUR)
    public JournalSynchro synchronsiserDataTourisme(){
        Source source = importeurDataTourisme.getSource();
        JournalSynchro dernier = repo.trouverDernier(source);
        LocalDateTime derniereSynchro = dernier.getDateSynchro();
        LocalDateTime now = LocalDateTime.now();

        if (dernier != null && now.isBefore(dernier.getDateSynchro().plusMonths(1))) {
            throw new DateTimeException("délai synchro < à 1 mois");           
        }

        ResultatImport resImport = importeurDataTourisme.importer();

        int nbLignesLues = resImport.getNbLignesLues();
        List<String> erreurs = resImport.getErrors();
        int nbRejets = erreurs.size();
        int nbAjouts = resImport.getNbLignesLues() - dernier.getNbLignesLues();

        StatutSynchro statut;
        if (nbRejets == 0) {
            statut = StatutSynchro.SUCCES;
        }
        else if(nbAjouts != 0){
            statut = StatutSynchro.PARTIEL;
        }
        else{
            statut = StatutSynchro.ECHEC;
            //SI LA SYNCHRO EST UN ECHEC, EST-CE QU'ON RETENTE DIRECT APRES?
        }

        JournalSynchro res = new JournalSynchro("SYNC_DATA_T: "+ now.toString(), source, now, nbAjouts, nbRejets, nbLignesLues, statut, erreurs);
        repo.enregistrer(res);
        return res;
        /* */
        //SYNCHRONISER 1 FOIS PAR MOIS, DONC UTILISER DateTime ET APPELER importer() QUI NOUS DONNE erreurs (ET IMPLICITEMENT nbRejets, nbAjouts)
        //POUR statut, statut = StatutSynchro.SUCCES si nbRejets == 0, statut = StatutSynchro.PARTIEL si nbRejets et (nbAjouts ou nbMisesAJour) != 0, et statut = StatutSynchro.PARTIEL.ECHEC si nbAjouts et nbMisesAJour == 0
        //COMMENTAIRE YOURI: PAS SCALABLE NI OPTI D'AVOIR DES IDENTIFIANTS STRING.
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
