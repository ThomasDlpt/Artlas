package backend.circuits;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import backend.geographie.Adresse;
import backend.geographie.Commune;
import backend.lieu.Lieu;

/**
 * Itinéraire culturel composé d'étapes ordonnées (numOrdre de 1 à n).
 *
 * Cycle de vie (cf. diag_etat_circuit) : un circuit peut être créé vide puis rempli
 * étape par étape ; il n'est considéré valide (enregistrable) qu'à partir de 2 étapes.
 *
 * distanceKm, dureeMinutes et communeDepart sont des attributs dérivés,
 * recalculés automatiquement après chaque modification des étapes.
 */
public class Circuit {
    /** Vitesse de marche utilisée pour estimer le temps de trajet entre deux étapes. */
    public static final double VITESSE_MARCHE_KMH = 5.0;
    public static final int NB_ETAPES_MIN = 2;

    private static final String ALPHABET_CODE = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int LONGUEUR_CODE = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final String id;
    private String nom;
    private String description;
    private final String codePartage;
    private double distanceKm;
    private int dureeMinutes;
    private final List<EtapeCircuit> etapes;
    private Commune communeDepart;

    public Circuit(String nom, String description) {
        setNom(nom);
        this.description = description;
        this.id = UUID.randomUUID().toString();
        this.codePartage = genererCodePartage();
        this.etapes = new ArrayList<>();
    }

    // ---------- Getters / setters ----------

    public String getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        if (nom == null || nom.isBlank()) {
            throw new IllegalArgumentException("le nom du circuit est obligatoire");
        }
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCodePartage() {
        return codePartage;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public int getDureeMinutes() {
        return dureeMinutes;
    }

    /** Liste en lecture seule : passer par ajouterEtape / retirerEtape / reordonner pour la modifier. */
    public List<EtapeCircuit> getEtapes() {
        return Collections.unmodifiableList(etapes);
    }

    public int getNbEtapes() {
        return etapes.size();
    }

    /** Commune du lieu de la première étape (null si le circuit est vide). */
    public Commune getCommuneDepart() {
        return communeDepart;
    }


    // ---------- Opérations métier ----------

    /**
     * Ajoute un lieu à la fin du circuit (numOrdre = n + 1).
     * @throws IllegalArgumentException si le lieu est null, déjà présent, ou si la durée est négative
     */
    public void ajouterEtape(Lieu lieu, int dureeVisiteMin) {
        if (lieu == null) {
            throw new IllegalArgumentException("lieu null");
        }
        if (contientLieu(lieu)) {
            throw new IllegalArgumentException("ce lieu fait déjà partie du circuit");
        }
        etapes.add(new EtapeCircuit(lieu, etapes.size() + 1, dureeVisiteMin));
        recalculer();
    }

    /**
     * Retire l'étape située à la position donnée (de 1 à n).
     */
    public void retirerEtape(int position) {
        verifierPosition(position);
        etapes.remove(position - 1);
        renumeroter();
        recalculer();
    }

    /**
     * Déplace l'étape de la position {@code position} vers {@code nouvellePosition} (de 1 à n).
     */
    public void reordonner(int position, int nouvellePosition) {
        verifierPosition(position);
        verifierPosition(nouvellePosition);
        if (position == nouvellePosition) {
            return;
        }
        EtapeCircuit etape = etapes.remove(position - 1);
        etapes.add(nouvellePosition - 1, etape);
        renumeroter();
        recalculer();
    }

    /**
     * Met à jour distanceKm (somme des distances à vol d'oiseau entre étapes consécutives),
     * dureeMinutes (durées de visite + trajet à pied) et    (commune de la 1re étape).
     */
    public void recalculer() {
        double distance = 0;
        int dureeVisites = 0;
        for (int i = 0; i < etapes.size(); i++) {
            dureeVisites += etapes.get(i).getDureeVisiteMin();
            if (i > 0) {
                Adresse precedente = etapes.get(i - 1).getLieu().getAdresse();
                Adresse courante = etapes.get(i).getLieu().getAdresse();
                if (precedente != null && courante != null) {
                    distance += precedente.distanceKm(courante);
                }
            }
        }
        this.distanceKm = distance;
        int dureeTrajet = (int) Math.round(distance / VITESSE_MARCHE_KMH * 60);
        this.dureeMinutes = dureeVisites + dureeTrajet;

        if (etapes.isEmpty() || etapes.get(0).getLieu().getAdresse() == null) {
            this.communeDepart = null;
        } else {
            this.communeDepart = etapes.get(0).getLieu().getAdresse().getCommune();
        }
    }

    /** Un circuit est valide (enregistrable) s'il contient au moins 2 étapes. */
    public boolean estValide() {
        return etapes.size() >= NB_ETAPES_MIN;
    }

    public boolean contientLieu(Lieu lieu) {
        for (EtapeCircuit e : etapes) {
            if (e.getLieu() == lieu || (e.getLieu().getId() != null && e.getLieu().getId().equals(lieu.getId()))) {
                return true;
            }
        }
        return false;
    }

    // ---------- Méthodes internes ----------

    private void verifierPosition(int position) {
        if (position < 1 || position > etapes.size()) {
            throw new IndexOutOfBoundsException(
                    "position " + position + " invalide (le circuit a " + etapes.size() + " étapes)");
        }
    }

    private void renumeroter() {
        for (int i = 0; i < etapes.size(); i++) {
            etapes.get(i).setNumOrdre(i + 1);
        }
    }

    private static String genererCodePartage() {
        StringBuilder sb = new StringBuilder(LONGUEUR_CODE);
        for (int i = 0; i < LONGUEUR_CODE; i++) {
            sb.append(ALPHABET_CODE.charAt(RANDOM.nextInt(ALPHABET_CODE.length())));
        }
        return sb.toString();
    }

}