package backend.geographie;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.util.Objects;

/**
 * Département français (ex : code "35", nom "Ille-et-Vilaine"), rattaché à une région.
 */
@Entity
public class Departement {
    @Id
    private String code;
    private String nom;
    @ManyToOne(cascade = CascadeType.MERGE)
    private Region region;

    public Departement() {
    }

    public Departement(String code, String nom, Region region) {
        if (code == null || code.isBlank() || nom == null || nom.isBlank()) {
            throw new IllegalArgumentException("code et nom du département obligatoires");
        }
        if (region == null) {
            throw new IllegalArgumentException("un département doit appartenir à une région");
        }
        this.code = code;
        this.nom = nom;
        this.region = region;
    }

    public String getCode() {
        return code;
    }

    public String getNom() {
        return nom;
    }

    public Region getRegion() {
        return region;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Departement)) return false;
        return code.equals(((Departement) o).code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return nom + " (" + code + ")";
    }
}
