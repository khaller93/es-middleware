package at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes.hierarchy;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.service.analysis.RegisterForAnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.SameAsResourceService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.SPARQLService;
import java.util.Set;
import org.mapdb.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * This class is a concrete implementation of {@link ClassHierarchyService} that uses {@link
 * SPARQLService} to gather the class hierarchy.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Service
@RegisterForAnalyticalProcessing(name = ClassHierarchyWithSPARQLService.UID, requiresSPARQL = true,
    prerequisites = {SameAsResourceService.class})
public class ClassHierarchyWithSPARQLService implements ClassHierarchyService {

  private static final Logger logger = LoggerFactory
      .getLogger(ClassHierarchyWithSPARQLService.class);

  public static final String UID = "esm.service.analytics.dataset.class.hierarchy";

  private static final String ALL_SUBCLASSES_QUERY = "SELECT DISTINCT ?class ?superClass WHERE {\n"
      + "    {\n"
      + "      _:a a ?class\n"
      + "    } UNION {\n"
      + "      ?class a rdfs:Class\n"
      + "    } UNION {\n"
      + "      ?class rdfs:subClassOf _:b\n"
      + "    } UNION {\n"
      + "      _:c rdfs:subClassOf ?class\n"
      + "    }\n"
      + "    ?class rdfs:subClassOf+ ?superClass .\n"
      + "    FILTER (?class != ?superClass) .\n"
      + "    FILTER NOT EXISTS {\n"
      + "        ?class rdfs:subClassOf+ ?anotherSuperClass .\n"
      + "        ?anotherSuperClass rdfs:subClassOf+ ?superClass .\n"
      + "        FILTER (isIRI(?anotherClass)) .\n"
      + "    }\n"
      + "    FILTER(isIRI(?class) && isIRI(?superClass)) .\n"
      + "}\n"
      + "OFFSET %d\n"
      + "LIMIT %d";

  private final SPARQLService sparqlService;
  private final SameAsResourceService sameAsResourceService;
  private final DB mapDB;

  public ClassHierarchyWithSPARQLService(
      SPARQLService sparqlService,
      SameAsResourceService sameAsResourceService, DB mapDB) {
    this.sparqlService = sparqlService;
    this.sameAsResourceService = sameAsResourceService;
    this.mapDB = mapDB;
  }

  @Override
  public Set<Resource> getMostSpecificClasses(Set<Resource> classes) {
    return null;
  }

  @Override
  public void compute() {

  }
}
