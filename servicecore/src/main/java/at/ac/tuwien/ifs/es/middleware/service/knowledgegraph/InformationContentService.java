package at.ac.tuwien.ifs.es.middleware.service.knowledgegraph;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.SPARQLService;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * This service computes and maintains information content information about classes.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class InformationContentService {

  private SPARQLService sparqlService;

  public InformationContentService(@Autowired SPARQLService sparqlService) {
    this.sparqlService = sparqlService;
  }

  @Cacheable("ic-class-map")
  public Map<Resource,Double> getInformationContentForClasses(){
    return null;
  }

}
