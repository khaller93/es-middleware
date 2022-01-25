package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.DependsOn;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Component
public class DAODependencyGraphService {

  private final KGSparqlDAO sparqlDAO;
  private final KGGremlinDAO gremlinDAO;
  private final KGFullTextSearchDAO fullTextSearchDAO;

  private Set<String> sparqlRequirements;
  private Set<String> gremlinRequirements;
  private Set<String> ftsRequirements;

  @Autowired
  public DAODependencyGraphService(
      @Qualifier("getSparqlDAO") KGSparqlDAO sparqlDAO,
      @Qualifier("getGremlinDAO") KGGremlinDAO gremlinDAO,
      @Qualifier("getFullTextSearchDAO") KGFullTextSearchDAO fullTextSearchDAO) {
    this.sparqlDAO = sparqlDAO;
    this.gremlinDAO = gremlinDAO;
    this.fullTextSearchDAO = fullTextSearchDAO;
  }

  public Set<String> getSPARQLRequirements() {
    if (sparqlRequirements == null) {
      sparqlRequirements = getRequirements(sparqlDAO);
    }
    return sparqlRequirements;
  }

  public Set<String> getGremlinRequirements() {
    if (gremlinRequirements == null) {
      gremlinRequirements = getRequirements(gremlinDAO);
    }
    return gremlinRequirements;
  }

  public Set<String> getFTSRequirements() {
    if (ftsRequirements == null) {
      ftsRequirements = getRequirements(fullTextSearchDAO);
    }
    return ftsRequirements;
  }

  private Set<String> getRequirements(KGDAO kgdao) {
    checkArgument(kgdao != null,
        "The KG DAO for which the sparqlRequirements shall be fetched must not be null.");
    DependsOn annotation = kgdao.getClass().getAnnotation(DependsOn.class);
    if (annotation != null) {
      Set<String> requirements = new HashSet<>();
      if (annotation.sparql()) {
        requirements.add(KGSparqlDAO.class.getName());
      }
      if (annotation.gremlin()) {
        requirements.add(KGGremlinDAO.class.getName());
      }
      if (annotation.fulltextSearch()) {
        requirements.add(KGFullTextSearchDAO.class.getName());
      }
      return requirements;
    } else {
      return Collections.emptySet();
    }
  }

}
