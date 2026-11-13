package backend.lieu;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import backend.geographie.Adresse;
import backend.geographie.Commune;

import static org.junit.jupiter.api.Assertions.*;

class LieuTest {

    // Lieu est abstraite : on teste via LieuBasilic
    private Lieu creerLieu(String id, String nom, double latitude, double longitude){
        Adresse adresse = new Adresse("1 rue Test", "00000", new Commune("00000", "Test"), latitude, longitude);
        return new LieuBasilic(id, nom, null, TypeLieu.MUSEE, null, null, null, null, adresse);
    }
    private Adresse creerAdresse(double lat,double lon){
        return new Adresse("1 rue Test","00000",new Commune("111111","Test"),lat,lon);
    }

    //----------------------Constructeurs et getters-----------------------------
    @Test
    void testConstructeurVide_initialiseList(){
        Lieu lieu = new LieuBasilic();
        assertTrue(lieu.getLabels().isEmpty());
        assertNull(lieu.getId());
    }

    @Test
    void testConstructeurComplet_initialiseToutLesChamps(){
        List<String> labels = new ArrayList<>(List.of("Musée de France"));
        LocalDate date = LocalDate.of(2026,5,1);
        Adresse adresse = creerAdresse(48.1,-1.6);
        Lieu lieu = new LieuBasilic("id1", "Nom", "desc", TypeLieu.MUSEE,
                "https://exemple.fr", "photo.jpg", labels, date, adresse);
        assertEquals("id1",lieu.getId());
        assertEquals("Nom",lieu.getNom());
        assertEquals("desc",lieu.getDescription());
        assertEquals(TypeLieu.MUSEE,lieu.getType());
        assertEquals("https://exemple.fr", lieu.getSiteWeb());
        assertEquals("photo.jpg",lieu.getPhoto());
        assertEquals(labels,lieu.getLabels());
        assertEquals(date, lieu.getDateMiseAJour());
        assertEquals(adresse,lieu.getAdresse());
    }

    @Test
    void constructeurComplet_labelsNull_creerListeVide(){
        Lieu lieu = new LieuBasilic("id1","Nom",null,TypeLieu.AUTRE,null,null,null,null,null);
        assertNotNull(lieu.getLabels());
        assertTrue(lieu.getLabels().isEmpty());
    }

    @Test
    void constructeurComplet_chainesVides_deviennentNull(){
        Lieu lieu = new LieuBasilic("id1","  ","",TypeLieu.AUTRE," ","",
                new ArrayList<>(List.of("", "  ", "Label", "Label")),null,null);
        assertNull(lieu.getNom());
        assertNull(lieu.getDescription());
        assertNull(lieu.getSiteWeb());
        assertNull(lieu.getPhoto());
        assertEquals(List.of("Label"),lieu.getLabels());
    }

    //----------------------mettreAJour-----------------------------
    @Test
    void mettreAJour_null_Ne_Modifie_Rien(){
        LocalDate date = LocalDate.of(2020,1,1);
        Adresse adresse = creerAdresse(48.1,-1.6);
        Lieu lieu = new LieuBasilic("id1","Nom","desc",TypeLieu.MUSEE,
                "https://exemple.fr","photo.jpg",new ArrayList<>(List.of("L1")),date,adresse);

        lieu.mettreAJour(null);

        assertEquals("Nom",lieu.getNom());
        assertEquals("desc",lieu.getDescription());
        assertEquals(TypeLieu.MUSEE,lieu.getType());
        assertEquals("https://exemple.fr",lieu.getSiteWeb());
        assertEquals("photo.jpg",lieu.getPhoto());
        assertEquals(List.of("L1"),lieu.getLabels());
        assertEquals(date,lieu.getDateMiseAJour());
        assertSame(adresse,lieu.getAdresse());
    }

    @Test
    void mettreAJour_tousChampsRenseignes_remplaceTout(){
        Lieu lieu = new LieuBasilic("id1","Ancien nom","ancienne desc",TypeLieu.AUTRE,
                "https://ancien.fr","ancienne.jpg",null,LocalDate.of(2020,1,1),creerAdresse(0,0));
        Adresse nouvelleAdresse = creerAdresse(48.1,-1.6);
        Lieu donnees = new LieuBasilic("id2","Nouveau nom","nouvelle desc",TypeLieu.THEATRE,
                "https://nouveau.fr","nouvelle.jpg",null,null,nouvelleAdresse);

        lieu.mettreAJour(donnees);

        assertEquals("id1",lieu.getId()); // l'id n'est jamais modifié
        assertEquals("Nouveau nom",lieu.getNom());
        assertEquals("nouvelle desc",lieu.getDescription());
        assertEquals(TypeLieu.THEATRE,lieu.getType());
        assertEquals("https://nouveau.fr",lieu.getSiteWeb());
        assertEquals("nouvelle.jpg",lieu.getPhoto());
        assertSame(nouvelleAdresse,lieu.getAdresse());
        assertEquals(LocalDate.now(),lieu.getDateMiseAJour());
    }

