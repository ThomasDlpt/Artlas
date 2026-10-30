package backend.synchronisation;

import java.util.ArrayList;
import java.util.List;

import backend.lieux.Lieu;
import backend.lieux.Source;

public class ResultatImport {
    private final Source source;
    private final int nbLignesLues;
    private final List<String> erreurs;
    private final List<Lieu> lieux;

    public ResultatImport(Source source, int nbLignesLues, List<String> erreurs, List<Lieu> lieux){
        this.source = source;
        this.nbLignesLues = nbLignesLues;
        this.erreurs = List.copyOf(erreurs);
        this.lieux = List.copyOf(lieux);
    }

    public Source getSource(){
        return source;
    }

    public int getNbLignesLues(){
        return nbLignesLues;
    }

    public List<String> getErrors(){
        return erreurs;
    }
    
    public List<Lieu> getLieux(){
        return lieux;
    }
}
