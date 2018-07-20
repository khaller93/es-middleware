package at.ac.tuwien.ifs.es.middleware.dao.graphdb.conf;

import at.ac.tuwien.ifs.es.middleware.dao.graphdb.EmbeddedGraphDbDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * This is a conf for embedded GraphDBs.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Component("EmbeddedGraphDB")
public class EmbeddedGraphDbConfig extends GraphDbConfig {

  @Autowired
  public EmbeddedGraphDbConfig(ApplicationContext context,
      @Value("${graphdb.fts.choice:#{null}}") String fullTextSearchChoice,
      @Value("${esm.db.gremlin.choice:#{null}}") String gremlinChoice) {
    super(context, fullTextSearchChoice, gremlinChoice);
  }

  @Bean
  @Override
  public KGSparqlDAO getSparqlDAO() {
    return getContext().getBean(EmbeddedGraphDbDAO.class);
  }

}
