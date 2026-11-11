package backend.lieu;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import backend.geographie.Adresse;

public class Lieu {
    private String id;
    private String nom;
    private String description; // [0..1]
    private TypeLieu type;
    private String siteWeb; // [0..1]
    private String photo; // [0..1]
    private final List<String> labels;
    private LocalDate dateMiseAJour;
    private Adresse adresse;



    public Lieu(){
        this.labels = new ArrayList<>();

    }

    public Lieu(
        String id,
        String nom,
        String description,
        TypeLieu type,
        String siteWeb,
        String photo,
        List<String> labels,
        LocalDate dateMiseAJour,
        Adresse adresse
    ){
        this.id = id;
        this.nom = normaliser(nom);
        this.description = normaliser(description);
        this.type = type;
        this.siteWeb = normaliser(siteWeb);
        this.photo = normaliser(photo);
        this.labels = new ArrayList<>();
        if(labels != null){
            for(String label : labels){
                if(estRenseigne(label) && !this.labels.contains(label)){
                    this.labels.add(label);
                }
            }
        }
        this.dateMiseAJour = dateMiseAJour;
        this.adresse = adresse;

    }



    public String getId(){
        return id;
    }

    public String getNom(){
        return nom;
    }

    public String getDescription(){
        return description;
    }

    public TypeLieu getType(){
        return type;
    }

    public String getSiteWeb(){
        return siteWeb;
    }

    public String getPhoto(){
        return photo;
    }

    // liste en lecture seule : les labels ne se modifient que via le constructeur ou mettreAJour
    public List<String> getLabels(){
        return Collections.unmodifiableList(labels);
    }

    public LocalDate getDateMiseAJour(){
        return dateMiseAJour;
    }

    public Adresse getAdresse(){
        return adresse;
    }

    // liste en lecture seule : les sources ne s'ajoutent que via ajouterSource


    // distance en km entre les deux lieux (formule de haversine)
    public double distanceVers(Lieu autre){
        if(autre == null || autre.getAdresse()==null || this.adresse == null){
            throw new IllegalArgumentException("un des 2 lieu est vide ");
        }
        //Récupération donnée
        double lat1 = this.adresse.getLatitude();
        double long1 = this.adresse.getLongitude();
        double lat2 = autre.getAdresse().getLatitude();
        double long2 = autre.getAdresse().getLongitude();

        // Constante rayon de la terre
        double R = 6371.0;

        //Ecart et radian
        double dLat = Math.toRadians(lat2-lat1);
        double dLon = Math.toRadians(long2-long1);

        //Formule de haversine
        double a = (Math.sin(dLat/2)*Math.sin(dLat/2)) + Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))*(Math.sin(dLon/2)*Math.sin(dLon/2));

        double c = 2*Math.atan2(Math.sqrt(a),Math.sqrt(1-a));
        return R*c;
    }


    // remplace les champs par ceux de donnees quand ils sont renseignés
    public void mettreAJour(Lieu donnees){
        if(donnees== null){
            return;
        }
        // les chaînes vides ont déjà été converties en null par le constructeur
        if(donnees.nom != null){this.nom = donnees.nom;}
        if(donnees.description !=null){this.description=donnees.description;}
        if(donnees.type !=null){this.type=donnees.type;}
        if(donnees.siteWeb !=null){this.siteWeb=donnees.siteWeb;}
        if(donnees.photo !=null){this.photo=donnees.photo;}
        if(donnees.adresse !=null){this.adresse=donnees.adresse;}

        for(String label :donnees.getLabels()){
            if(!this.labels.contains(label)){
               this.labels.add(label);
            }
        }

        this.dateMiseAJour =LocalDate.now();

    }

    // vrai si la chaîne n'est ni null, ni vide, ni composée uniquement d'espaces
    private static boolean estRenseigne(String valeur){
        return valeur != null && !valeur.trim().isEmpty();
    }

    // renvoie null si la chaîne est vide ou ne contient que des espaces
    private static String normaliser(String valeur){
        return estRenseigne(valeur) ? valeur : null;
    }
}
