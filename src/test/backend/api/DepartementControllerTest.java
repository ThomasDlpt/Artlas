package backend.api;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import backend.consultation.DepartementService;
import backend.geographie.Departement;
import backend.geographie.Region;

/** Test du contrôleur seul (MockMvc autonome) : pas besoin de démarrer toute l'application. */
@ExtendWith(MockitoExtension.class)
class DepartementControllerTest {

    @Mock
    private DepartementService service;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new DepartementController(service)).build();
    }

    @Test
    void get_renvoieLesDepartementsEnJson() throws Exception {
        Region bretagne = new Region("53", "Bretagne");
        when(service.lister()).thenReturn(List.of(
            new Departement("22", "Côtes-d’Armor", bretagne),
            new Departement("35", "Ille-et-Vilaine", bretagne)));

        mvc.perform(get("/api/departements"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].code").value("22"))
            .andExpect(jsonPath("$[0].nom").value("Côtes-d’Armor"))
            .andExpect(jsonPath("$[1].code").value("35"))
            .andExpect(jsonPath("$[1].nom").value("Ille-et-Vilaine"));
    }

    @Test
    void get_neRenvoiePasLaRegion() throws Exception {
        when(service.lister()).thenReturn(List.of(new Departement("35", "Ille-et-Vilaine", new Region("53", "Bretagne"))));

        mvc.perform(get("/api/departements"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].region").doesNotExist());
    }

    @Test
    void get_rienImporte_renvoieListeVideEtStatut200() throws Exception {
        when(service.lister()).thenReturn(List.of());

        mvc.perform(get("/api/departements"))
            .andExpect(status().isOk())
            .andExpect(content().json("[]"));
    }
}
