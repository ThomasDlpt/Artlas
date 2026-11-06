package backend.synchronisation;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import backend.accesdonnees.CommuneRepository;
import backend.lieux.Lieu;
import backend.lieux.Source;

public class SynchronisationService {
    private double seulDoublonMetres;
    private Importeur importeur;

    public List<JournalSynchro> synchroniser(){
        
        
        return new ArrayList<>();
    }

    //JE PENSE QU'IL FAUT FAIRE 2 METHODES SYNCHRONISERSOURCE (1 POUR CHAQUE IMPORTEUR)
    public JournalSynchro synchronsiserSource(){
        Source source = importeur.getSource();
        //SYNCHRONISER 1 FOIS PAR MOIS, DONC UTILISER DateTime ET APPELER importer() QUI NOUS DONNE erreurs (ET IMPLICITEMENT nbRejets, nbAjouts)
        //POUR statut, statut = StatutSynchro.SUCCES si nbRejets == 0, statut = StatutSynchro.PARTIEL si nbRejets et (nbAjouts ou nbMisesAJour) != 0, et statut = StatutSynchro.PARTIEL.ECHEC si nbAjouts et nbMisesAJour == 0
        return new JournalSynchro(null, null, null, null, 0, 0, 0, null, null);

    }

    private boolean valider(Lieu lieu){
        return false;
    }

    private void fusionner(Lieu existant, Lieu nouveau){
        
    }
  

}
