package at.ac.tuwien.ifs.exploratorysearch.controller;

import at.ac.tuwien.ifs.exploratorysearch.service.SPARQLService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * This {@link RestController} handles incoming SPARQL queries and delegates them to the SPARQL
 * service.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/sparql")
public class SPARQLController {

  private static final Logger logger = LoggerFactory.getLogger(SPARQLService.class);

  private SPARQLService sparqlService;

  /**
   * Creates a new {@link SPARQLController} using the given {@code sparqlService} for executing the
   * queries.
   *
   * @param sparqlService which shall be used by this controller.
   */
  public SPARQLController(@Autowired SPARQLService sparqlService) {
    assert sparqlService != null;
    this.sparqlService = sparqlService;
  }

  @RequestMapping(value = "", method = {RequestMethod.GET, RequestMethod.POST})
  public String query(@RequestParam String query, @RequestParam String format,
      @RequestParam(name = "infer", defaultValue = "false", required = false) boolean inference) {
    //TODO: Implement
    return null;
  }

  @RequestMapping(value = "/update")
  public String update(@RequestParam String query) {
    //TODO: Implement
    return null;
  }
}
