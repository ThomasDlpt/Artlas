package backend.lieu;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import backend.geographie.Adresse;
import backend.geographie.Commune;

import static org.junit.jupiter.api.Assertions.*;

class TestLieu {

    private Lieu creerLieu(String id, String nom, double latitude, double longitude){
        Adresse adresse = new Adresse("1 rue Test", "00000", new Commune("00000", "Test"), latitude, longitude);
        return new Lieu(id, nom, null, TypeLieu.MUSEE, null, null, null, null, adresse);
    }
    private Adresse creerAdresse(double lat,double lon){
        return new Adresse("1 rue Test","00000",new Commune("111111","Test"),lat,lon);
    }

    private SourceLieu creerSource(Source source, String idExterne){
        return new SourceLieu(source,idExterne,LocalDateTime.of(2026,1,1,12,0));
    }

    //----------------------Constructeurs et getters-----------------------------
    @Test
    void testConstructeurVide_initialiseList(){
        Lieu lieu = new Lieu();
        assertTrue(lieu.getLabels().isEmpty());
        assertTrue(lieu.getSource().isEmpty());
        assertNull(lieu.getId());
    }

    @Test
    void testConstructeurComplet_initialiseToutLesChamps(){
        List<String> labels = new ArrayList<>(List.of("Musée de France"));
        LocalDate date = LocalDate.of(2026,5,1);
        Adresse adresse = creerAdresse(48.1,-1.6);
        Lieu lieu = new Lieu ("id1", "Nom", "desc", TypeLieu.MUSEE,
                "https://exemple.fr", "photo.jpg", labels, date, adresse);
        assertEquals("id1",lieu.getId());
        assertEquals("Nom",lieu.getNom());
        assertEquals("desc",lieu.getNom());
        assertEquals(TypeLieu.MUSEE,lieu.getType());
        assertEquals("https://exemple.fr", lieu.getSiteWeb());
        assertEquals("photot.png",lieu.getPhoto());
        assertEquals(labels,lieu.getLabels());
        assertEquals(date, lieu.getDateMiseAJour());
        assertEquals(adresse,lieu.getAdresse());
        assertTrue(lieu.getSource().isEmpty());
    }

    @Test
    void constructeurComplet_labelsNull_creerListeVide(){
        Lieu lieu = new Lieu("id1","Nom",null,TypeLieu.AUTRE,null,null,null,null,null);
        assertNotNull(lieu.getLabels());
        assertTrue(lieu.getLabels().isEmpty());
    }
    //----------------------ajouterSource-----------------------------
    @Test
    void testAjouterSource_null(){
        Lieu lieu = new Lieu("id1","Nom",null,TypeLieu.AUTRE,null,null,null,null,null);
        lieu.ajouterSource(null);
        assertTrue(lieu.getSource().isEmpty());
    }

    @Test
    void testAjouterSource_nouvelleSource_estAjoutee(){
        Lieu lieu = new Lieu();
        SourceLieu s = creerSource(Source.BASILIC,"A1");
        lieu.ajouterSource(s);
        assertEquals(1,lieu.getSource().size());
        assertSame(s,lieu.getSource().get(0));
    }

    @Test
    void testAjouterSource_doublon_Ignorer(){
        Lieu lieu = new Lieu();
        SourceLieu sl = creerSource(Source.DATATOURISME,"B5");
        lieu.ajouterSource(sl);
        assertEquals(1,lieu.getSource().size());
        assertSame(sl,lieu.getSource().get(0));

        lieu.ajouterSource(sl);
        assertEquals(1,lieu.getSource().size());
        assertSame(sl,lieu.getSource().get(0));

    }

    @Test
    void testAjouterSource_memeSourceID_Different(){
        Lieu lieu = new Lieu();
        SourceLieu sl1 = creerSource(Source.BASILIC,"A3");
        SourceLieu sl2 = creerSource(Source.BASILIC,"C7");
        lieu.ajouterSource(sl1);
        lieu.ajouterSource(sl2);
        assertEquals(2,lieu.getSource().size());
        assertSame(sl1,lieu.getSource().get(0));
        assertNotSame(sl1,lieu.getSource().get(1));
        assertSame(sl2,lieu.getSource().get(1));
        assertNotSame(sl2,lieu.getSource().get(0));
    }

    @Test
    void testAjouterSource_SourceDifferent_IdSame(){
        Lieu lieu = new Lieu();

        SourceLieu sl1 = creerSource(Source.BASILIC,"A3");
        SourceLieu sl2 = creerSource(Source.DATATOURISME,"A3");
        lieu.ajouterSource(sl1);
        lieu.ajouterSource(sl2);
        assertEquals(2,lieu.getSource().size());
        assertSame(sl1,lieu.getSource().get(0));
        assertNotSame(sl1,lieu.getSource().get(1));
        assertSame(sl2,lieu.getSource().get(1));
        assertNotSame(sl2,lieu.getSource().get(0));
    }

    @Test
    void mettreAJour_null_Ne_Modifie_Rien(){
        Lieu lieu = new Lieu

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
