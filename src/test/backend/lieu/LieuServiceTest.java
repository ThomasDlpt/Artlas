package backend.lieu;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import backend.accesdonnees.LieuRepository;
import backend.consultation.LieuService;
import backend.recherche.Bbox;
import backend.recherche.FiltreRecherche;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LieuServiceTest {

    @Mock
    private LieuRepository repo;

    private LieuService service;

    @BeforeEach
    void setUp(){
        service = new LieuService(repo);
    }

    private Lieu creerLieu(String id, String nom){
        return new LieuBasilic(id, nom, null, TypeLieu.MUSEE, null, null, null, null, null);
    }

    //----------------------Constructeur-----------------------------
    @Test
    void constructeur_repoNull_leveException(){
        assertThrows(IllegalArgumentException.class, () -> new LieuService(null));
    }

    //----------------------rechercher-----------------------------
    @Test
    void rechercher_delegueAuRepository(){
        FiltreRecherche filtre = new FiltreRecherche(List.of(TypeLieu.MUSEE), "35", null, null);
        List<Lieu> attendus = List.of(creerLieu("1", "Musée des Beaux-Arts"));
        when(repo.trouverParFiltre(filtre)).thenReturn(attendus);

        List<Lieu> resultat = service.rechercher(filtre);

        assertSame(attendus, resultat);
        verify(repo).trouverParFiltre(filtre);
    }

    @Test
    void rechercher_filtreNull_estTransmisAuRepository(){
        List<Lieu> tous = List.of(creerLieu("1", "A"), creerLieu("2", "B"));
        when(repo.trouverParFiltre(null)).thenReturn(tous);

        assertEquals(tous, service.rechercher(null));
        verify(repo).trouverParFiltre(null);
    }

    @Test
    void rechercher_aucunResultat_renvoieListeVide(){
        FiltreRecherche filtre = new FiltreRecherche(null, null, null, "introuvable");
        when(repo.trouverParFiltre(filtre)).thenReturn(List.of());

        assertTrue(service.rechercher(filtre).isEmpty());
    }

    //----------------------selectionner-----------------------------
    @Test
    void selectionner_idExistant_renvoieLeLieu(){
        Lieu lieu = creerLieu("id1", "Musée");
        when(repo.trouverParId("id1")).thenReturn(lieu);

        assertSame(lieu, service.selectionner("id1"));
        verify(repo).trouverParId("id1");
    }

    @Test
    void selectionner_idInconnu_renvoieNull(){
        when(repo.trouverParId("inconnu")).thenReturn(null);

        assertNull(service.selectionner("inconnu"));
    }

    @Test
    void selectionner_idNull_leveException(){
        assertThrows(IllegalArgumentException.class, () -> service.selectionner(null));
        verifyNoInteractions(repo);
    }

    @Test
    void selectionner_idVide_leveException(){
        assertThrows(IllegalArgumentException.class, () -> service.selectionner(""));
        verifyNoInteractions(repo);
    }

    //----------------------rechercherDansZone-----------------------------
    @Test
    void rechercherDansZone_delegueAuRepository(){
        Bbox zone = new Bbox(47.0, 49.0, -2.0, -1.0);
        FiltreRecherche filtre = new FiltreRecherche(null, null, null, null);
        List<Lieu> attendus = List.of(creerLieu("1", "Rennes"));
        when(repo.trouverDansZone(zone, filtre)).thenReturn(attendus);

        assertSame(attendus, service.rechercherDansZone(zone, filtre));
        verify(repo).trouverDansZone(zone, filtre);
    }

    @Test
    void rechercherDansZone_filtreNull_accepte(){
        Bbox zone = new Bbox(47.0, 49.0, -2.0, -1.0);
        when(repo.trouverDansZone(zone, null)).thenReturn(List.of());

        assertTrue(service.rechercherDansZone(zone, null).isEmpty());
        verify(repo).trouverDansZone(zone, null);
    }

    @Test
    void rechercherDansZone_zoneNull_leveException(){
        FiltreRecherche filtre = new FiltreRecherche(null, null, null, null);

        assertThrows(IllegalArgumentException.class, () -> service.rechercherDansZone(null, filtre));
        verifyNoInteractions(repo);
    }
}
