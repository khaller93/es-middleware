package at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes;

import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.Resource;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.serializer.RDFTermJsonUtil;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.SPARQLService;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.sparql.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.service.analysis.RegisterForAnalyticalProcessing;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.mapdb.DB;
import org.mapdb.HTreeMap.KeySet;
import org.mapdb.Serializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * This is an implementation get {@link AllClassesService} using the {@link SPARQLService}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Primary
@Service
@RegisterForAnalyticalProcessing(name = AllClassesWithSPARQLService.UID, requiresSPARQL = true)
public class AllClassesWithSPARQLService implements AllClassesService {

  public static final String UID = "esm.service.analytics.dataset.all.classes";

  private static final String ALL_CLASSES_QUERY = "SELECT DISTINCT ?class WHERE {\n"
      + "    {_:a a ?class}\n"
      + "     UNION\n"
      + "    {?class a rdfs:Class}\n"
      + "     UNION\n"
      + "    {?class rdfs:subClassOf _:b}\n"
      + "     UNION\n"
      + "    {_:c rdfs:subClassOf ?class}\n"
      + "    FILTER(isIRI(?class)) .\n"
      + "}";

  private final SPARQLService sparqlService;
  private final DB mapDb;

  private final KeySet<String> classList;

  @Autowired
  public AllClassesWithSPARQLService(SPARQLService sparqlService, DB mapDb) {
    this.sparqlService = sparqlService;
    this.mapDb = mapDb;
    this.classList = mapDb
        .hashSet(AllClassesWithSPARQLService.UID, Serializer.STRING).createOrOpen();
  }

  @Override
  public Set<Resource> getAllClasses() {
    return classList.stream().map(Resource::new).collect(Collectors.toSet());
  }

  @Override
  public void compute() {
    classList.addAll(((SelectQueryResult) sparqlService.query(ALL_CLASSES_QUERY, true)).value()
        .stream().map(row -> RDFTermJsonUtil.stringValue((BlankNodeOrIRI) row.get("class")))
        .collect(Collectors.toSet()));
    mapDb.commit();
  }

}
