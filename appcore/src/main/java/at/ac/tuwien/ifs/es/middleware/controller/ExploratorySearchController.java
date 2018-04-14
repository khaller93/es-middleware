package at.ac.tuwien.ifs.es.middleware.controller;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.request.DynamicExplorationFlowRequest;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.service.exception.ExplorationFlowSpecificationException;
import at.ac.tuwien.ifs.es.middleware.service.exploration.factory.CommonExplorationFlowFactory;
import at.ac.tuwien.ifs.es.middleware.service.exploration.factory.DynamicExplorationFlowFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * This {@link RestController} handles all incoming requests for the exploratory search services.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/explore")
@Api(value = "Exploratory Search Endpoint",
    description = "Operations to explore the managed knowledge graph.")
public class ExploratorySearchController {

  private static final Logger logger = LoggerFactory.getLogger(ExploratorySearchController.class);

  private CommonExplorationFlowFactory commonExplorationFlowFactory;
  private DynamicExplorationFlowFactory dynamicExplorationFlowFactory;

  public ExploratorySearchController(
      @Autowired CommonExplorationFlowFactory commonExplorationFlowFactory,
      @Autowired DynamicExplorationFlowFactory dynamicExplorationFlowFactory) {
    this.commonExplorationFlowFactory = commonExplorationFlowFactory;
    this.dynamicExplorationFlowFactory = dynamicExplorationFlowFactory;
  }

  @GetMapping(value = "/with/custom/flow", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Apply a custom exploration flow.")
  public ExplorationContext exploreWithFullTextSearch(
      @RequestBody DynamicExplorationFlowRequest request)
      throws ExplorationFlowSpecificationException {
    return dynamicExplorationFlowFactory.constructFlow(request).execute();
  }

  @GetMapping(value = "/with/fts/{keyword}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Apply full-text-search with the given keyword.")
  public ExplorationContext exploreWithFullTextSearch(
      @ApiParam(value = "Keyword for which corresponding resources shall be found.", required = true) @PathVariable String keyword,
      @ApiParam(value = "Specifies that the 'offsetNr' topmost resources shall be skipped.", required = false) @RequestParam(required = false) Integer offsetNr,
      @ApiParam(value = "Specifies that only the 'limitNr' topmost resources shall be returned.") @RequestParam(required = false) Integer limitNr)
      throws ExplorationFlowSpecificationException {
    return commonExplorationFlowFactory
        .constructFullTextSearchFlow(keyword, null, limitNr, offsetNr).execute();
  }

}
