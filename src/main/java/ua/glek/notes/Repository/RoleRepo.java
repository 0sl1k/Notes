package ua.glek.notes.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.glek.notes.Model.Roles;

@Repository
public interface RoleRepo extends JpaRepository<Roles, Long> {
    Roles findByName(String roleUser);
}
