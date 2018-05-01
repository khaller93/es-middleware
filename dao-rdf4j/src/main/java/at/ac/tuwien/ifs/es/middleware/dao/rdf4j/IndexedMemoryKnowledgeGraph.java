package at.ac.tuwien.ifs.es.middleware.dao.rdf4j;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.FullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.GremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.InMemoryGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KnowledgeGraphDAO;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.SelectQueryResult;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.RDFTerm;
import org.apache.commons.text.StringSubstitutor;
import org.eclipse.rdf4j.sail.lucene.LuceneSail;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * This class is an implementation of {@link RDF4JKnowledgeGraphDAO}. This class can be used for
 * testing the overlying services without starting a standalone triplestore instance.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Component("IndexedMemoryDB")
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class IndexedMemoryKnowledgeGraph extends RDF4JKnowledgeGraphDAO implements
    FullTextSearchDAO {

  private final static Logger logger = LoggerFactory.getLogger(IndexedMemoryKnowledgeGraph.class);

  private final static String FTS_QUERY =
      "PREFIX search: <http://www.openrdf.org/contrib/lucenesail#>\n"
          + "SELECT ?resource ?score\n"
          + "WHERE\n"
          + "{\n"
          + "  ?resource search:matches [\n"
          + "    search:query \"${keyword}\";\n"
          + "    search:score ?score\n"
          + "  ] .\n"
          + "  ${class-filter}\n"
          + "} ORDER BY DESC(?score)\n"
          + "${offset}\n"
          + "${limit}\n";

  private final static String[] FTS_CLASSES_FILTER = new String[]{
      "\n?resource a %s .\n",
      "\n?resource a ?class .\nFILTER(?class in (%s)) .\n"
  };

  private ApplicationContext context;

  /**
   * Creates a new {@link KnowledgeGraphDAO} in
   * memory that is indexed.
   */
  public IndexedMemoryKnowledgeGraph(@Autowired ApplicationContext context) {
    this.context = context;
    LuceneSail luceneSail = new LuceneSail();
    luceneSail.setParameter(LuceneSail.LUCENE_RAMDIR_KEY, "true");
    luceneSail.setBaseSail(new MemoryStore());
    init(luceneSail);
  }

  /**
   * Prepares a filter for the given list of class IRIs. This filter ensures that all returned
   * resources of the full-text-search belong to at least one of the given classes.
   *
   * @param classes of which a returned resource must be a member (at least of one given class).
   * @return a class filter for the full-text-search query.
   */
  private static String prepareFilter(List<BlankNodeOrIRI> classes) {
    if (classes == null || classes.isEmpty()) {
      return "";
    } else if (classes.size() == 1) {
      return String.format(FTS_CLASSES_FILTER[0],
          BlankOrIRIJsonUtil.stringForSPARQLResourceOf(classes.get(0)));
    } else {
      return String.format(FTS_CLASSES_FILTER[1],
          classes.stream().map(BlankOrIRIJsonUtil::stringForSPARQLResourceOf)
              .collect(Collectors.joining(", ")));
    }
  }

  @Override
  public List<Map<String, RDFTerm>> searchFullText(String keyword,
      List<BlankNodeOrIRI> classes, Integer offset, Integer limit) {
    logger
        .debug("FTS call for {} was triggered with parameters: offset={}, limit={}, and classes={}",
            keyword, offset, limit, classes);
    Map<String, String> valueMap = new HashMap<>();
    valueMap.put("keyword", keyword);
    valueMap.put("class-filter", prepareFilter(classes));
    valueMap.put("offset", offset != null ? "OFFSET " + offset.toString() : "");
    valueMap.put("limit", limit != null ? "LIMIT " + limit.toString() : "");
    String filledFtsQuery = new StringSubstitutor(valueMap).replace(FTS_QUERY);
    logger.trace(
        "Query resulting from FTS call for {} with parameters (offset={}, limit={}, classes={}).",
        filledFtsQuery, offset, limit, classes);
    return ((SelectQueryResult) this.query(filledFtsQuery, true)).value();
  }

  @Override
  public FullTextSearchDAO getFullTextSearchDAO() {
    return this;
  }

  @Override
  public GremlinDAO getGremlinDAO() {
    return context.getBean("InMemoryGremlin", InMemoryGremlinDAO.class);
  }
}
