package backend.synchronisation;

import java.util.ArrayList;
import java.util.List;

import backend.lieux.Source;

public class ResultatImport {
    private Source source;
    private int nbLignesLues;
    private List<String> errors;

    public ResultatImport(Source source, int nbLignesLues, List<String> errors){
        this.source = source;
        this.nbLignesLues = nbLignesLues;
        this.errors = List.copyOf(errors);
    }

    public Source getSource(){
        return source;
    }

    public int getNbLignesLues(){
        return nbLignesLues;
    }

    public List<String> getErrors(){
        return errors;
    }
    
}
