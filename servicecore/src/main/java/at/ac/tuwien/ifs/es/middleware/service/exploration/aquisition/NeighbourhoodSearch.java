package at.ac.tuwien.ifs.es.middleware.service.exploration.aquisition;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KnowledgeGraphDAO;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.ExplorationContext;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Lazy
@Component
public class NeighbourhoodSearch implements AcquisitionSource {

  private KnowledgeGraphDAO knowledgeGraphDAO;

  public NeighbourhoodSearch(@Autowired @Qualifier("SpecifiedKnowledgeGraphDAO")
      KnowledgeGraphDAO knowledgeGraphDAO) {
    this.knowledgeGraphDAO = knowledgeGraphDAO;
  }

  @Override
  public Class<?> getParameterClass() {
    return null;
  }

  @Override
  public ExplorationContext apply(JsonNode jsonNode) {
    return null;
  }

}
