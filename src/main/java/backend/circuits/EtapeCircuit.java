package backend.circuits;

import backend.lieu.Lieu;
import jakarta.persistence.*;

@Entity
public class EtapeCircuit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)   // auto-increment
    private Long id;

    @ManyToOne(optional = false)
    private Lieu lieu;
    private int numOrdre;
    private int dureeVisiteMin;

    protected EtapeCircuit() {}                            // required by JPA

    public EtapeCircuit(Lieu lieu, int numOrdre, int dureeVisiteMin) {
        if (lieu == null) {
            throw new IllegalArgumentException("une étape doit avoir un lieu");
        }
        if (numOrdre < 1) {
            throw new IllegalArgumentException("numOrdre doit être >= 1");
        }
        if (dureeVisiteMin < 0) {
            throw new IllegalArgumentException("la durée de visite ne peut pas être négative");
        }
        this.lieu = lieu;
        this.numOrdre = numOrdre;
        this.dureeVisiteMin = dureeVisiteMin;
    }

    public Lieu getLieu() { return lieu; }
    public int getNumOrdre() { return numOrdre; }
    public int getDureeVisiteMin() { return dureeVisiteMin; }

    /** Réservé à Circuit, qui gère la numérotation des étapes. */
    void setNumOrdre(int numOrdre) { this.numOrdre = numOrdre; }
}