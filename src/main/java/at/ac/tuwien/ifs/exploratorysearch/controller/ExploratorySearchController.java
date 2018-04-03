package at.ac.tuwien.ifs.exploratorysearch.controller;

import at.ac.tuwien.ifs.exploratorysearch.dto.exploration.ExplorationResponse;
import at.ac.tuwien.ifs.exploratorysearch.service.exception.ExplorationFlowSpecificationException;
import at.ac.tuwien.ifs.exploratorysearch.service.exploration.ExplorationFlowFactory;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

  private ExplorationFlowFactory explorationFlowFactory;

  public ExploratorySearchController(@Autowired
      ExplorationFlowFactory explorationFlowFactory) {
    this.explorationFlowFactory = explorationFlowFactory;
  }

  @GetMapping(value = "/with/fts/{keyword}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Apply full-text-search with the given keyword.")
  public ExplorationResponse exploreWithFullTextSearch(
      @ApiParam(value = "Keyword for which corresponding resources shall be found.", required = true) @PathVariable String keyword,
      @ApiParam(value = "Specifies that the 'offsetNr' topmost resources shall be skipped.", required = false) @RequestParam(required = false) Integer offsetNr,
      @ApiParam(value = "Specifies that only the 'limitNr' topmost resources shall be returned.") @RequestParam(required = false) Integer limitNr)
      throws ExplorationFlowSpecificationException {
    ObjectNode parameterMap = JsonNodeFactory.instance.objectNode();
    ObjectNode fts = JsonNodeFactory.instance.objectNode();
    fts.put("keyword", keyword);
    parameterMap.set("fts", fts);
    return explorationFlowFactory.get("fts").execute(parameterMap);
  }

}
