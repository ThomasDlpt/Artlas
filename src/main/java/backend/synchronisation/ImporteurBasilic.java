package backend.synchronisation;

import backend.accesdonnees.CommuneRepository;
import backend.geographie.Commune;
import backend.geographie.Departement;
import backend.geographie.Region;
import backend.geographie.Adresse;
import backend.lieu.Source;
import backend.lieu.TypeLieu;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import backend.lieu.Lieu;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

@Component
public class ImporteurBasilic implements Importeur{

    private static final String REQUETE = "/dced78ee-0823-4b61-86e6-57717308d4e4/data/?page_size=200";
    //private static final String REQUETE = "/dced78ee-0823-4b61-86e6-57717308d4e4/data/?page_size=50&Type équipement ou lieu__exact=Musée";

    private final RestClient client;
    private final CommuneRepository communeRepo;

    public ImporteurBasilic(@Value("${basilic.url}") String url, CommuneRepository communeRepo) {
        this.client = RestClient.create(url);
        this.communeRepo = communeRepo;
    }

    @Override
    public Source getSource() {
        return Source.BASILIC;
    }

    @Override
    public ResultatImport importer() {

        List<String> erreurs = new ArrayList<>();
        List<Lieu> lieux = new ArrayList<>();
        int pageLues = 0;
        int page = 1;
        boolean pageSuivante = true;

        while (pageSuivante) {
            try {
                JsonNode res = client.get().uri(REQUETE + "&page=" + page).retrieve().body(JsonNode.class);
                for (JsonNode obj : res.path("data")) {
                    pageLues++;
                    try {
                        String id = obj.path("Identifiant origine").asText();
                        String nom = obj.path("Nom").asText();
                        TypeLieu type = convertirCategories(obj.path("Type équipement ou lieu").asText());
                        List<String> labels = new ArrayList<>();
                        String label = obj.path("Label et appellation").asText();
                        if (!label.isEmpty()) {
                            labels.add(label);
                        }
                        String rue = obj.path("Adresse").asText();
                        String codePostal = obj.path("Code Postal").asText();
                        double latitude = obj.path("Latitude").asDouble();
                        double longitude = obj.path("Longitude").asDouble();
                        String codeInsee = obj.path("code_insee").asText();
                        String nomCommune = obj.path("libelle_geographique").asText();
                        String codeDep = obj.path("N_Département").asText();
                        String nomDep = obj.path("Département").asText();
                        String codeReg = obj.path("N_Région").asText();
                        String nomReg = obj.path("Région").asText();
                        Commune c = communeRepo.renvoieOuCreeCommune(codeInsee, nomCommune);
                        if (c.getDepartement() == null && !codeDep.isEmpty() && !codeReg.isEmpty()) {
                            Region reg = new Region(codeReg, nomReg);
                            Departement dep = new Departement(codeDep, nomDep, reg);
                            c.setDepartement(dep);
                        }
                        Adresse adresse = new Adresse(rue, codePostal, c, latitude, longitude);
                        Lieu lieu = new Lieu(id, nom, null, type, null, null, labels, LocalDate.now(), adresse);
                        lieux.add(lieu);
                    } catch (Exception e) {
                        erreurs.add(e.getMessage());
                    }
                }
                System.out.println(" Pages : " + page);
                pageSuivante = !res.path("links").path("next").isNull();
                page++;
            }catch (Exception e){
                System.out.println(e.getMessage());
            }
        }
        return new ResultatImport(getSource(), pageLues, erreurs, lieux);
    }

    private TypeLieu convertirCategories(String types){
        return switch (types){
            case "Musée" -> TypeLieu.MUSEE;
            case "Bibliothèque" -> TypeLieu.BIBLIOTHEQUE;
            case "Cinéma" -> TypeLieu.CINEMA;
            case "Centre d'art", "Fonds régional d'art contemporain", "Lieu d'exposition" -> TypeLieu.LIEU_EXPOSITION;
            case "Monument", "Lieu archéologique", "Lieu de mémoire" -> TypeLieu.MONUMENT;
            case "Théâtre", "Scène", "Opéra", "Cirque" -> TypeLieu.THEATRE;
            default -> TypeLieu.AUTRE;
        };
    }

    public static void main(String[] args) {
        CommuneRepository communeRepo = new CommuneRepository();
        ImporteurBasilic test = new ImporteurBasilic("https://tabular-api.data.gouv.fr/api/resources", communeRepo);
        ResultatImport res = test.importer();
        System.out.println("Lignes lues : " + res.getNbLignesLues() + ", Lieux : " + res.getLieux().size());
    }
}
