package at.ac.tuwien.ifs.es.middleware.dao.stardog;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KnowledgeGraphDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.RDF4JKnowledgeGraphDAO;
import at.ac.tuwien.ifs.es.middleware.dto.exception.KnowledgeGraphDAOException;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.SelectQueryResult;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.RDFTerm;
import org.apache.commons.text.StringSubstitutor;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * This is an implementation of {@link KnowledgeGraphDAO} and {@link KGFullTextSearchDAO} for
 * Stardog using RDF4J.
 *
 * @author Kevin Haller
 * @version 1.0
 * @see <a href="https://www.stardog.com/docs/">Stardog</a>
 * @since 1.0
 */
@Lazy
@Component("Stardog")
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class StardogKnowledgeGraphDAO extends RDF4JKnowledgeGraphDAO implements
    KGFullTextSearchDAO {

  private static final Logger logger = LoggerFactory.getLogger(StardogKnowledgeGraphDAO.class);

  private static final String SEARCH_QUERY = "SELECT DISTINCT ?resource ?score\n"
      + "WHERE {\n"
      + "?resource ?p ?l.\n"
      + "(?l ?score) <tag:stardog:api:property:textMatch> '${keyword}'.\n"
      + "${classes-filter}\n"
      + "}\n"
      + "ORDER BY DESC(?score)"
      + "${offset}\n"
      + "${limit}";

  private final static String[] FTS_CLASSES_FILTER = new String[]{
      "\n?resource a %s .\n",
      "\n?resource a ?class .\nFILTER(?class in (%s)) .\n"
  };

  private StardogConfiguration stardogConfiguration;

  /**
   * Creates a new Stardog DAO for {@link KnowledgeGraphDAO} and {@link KGFullTextSearchDAO}.
   *
   * @param stardogConfiguration that specifies properties for Stardog.
   */
  @Autowired
  public StardogKnowledgeGraphDAO(StardogConfiguration stardogConfiguration,
      ApplicationContext context) {
    super(context);
    this.stardogConfiguration = stardogConfiguration;
  }

  @PostConstruct
  public void setUp() {
    SPARQLRepository sparqlRepository = new SPARQLRepository(
        stardogConfiguration.getSPARQLEndpointURL());
    sparqlRepository.setUsernameAndPassword(stardogConfiguration.getUsername(),
        stardogConfiguration.getPassword());
    this.init(sparqlRepository);
  }

  @Override
  public List<Map<String, RDFTerm>> searchFullText(String keyword, List<BlankNodeOrIRI> classes,
      Integer offset, Integer limit) throws KnowledgeGraphDAOException {
    logger.debug("Searching for '{}' of classes {} with limit={}, offset={}.", keyword, classes,
        offset, limit);
    Map<String, String> queryValuesMap = new HashMap<>();
    queryValuesMap.put("keyword", keyword);
    if (classes != null && !classes.isEmpty()) {
      if (classes.size() == 1) {
        queryValuesMap.put("classes-filter", String.format(FTS_CLASSES_FILTER[0],
            BlankOrIRIJsonUtil.stringForSPARQLResourceOf(classes.get(0))));
      } else {
        queryValuesMap.put("classes-filter", String.format(FTS_CLASSES_FILTER[1],
            classes.stream().map(BlankOrIRIJsonUtil::stringForSPARQLResourceOf).collect(
                Collectors.joining(", "))));
      }
    } else {
      queryValuesMap.put("classes-filter", "");
    }
    queryValuesMap.put("offset", offset != null ? String.format("OFFSET %d", offset) : "");
    queryValuesMap.put("limit", limit != null ? String.format("LIMIT %d", limit) : "");
    String searchQuery = new StringSubstitutor(queryValuesMap).replace(SEARCH_QUERY);
    logger
        .trace("Searching with '{}' for '{}' of classes {} with limit={}, offset={}.", searchQuery,
            keyword, classes, offset, limit);
    return ((SelectQueryResult) query(searchQuery, true)).value();
  }

  @Override
  public KGFullTextSearchDAO getFullTextSearchDAO() {
    return this;
  }

  @Override
  public KGGremlinDAO getGremlinDAO() {
    //TODO: Implement.
    return null;
  }
}
