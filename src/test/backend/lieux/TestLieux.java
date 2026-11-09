package backend.lieux;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import backend.geographie.Adresse;
import backend.geographie.Commune;

class TestLieux {

    private Lieu creerLieu(String id, String nom, double latitude, double longitude){
        Adresse adresse = new Adresse("1 rue Test", "00000", new Commune("00000", "Test"), latitude, longitude);
        return new Lieu(id, nom, null, TypeLieu.MUSEE, null, null, null, null, adresse);
    }

    @Test
    void distanceVers_rennesParis_environ308km(){
        Lieu rennes = creerLieu("1", "Rennes", 48.1173, -1.6778);
        Lieu paris = creerLieu("2", "Paris", 48.8566, 2.3522);

        assertEquals(308.0, rennes.distanceVers(paris), 2.0);
    }

    @Test
    void distanceVers_memeLieu_zero(){
        Lieu rennes = creerLieu("1", "Rennes", 48.1173, -1.6778);

        assertEquals(0.0, rennes.distanceVers(rennes), 0.001);
    }

    @Test
    void distanceVers_estSymetrique(){
        Lieu rennes = creerLieu("1", "Rennes", 48.1173, -1.6778);
        Lieu paris = creerLieu("2", "Paris", 48.8566, 2.3522);

        assertEquals(rennes.distanceVers(paris), paris.distanceVers(rennes), 0.001);
    }

    @Test
    void distanceVers_lieuNull_leveException(){
        Lieu rennes = creerLieu("1", "Rennes", 48.1173, -1.6778);

        assertThrows(IllegalArgumentException.class, () -> rennes.distanceVers(null));
    }

    @Test
    void distanceVers_adresseNull_leveException(){
        Lieu rennes = creerLieu("1", "Rennes", 48.1173, -1.6778);
        Lieu sansAdresse = new Lieu();

        assertThrows(IllegalArgumentException.class, () -> rennes.distanceVers(sansAdresse));
        assertThrows(IllegalArgumentException.class, () -> sansAdresse.distanceVers(rennes));
    }
}
