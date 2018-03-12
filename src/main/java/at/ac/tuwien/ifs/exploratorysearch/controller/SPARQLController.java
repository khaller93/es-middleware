package at.ac.tuwien.ifs.exploratorysearch.controller;

import at.ac.tuwien.ifs.exploratorysearch.dao.knowledgegraph.exception.SPARQLExecutionException;
import at.ac.tuwien.ifs.exploratorysearch.dto.exception.QueryResultFormatException;
import at.ac.tuwien.ifs.exploratorysearch.service.SPARQLService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
@Api(value = "SPARQL Endpoint", description = "Operations for executing SPARQL queries.")
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
    this.sparqlService = sparqlService;
  }

  @RequestMapping(value = "", method = {RequestMethod.GET})
  @ApiOperation(value = "Query the managed knowledge graph using SPARQL, i.e. SELECT/ASK/CONSTRUCT")
  public ResponseEntity<byte[]> query(
      @ApiParam(value = "Select, Ask or Construct SPARQL query.", required = true) @RequestParam String query,
      @ApiParam(value = "MIME type of the returned query result.", required = true) @RequestParam String format,
      @ApiParam("Whether inferred statements should be considered/returned, or not.")
      @RequestParam(name = "infer", defaultValue = "false", required = false) boolean inference)
      throws SPARQLExecutionException, QueryResultFormatException {
    logger.info("SPARQL Query request with query '{}'. Param: {Format: {}, Inference: {}}", query,
        format, inference);
    byte[] response = sparqlService.query(query, inference).transform(format);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.valueOf(format));
    return new ResponseEntity<>(response, headers, HttpStatus.OK);
  }

  @RequestMapping(value = "/alter", method = {RequestMethod.POST})
  @ApiOperation(value = "Alter the managed knowledge graph using SPARQL i.e. INSERT/DELETE")
  public String update(@RequestParam String query) {
    //TODO: Implement
    return null;
  }
}