    @Test
    void mettreAJour_champsNull_conserveLesValeurs(){
        Adresse adresse = creerAdresse(48.1,-1.6);
        Lieu lieu = new LieuBasilic("id1","Nom","desc",TypeLieu.MUSEE,
                "https://exemple.fr","photo.jpg",null,LocalDate.of(2020,1,1),adresse);

        lieu.mettreAJour(new LieuBasilic()); // tous les champs de donnees sont null

        assertEquals("Nom",lieu.getNom());
        assertEquals("desc",lieu.getDescription());
        assertEquals(TypeLieu.MUSEE,lieu.getType());
        assertEquals("https://exemple.fr",lieu.getSiteWeb());
        assertEquals("photo.jpg",lieu.getPhoto());
        assertSame(adresse,lieu.getAdresse());
        assertTrue(lieu.getLabels().isEmpty());
        assertEquals(LocalDate.now(),lieu.getDateMiseAJour());
    }

    @Test
    void mettreAJour_labels_ajouteSansDoublon(){
        Lieu lieu = new LieuBasilic("id1","Nom",null,TypeLieu.MUSEE,null,null,
                new ArrayList<>(List.of("Musée de France")),null,null);
        Lieu donnees = new LieuBasilic("id2",null,null,null,null,null,
                new ArrayList<>(List.of("Musée de France","Maison des Illustres")),null,null);

        lieu.mettreAJour(donnees);

        assertEquals(List.of("Musée de France","Maison des Illustres"),lieu.getLabels());
    }

    //----------------------distanceVers-----------------------------
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
        Lieu sansAdresse = new LieuBasilic();

        assertThrows(IllegalArgumentException.class, () -> rennes.distanceVers(sansAdresse));
        assertThrows(IllegalArgumentException.class, () -> sansAdresse.distanceVers(rennes));
    }

    @Test
    void constructeurComplet_labelNull_ignore(){
        List<String> labels = new ArrayList<>();
        labels.add(null);
        labels.add("Label");
        Lieu lieu = new LieuBasilic("id1","Nom",null,TypeLieu.AUTRE,null,null,labels,null,null);
        assertEquals(List.of("Label"),lieu.getLabels());
    }

    @Test
    void getLabels_estEnLectureSeule(){
        Lieu lieu = new LieuBasilic("id1","Nom",null,TypeLieu.AUTRE,null,null,
                new ArrayList<>(List.of("Label")),null,null);
        assertThrows(UnsupportedOperationException.class, () -> lieu.getLabels().add("Autre"));
    }

    //----------------------LieuDataTourisme-----------------------------
    @Test
    void lieuDataTourisme_constructeurVide(){
        Lieu lieu = new LieuDataTourisme();
        assertNull(lieu.getId());
        assertTrue(lieu.getLabels().isEmpty());
    }

    @Test
    void lieuDataTourisme_constructeurComplet(){
        LocalDate date = LocalDate.of(2026,5,1);
        Adresse adresse = creerAdresse(48.1,-1.6);
        Lieu lieu = new LieuDataTourisme("dt1","Théâtre","desc",TypeLieu.THEATRE,
                "https://exemple.fr","photo.jpg",new ArrayList<>(List.of("Label")),date,adresse);
        assertEquals("dt1",lieu.getId());
        assertEquals("Théâtre",lieu.getNom());
        assertEquals(TypeLieu.THEATRE,lieu.getType());
        assertEquals(List.of("Label"),lieu.getLabels());
        assertEquals(date,lieu.getDateMiseAJour());
        assertSame(adresse,lieu.getAdresse());
    }

    @Test
    void mettreAJour_entreSourcesDifferentes(){
        Lieu basilic = new LieuBasilic("id1","Nom",null,TypeLieu.MUSEE,null,null,null,null,null);
        Lieu dataTourisme = new LieuDataTourisme("dt1",null,"desc DT",null,null,null,
                new ArrayList<>(List.of("Label DT")),null,null);

        basilic.mettreAJour(dataTourisme);

        assertEquals("Nom",basilic.getNom());
        assertEquals("desc DT",basilic.getDescription());
        assertEquals(List.of("Label DT"),basilic.getLabels());
    }

    //----------------------Enums-----------------------------
    @Test
    void typeLieu_valeurs(){
        assertEquals(7,TypeLieu.values().length);
        assertEquals(TypeLieu.MUSEE,TypeLieu.valueOf("MUSEE"));
    }

    @Test
    void source_valeurs(){
        assertArrayEquals(new Source[]{Source.BASILIC,Source.DATATOURISME},Source.values());
        assertEquals(Source.DATATOURISME,Source.valueOf("DATATOURISME"));
    }
}
