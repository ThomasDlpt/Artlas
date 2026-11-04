package backend.geographie;


public class Commune{
    private String codeInsee;
    private String nom;

    
    
    public Commune(String codeInsee, String nom){
        this.codeInsee = codeInsee;
        this.nom = nom;
    }

    public String getCodeInsee(){
        return this.codeInsee;
    }

    public String getNom(){
        return this.nom;
    }
    
}