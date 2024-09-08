package ua.glek.notes.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.glek.notes.Model.Notes;
@Repository
public interface NotesRepo  extends JpaRepository<Notes, Long> {


}
