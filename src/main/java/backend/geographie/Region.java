package backend.geographie;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.util.Objects;

/**
 * Région administrative française (ex : code "53", nom "Bretagne").
 */
@Entity
public class Region {
    @Id
    private String code;
    private String nom;

    public Region() {
    }

    public Region(String code, String nom) {
        if (code == null || code.isBlank() || nom == null || nom.isBlank()) {
            throw new IllegalArgumentException("code et nom de la région obligatoires");
        }
        this.code = code;
        this.nom = nom;
    }

    public String getCode() {
        return code;
    }

    public String getNom() {
        return nom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Region)) return false;
        return code.equals(((Region) o).code);
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
