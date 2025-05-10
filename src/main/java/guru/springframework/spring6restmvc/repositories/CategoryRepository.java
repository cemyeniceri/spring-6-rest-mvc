package guru.springframework.spring6restmvc.repositories;

import guru.springframework.spring6restmvc.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
}
