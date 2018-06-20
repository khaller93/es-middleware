package at.ac.tuwien.ifs.es.middleware.dao.rdf4j.conf;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KnowledgeGraphDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.SPARQLSyncingGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.IndexedMemoryKnowledgeGraph;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * This is a conf for {@link IndexedMemoryKnowledgeGraph}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Component("IndexedMemoryDB")
public class IndexedMemoryKnowledgeGraphConfig implements KnowledgeGraphDAOConfig {

  @Value("${esm.db.gremlin.choice}")
  private String gremlinChoice;

  private ApplicationContext context;

  public IndexedMemoryKnowledgeGraphConfig(ApplicationContext context) {
    this.context = context;
  }

  @Override
  public KGSparqlDAO getSparqlDAO() {
    return context.getBean(IndexedMemoryKnowledgeGraph.class);
  }

  @Override
  public KGFullTextSearchDAO getFullTextSearchDAO() {
    return context.getBean(IndexedMemoryKnowledgeGraph.class);
  }

  @Override
  public KGGremlinDAO getGremlinDAO() {
    return context.getBean(
        gremlinChoice != null && !gremlinChoice.isEmpty() ? gremlinChoice : "InMemoryGremlin",
        SPARQLSyncingGremlinDAO.class);
  }
}
