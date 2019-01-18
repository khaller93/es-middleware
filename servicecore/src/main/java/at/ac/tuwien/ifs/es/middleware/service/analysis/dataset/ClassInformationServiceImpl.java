package at.ac.tuwien.ifs.es.middleware.service.analysis.dataset;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalysisEventStatus;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalysisPipelineProcessor;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.SPARQLService;
import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.RDFTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

/**
 * This is an implementation get {@link ClassInformationService} using the {@link SPARQLService}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Primary
@Service
public class ClassInformationServiceImpl implements ClassInformationService {

  private static final Logger logger = LoggerFactory.getLogger(ClassInformationService.class);

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

  private SPARQLService sparqlService;
  private AnalysisPipelineProcessor processor;

  @Autowired
  public ClassInformationServiceImpl(SPARQLService sparqlService,
      AnalysisPipelineProcessor processor) {
    this.sparqlService = sparqlService;
    this.processor = processor;
  }

  @PostConstruct
  private void setUp() {
    //processor.registerAnalysisService(this, true, false, false, null);
  }

  @Cacheable("sparql")
  @Override
  public Set<Resource> getAllClasses() {
    return ((SelectQueryResult) sparqlService.query(ALL_CLASSES_QUERY, true)).value()
        .stream().map(row -> new Resource((BlankNodeOrIRI) row.get("class")))
        .collect(Collectors.toSet());
  }

  @Override
  public void compute() {

  }

}
