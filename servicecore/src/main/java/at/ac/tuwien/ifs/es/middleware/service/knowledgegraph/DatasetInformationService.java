package at.ac.tuwien.ifs.es.middleware.service.knowledgegraph;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.schema.PGS;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.GremlinService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.SPARQLService;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.Literal;
import org.apache.commons.rdf.api.RDFTerm;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * This service computes and maintains information about the knowledge graph.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class DatasetInformationService {

  private static final Logger logger = LoggerFactory.getLogger(DatasetInformationService.class);

  private static final String DUPLICATES_QUERY =
      "SELECT ?s (GROUP_CONCAT(str(?z); separator=\" >|< \") AS ?sameAs) WHERE {\n"
          + "  ?s owl:sameAs ?z.\n"
          + "  FILTER(?s != ?z). \n"
          + "} GROUP BY ?s";

  private SPARQLService sparqlService;
  private GremlinService gremlinService;
  private PGS schema;

  /**
   * Creates a new {@link DatasetInformationService} that implements common queries for the
   * knowledge graph.
   *
   * @param sparqlService that shall be used to query the knowledge graph.
   * @param gremlinService that shall be used to query the knowledge graph.
   */
  @Autowired
  public DatasetInformationService(SPARQLService sparqlService, GremlinService gremlinService) {
    this.sparqlService = sparqlService;
    this.gremlinService = gremlinService;
    this.schema = gremlinService.getPropertyGraphSchema();
  }

  /**
   * Gets all the classes in the knowledge graph. Classes are those, which have a {@code rdf:type}
   * relationship with {@code rdfs:Class} and/or have an incoming {@code rdf:type} relationship.
   *
   * @return all the classes in the knowledge graph.
   */
  @Cacheable("gremlin")
  @SuppressWarnings("unchecked")
  public List<Resource> getAllClasses() {
    return gremlinService.traversal().V()
        .union(__.inE("http://www.w3.org/1999/02/22-rdf-syntax-ns#type").inV(),
            __.as("c").out("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
                .has(schema.iri().identifierAsString(),
                    "http://www.w3.org/2000/01/rdf-schema#Class")
                .select("c")).dedup()
        .toList()
        .stream().map(v -> new Resource(schema.iri().<String>apply(v)))
        .collect(Collectors.toList());
  }

}
