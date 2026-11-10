package backend.accesdonnees;

import backend.lieu.Source;
import backend.synchronisation.JournalSynchro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JournalSynchroRepository extends JpaRepository<JournalSynchro, String> {

    JournalSynchro findFirstBySourceOrderByDateSynchroDesc(Source src);

    default void enregistrer(JournalSynchro journal) {
        if (journal != null) {
            save(journal);
        }
    }

    default JournalSynchro trouverDernier(Source src) {
        return findFirstBySourceOrderByDateSynchroDesc(src);
    }
}