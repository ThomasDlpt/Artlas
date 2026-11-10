package backend.lieu;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import backend.geographie.Adresse;

public class Lieu {
    private String id;
    private String nom;
    private String description; // [0..1]
    private TypeLieu type;
    private String siteWeb; // [0..1]
    private String photo; // [0..1]
    private List<String> labels;
    private LocalDate dateMiseAJour;
    private Adresse adresse;
    private List<SourceLieu> source;


    public Lieu(){
        this.labels = new ArrayList<>();
        this.source = new ArrayList<>();
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
        this.nom = nom;
        this.description = description;
        this.type = type;
        this.siteWeb = siteWeb;
        this.photo = photo;
        this.labels = labels != null ? labels : new ArrayList<>();
        this.dateMiseAJour = dateMiseAJour;
        this.adresse = adresse;
        this.source = new ArrayList<>();
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

    public List<String> getLabels(){
        return labels;
    }

    public LocalDate getDateMiseAJour(){
        return dateMiseAJour;
    }

    public Adresse getAdresse(){
        return adresse;
    }

    public List<SourceLieu> getSource(){
        return source;
    }

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

    public void ajouterSource(SourceLieu source){
        if(source == null){
            return;
        }
        for(SourceLieu existante : this.source){
            if(existante.getSource() == source.getSource() && existante.getIdExterne().equals(source.getIdExterne())){
                return;
            }
        }
        this.source.add(source);


    }

    // remplace les champs par ceux de donnees quand ils sont renseignés
    public void mettreAJour(Lieu donnees){
        if(donnees== null){
            return;
        }
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
        for(SourceLieu source :donnees.getSource()){
            this.ajouterSource(source);
        }
        this.dateMiseAJour =LocalDate.now();

    }
}
