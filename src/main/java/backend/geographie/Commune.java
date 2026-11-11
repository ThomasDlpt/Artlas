package backend.geographie;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.util.Objects;

/**
 * Commune française identifiée par son code INSEE (ex : "35238" pour Rennes).
 * Le département peut être renseigné après la création (cas de CommuneRepository).
 */
@Entity
public class Commune {
    @Id
    private String codeInsee;
    private String nom;
    @ManyToOne(cascade = CascadeType.MERGE)
    private Departement departement;

    public Commune() {
    }

    /** Constructeur utilisé par CommuneRepository : le département est ajouté ensuite. */
    public Commune(String codeInsee, String nom) {
        this(codeInsee, nom, null);
    }

    public Commune(String codeInsee, String nom, Departement departement) {
        if (codeInsee == null || codeInsee.isBlank() || nom == null || nom.isBlank()) {
            throw new IllegalArgumentException("code INSEE et nom de la commune obligatoires");
        }
        this.codeInsee = codeInsee;
        this.nom = nom;
        this.departement = departement;
    }

    public String getCodeInsee() {
        return this.codeInsee;
    }

    public String getNom() {
        return this.nom;
    }

    /** Peut renvoyer null si le département n'a pas encore été rattaché. */
    public Departement getDepartement() {
        return departement;
    }

    public void setDepartement(Departement departement) {
        this.departement = departement;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Commune)) return false;
        return codeInsee.equals(((Commune) o).codeInsee);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codeInsee);
    }

    @Override
    public String toString() {
        return nom + " (" + codeInsee + ")";
    }
}
