package at.ac.tuwien.ifs.es.middleware.dao.graphdb;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.FullTextSearchDAO;
import java.util.List;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

@Lazy
@Repository("GraphDBLucene")
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class GraphDbLucene implements FullTextSearchDAO {

  @Override
  public List<BlankNodeOrIRI> searchFullText(String keyword, List<BlankNodeOrIRI> clazzes) {
    return null;
  }
}
