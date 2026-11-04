package backend.recherche;

import backend.lieux.TypeLieu;

import java.util.ArrayList;
import java.util.List;

public class FiltreRecherche {

    private List<TypeLieu> types;
    private String codeDepartment;
    private String label;
    private String texte;

    public FiltreRecherche(List<TypeLieu> types, String codeDepartment, String label, String texte) {
        this.types =  types != null ? new ArrayList<>(types) : new ArrayList<>();
        this.codeDepartment = codeDepartment;
        this.label = label;
        this.texte = texte;
    }

    public List<TypeLieu> getTypes() {
        return types;
    }

    public String getCodeDepartment() {
        return codeDepartment;
    }

    public String getTexte() {
        return texte;
    }

    public String getLabel() {
        return label;
    }
}
