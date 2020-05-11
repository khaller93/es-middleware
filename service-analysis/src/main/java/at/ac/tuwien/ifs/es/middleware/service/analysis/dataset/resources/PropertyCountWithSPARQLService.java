package at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources;

import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.Resource;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.sparql.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.service.analysis.RegisterForAnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.SPARQLService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Literal;
import org.apache.commons.rdf.api.RDFTerm;
import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Primary
@Service
@RegisterForAnalyticalProcessing(name = PropertyCountWithSPARQLService.UID, requiresSPARQL = true)
public class PropertyCountWithSPARQLService implements PropertyCountService {

  private static final Logger logger = LoggerFactory
      .getLogger(PropertyCountWithSPARQLService.class);

  public static final String UID = "esm.service.analytics.dataset.resource.property.count";

  private static final String QUERY = "SELECT ?p (count(*) as ?cnt) WHERE { \n"
      + "\t?s ?p ?o .\n"
      + "} GROUP BY ?p";


  private final SPARQLService sparqlService;
  private final DB mapDB;

  private final HTreeMap<String, Long> propertyCountMap;

  @Autowired
  public PropertyCountWithSPARQLService(
      SPARQLService sparqlService, DB mapDB) {
    this.sparqlService = sparqlService;
    this.mapDB = mapDB;
    this.propertyCountMap = mapDB.hashMap(UID, Serializer.STRING, Serializer.LONG).createOrOpen();
  }

  @Override
  public Optional<Long> getCountOf(Resource property) {
    Long value = propertyCountMap.get(property.getId());
    if (value != null) {
      return Optional.of(value);
    }
    return Optional.empty();
  }

  @Override
  public void compute() {
    Map<String, Long> propertyCountCache = new HashMap<>();
    List<Map<String, RDFTerm>> result = sparqlService.<SelectQueryResult>query(QUERY, true).value();
    int n = 0;
    for (Map<String, RDFTerm> row : result) {
      propertyCountCache.put(new Resource((IRI) row.get("p")).getId(),
          Long.parseLong(((Literal) row.get("cnt")).getLexicalForm()));
      n++;
      if (n % 1000 == 0) {
        logger.trace("Processed {} property counts.", n);
        propertyCountMap.putAll(propertyCountCache);
        propertyCountCache.clear();
      }
    }
    mapDB.commit();
  }

}
