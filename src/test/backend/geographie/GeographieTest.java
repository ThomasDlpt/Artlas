package backend.geographie;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GeographieTest {

    private Region bretagne;
    private Departement illeEtVilaine;
    private Commune rennes;

    @BeforeEach
    void setUp() {
        bretagne = new Region("53", "Bretagne");
        illeEtVilaine = new Departement("35", "Ille-et-Vilaine", bretagne);
        rennes = new Commune("35238", "Rennes", illeEtVilaine);
    }

    // ---------- Hiérarchie Commune -> Departement -> Region ----------

    @Test
    void navigationCommuneVersRegion() {
        assertEquals("35", rennes.getDepartement().getCode());
        assertEquals("53", rennes.getDepartement().getRegion().getCode());
        assertEquals("Bretagne", rennes.getDepartement().getRegion().getNom());
    }

    @Test
    void communeSansDepartementPuisRattachement() {
        Commune c = new Commune("79126", "Les Fosses");
        assertNull(c.getDepartement());
        Departement deuxSevres = new Departement("79", "Deux-Sèvres", new Region("75", "Nouvelle-Aquitaine"));
        c.setDepartement(deuxSevres);
        assertEquals("79", c.getDepartement().getCode());
    }

    @Test
    void egaliteParCode() {
        assertEquals(new Region("53", "Bretagne"), bretagne);
        assertEquals(new Departement("35", "Ille-et-Vilaine", bretagne), illeEtVilaine);
        assertEquals(new Commune("35238", "Rennes"), rennes);
        assertEquals(new Commune("35238", "Rennes").hashCode(), rennes.hashCode());
        assertNotEquals(new Commune("35047", "Bruz"), rennes);
    }

    @Test
    void parametresInvalidesRefuses() {
        assertThrows(IllegalArgumentException.class, () -> new Region(null, "Bretagne"));
        assertThrows(IllegalArgumentException.class, () -> new Region("53", ""));
        assertThrows(IllegalArgumentException.class, () -> new Departement("35", "Ille-et-Vilaine", null));
        assertThrows(IllegalArgumentException.class, () -> new Commune("", "Rennes"));
        assertThrows(IllegalArgumentException.class, () -> new Commune("35238", null));
    }

    // ---------- Adresse ----------

    @Test
    void adresseValide() {
        Adresse a = new Adresse("20 quai Émile Zola", "35000", rennes, 48.1097, -1.6744);
        assertEquals("20 quai Émile Zola", a.getRue());
        assertEquals("35000", a.getCodePostal());
        assertEquals(48.1097, a.getLatitude());
        assertEquals(-1.6744, a.getLongitude());
        assertSame(rennes, a.getCommune());
    }

    @Test
    void adresseSansRueAutorisee() {
        Adresse a = new Adresse(null, "35000", rennes, 48.11, -1.67);
        assertNull(a.getRue());
    }

    @Test
    void adresseCoordonneesInvalides() {
        assertThrows(IllegalArgumentException.class, () -> new Adresse("r", "35000", rennes, 91, 0));
        assertThrows(IllegalArgumentException.class, () -> new Adresse("r", "35000", rennes, -91, 0));
        assertThrows(IllegalArgumentException.class, () -> new Adresse("r", "35000", rennes, 0, 181));
        assertThrows(IllegalArgumentException.class, () -> new Adresse("r", "35000", rennes, 0, -181));
    }

    @Test
    void adresseSansCommuneRefusee() {
        assertThrows(IllegalArgumentException.class, () -> new Adresse("r", "35000", null, 48.1, -1.6));
    }

    @Test
    void distanceEntreDeuxAdresses() {
        Adresse parlement = new Adresse("Place du Parlement de Bretagne", "35000", rennes, 48.1124, -1.6781);
        Adresse musee = new Adresse("20 quai Émile Zola", "35000", rennes, 48.1097, -1.6744);
        double d = parlement.distanceKm(musee);
        assertEquals(0.40, d, 0.02); // ~400 m entre le Parlement et le Musée des Beaux-Arts
        assertEquals(d, musee.distanceKm(parlement), 1e-9);
        assertEquals(0.0, parlement.distanceKm(parlement), 1e-9);
    }

    @Test
    void distanceRennesParis() {
        Adresse a = new Adresse(null, "35000", rennes, 48.1173, -1.6778);
        Commune paris75 = new Commune("75056", "Paris", new Departement("75", "Paris", new Region("11", "Île-de-France")));
        Adresse paris = new Adresse(null, "75001", paris75, 48.8566, 2.3522);
        assertEquals(308, a.distanceKm(paris), 5);
    }
}
