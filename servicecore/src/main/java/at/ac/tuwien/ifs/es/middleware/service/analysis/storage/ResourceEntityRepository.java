package at.ac.tuwien.ifs.es.middleware.service.analysis.storage;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface ResourceEntityRepository extends JpaRepository<ResourceEntity, Long> {

  Optional<ResourceEntity> findByResourceId(String resourceId);

}
