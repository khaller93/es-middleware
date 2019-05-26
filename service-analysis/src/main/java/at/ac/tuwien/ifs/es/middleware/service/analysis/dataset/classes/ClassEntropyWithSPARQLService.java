package at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes;

import at.ac.tuwien.ifs.es.middleware.common.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.common.knowledgegraph.GremlinService;
import at.ac.tuwien.ifs.es.middleware.common.knowledgegraph.SPARQLService;
import at.ac.tuwien.ifs.es.middleware.sparql.result.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.common.exploration.context.util.result.RDFTermJsonUtil;
import at.ac.tuwien.ifs.es.middleware.common.analysis.RegisterForAnalyticalProcessing;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.Literal;
import org.apache.commons.rdf.api.RDFTerm;
import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * This is an implementation get {@link ClassEntropyService} using the {@link GremlinService}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Service
@RegisterForAnalyticalProcessing(name = ClassEntropyWithSPARQLService.UID,
    requiresSPARQL = true, prerequisites = {AllClassesService.class}, disabled = true)
public class ClassEntropyWithSPARQLService implements ClassEntropyService {

  private static final Logger logger = LoggerFactory.getLogger(ClassEntropyService.class);

  public static final String UID = "esm.service.analytics.dataset.classentropy.sparql";

  private static final int LOAD_SIZE = 1000;

  private static final String CLASS_DISTRIBUTION_QUERY =
      "select ?class (count(DISTINCT ?s) as ?cnt) where {\n"
          + "    VALUES ?class {\n"
          + "        %s\n"
          + "    }\n"
          + "    ?s a/rdfs:subClassOf* ?class .\n"
          + "} GROUP BY ?class";

  private static final String RESOURCE_TOTAL_QUERY = "select (count(DISTINCT ?s) as ?cnt) where {\n"
      + "    ?s a [] .\n"
      + "}";

  private SPARQLService sparqlService;
  private AllClassesService allClassesService;
  private DB mapDB;

  private final HTreeMap<String, Double> classEntropyMap;

  @Autowired
  public ClassEntropyWithSPARQLService(SPARQLService sparqlService,
      AllClassesService allClassesService,
      @Qualifier("persistent-mapdb") DB mapDB) {
    this.sparqlService = sparqlService;
    this.allClassesService = allClassesService;
    this.mapDB = mapDB;
    this.classEntropyMap = mapDB.hashMap(UID, Serializer.STRING, Serializer.DOUBLE)
        .createOrOpen();
  }

  @Override
  public Double getEntropyForClass(Resource resource) {
    return classEntropyMap.get(resource.getId());
  }

  private void processSPARQLResult(List<Resource> classList, List<Map<String, RDFTerm>> result,
      long total) {
    Map<Resource, Double> classMap = new HashMap<>();
    for (Resource clazz : classList) {
      classMap.put(clazz, -Math.log(1.0 / total));
    }
    for (Map<String, RDFTerm> classRow : result) {
      long classCount = Long.parseLong(((Literal) classRow.get("cnt")).getLexicalForm());
      Resource classResource = new Resource((BlankNodeOrIRI) classRow.get("class"));
      classMap.put(classResource, -Math.log(((double) classCount) / total));
    }
    for (Entry<Resource, Double> classEntry : classMap.entrySet()) {
      classEntropyMap.put(classEntry.getKey().getId(), classEntry.getValue());
    }
  }

  @Override
  public void compute() {
    List<Map<String, RDFTerm>> totalResult = sparqlService.<SelectQueryResult>query(
        RESOURCE_TOTAL_QUERY, true).value();
    if (!totalResult.isEmpty()) {
      long total = Long.parseLong(((Literal) totalResult.get(0).get("cnt")).getLexicalForm());
      if (total > 0) {
        int n = 0;
        List<Resource> classList = new LinkedList<>();
        Set<Resource> allClasses = allClassesService.getAllClasses();
        for (Resource classResource : allClasses) {
          classList.add(classResource);
          n++;
          if (n == LOAD_SIZE) {
            processSPARQLResult(classList, sparqlService.<SelectQueryResult>query(
                String.format(CLASS_DISTRIBUTION_QUERY,
                    classList.stream().map(c -> RDFTermJsonUtil.stringForSPARQLResourceOf(c.value())).collect(
                        Collectors.joining("\n"))),
                true).value(), total);
            n = 0;
            classList = new LinkedList<>();
          }
        }
        if (n > 0) {
          processSPARQLResult(classList,
              sparqlService.<SelectQueryResult>query(String.format(CLASS_DISTRIBUTION_QUERY,
                  classList.stream().map(c -> RDFTermJsonUtil.stringForSPARQLResourceOf(c.value())).collect(
                      Collectors.joining("\n"))), true).value(), total);
        }
        mapDB.commit();
      }
    }
  }

}
