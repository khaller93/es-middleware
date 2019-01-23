package at.ac.tuwien.ifs.es.middleware.service.analysis.dataset;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.service.analysis.RegisterForAnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.SPARQLService;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.RDFTerm;
import org.mapdb.DB;
import org.mapdb.IndexTreeList;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Primary
@Service
@RegisterForAnalyticalProcessing(name = AllResourcesServiceImpl.UID, requiresSPARQL = true)
public class AllResourcesServiceImpl implements AllResourcesService {

  private static final Logger logger = LoggerFactory.getLogger(AllResourcesServiceImpl.class);

  public static final String UID = "esm.service.analytics.dataset.all.resources";

  private static final int LOAD_LIMIT = 100000;

  private static final String ALL_RESOURCE_IRIS_QUERY = "SELECT DISTINCT ?resource WHERE {\n"
      + "    {?resource ?p1 _:o1}\n"
      + "     UNION\n"
      + "    {\n"
      + "        _:o2 ?p2 ?resource .\n"
      + "        FILTER (isIRI(?resource)) .\n"
      + "    } \n"
      + "}\n"
      + "OFFSET ${offset}\n"
      + "LIMIT ${limit}";

  private final SPARQLService sparqlService;
  private final DB mapDB;

  private final IndexTreeList<String> resourceList;

  @Autowired
  public AllResourcesServiceImpl(
      SPARQLService sparqlService,
      DB mapDB) {
    this.sparqlService = sparqlService;
    this.mapDB = mapDB;
    this.resourceList = mapDB
        .indexTreeList(AllResourcesServiceImpl.UID, Serializer.STRING).createOrOpen();
  }

  @Override
  public List<Resource> getResourceList() {
    return resourceList.stream().map(Resource::new).collect(Collectors.toList());
  }

  @Override
  public void compute() {
    int offset = 0;
    List<Map<String, RDFTerm>> results;
    String resourceQuery = new StrSubstitutor(Collections.singletonMap("limit", LOAD_LIMIT))
        .replace(ALL_RESOURCE_IRIS_QUERY);
    do {
      results = sparqlService.<SelectQueryResult>query(
          new StrSubstitutor(Collections.singletonMap("offset", offset)).replace(resourceQuery),
          true).value();
      if (results != null) {
        results.stream()
            .map(row -> BlankOrIRIJsonUtil.stringValue((BlankNodeOrIRI) row.get("resource")))
            .forEach(resourceList::add);
        offset += results.size();
        logger.trace("Loaded {} resources. {} resources already loaded..", results.size(), offset);
      } else {
        break;
      }
    } while (results.size() == LOAD_LIMIT);
    mapDB.commit();
  }


}
