package at.ac.tuwien.ifs.es.middleware.controller;

import at.ac.tuwien.ifs.es.middleware.dto.exception.KnowledgeGraphSPARQLException;
import at.ac.tuwien.ifs.es.middleware.dto.exception.MalformedSPARQLQueryException;
import at.ac.tuwien.ifs.es.middleware.dto.exception.SPARQLExecutionException;
import at.ac.tuwien.ifs.es.middleware.dto.exception.SPARQLResultFormatException;
import at.ac.tuwien.ifs.es.middleware.dto.exception.SPARQLResultSerializationException;
import at.ac.tuwien.ifs.es.middleware.service.sparql.SPARQLService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
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

  /**
   * Helper method for sorting and preparing the given {@code mimeTypes}.
   *
   * @param mimeTypes that shall be processed.
   * @return sorted {@link List} of mime types.
   */
  private List<String> prepareMimeTypes(List<MediaType> mimeTypes) {
    MediaType.sortBySpecificityAndQuality(mimeTypes);
    return mimeTypes.stream().map(m -> String.format("%s/%s", m.getType(), m.getSubtype()))
        .collect(Collectors.toList());
  }

  /**
   * Helper method for SPARQL query requests.
   */
  private ResponseEntity<byte[]> issueSPARQLQuery(String query, List<String> mimeTypes,
      boolean inference) throws KnowledgeGraphSPARQLException, SPARQLResultFormatException {
    byte[] response = sparqlService.query(query, inference).transform(mimeTypes);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.valueOf(mimeTypes.get(0)));
    return new ResponseEntity<>(response, headers, HttpStatus.OK);
  }

  @GetMapping(value = "")
  @ApiOperation(value = "Query the managed knowledge graph using corresponding SPARQL operations (SELECT/ASK/DESCRIBE/CONSTRUCT).")
  @ApiResponses({
      @ApiResponse(code = 200, message = "Query was executed successfully. The result can be found in the response body."),
      @ApiResponse(code = 400, message = "Bad request is issued, if invalid parameters have been specified or the query is malformed (see documentation)."),
      @ApiResponse(code = 406, message = "None of the given MIME Types is supported."),
      @ApiResponse(code = 500, message = "This response indicates that the query execution failed. Reason can be found in the response body."),
  })
  public ResponseEntity<byte[]> queryGet(
      @ApiParam(value = "SELECT, ASK, DESCRIBE or CONSTRUCT query that shall be executed.", required = true) @RequestParam String query,
      @ApiParam(value = "Accepted MIME type for query response.", required = true) @RequestHeader(value = "Accept") String mimeTypes,
      @ApiParam("Whether inferred statements should be considered/returned, or not.")
      @RequestParam(name = "infer", defaultValue = "false", required = false) boolean inference)
      throws KnowledgeGraphSPARQLException, SPARQLResultFormatException {
    logger.info("SPARQL Query request with query '{}'. Param: {Accept: {}, Inference: {}}", query,
        mimeTypes, inference);
    return issueSPARQLQuery(query, prepareMimeTypes(MediaType.parseMediaTypes(mimeTypes)),
        inference);
  }

  @PostMapping(value = "", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  @ApiOperation(value = "Query the managed knowledge graph using corresponding SPARQL operations (SELECT/ASK/DESCRIBE/CONSTRUCT).")
  @ApiResponses({
      @ApiResponse(code = 200, message = "Query was executed successfully. The result can be found in the response body."),
      @ApiResponse(code = 400, message = "Bad request is issued, if invalid parameters have been specified or the query is malformed (see documentation)."),
      @ApiResponse(code = 406, message = "None of the given MIME Types is supported."),
      @ApiResponse(code = 500, message = "This response indicates that the query execution failed. Reason can be found in the response body."),
  })
  public ResponseEntity<byte[]> queryPost(
      @ApiParam(value = "SELECT, ASK, DESCRIBE or CONSTRUCT query that shall be executed.", required = true) @RequestParam String query,
      @ApiParam(value = "Accepted MIME type for query response.") @RequestHeader(value = "Accept") String mimeTypes,
      @ApiParam("Whether inferred statements should be considered/returned, or not.")
      @RequestParam(name = "infer", defaultValue = "false", required = false) boolean inference)
      throws KnowledgeGraphSPARQLException, SPARQLResultFormatException {
    logger.info("SPARQL Query request with query '{}'. Param: {Accept: {}, Inference: {}}", query,
        mimeTypes, inference);
    return issueSPARQLQuery(query, prepareMimeTypes(MediaType.parseMediaTypes(mimeTypes)),
        inference);
  }

  @PostMapping(value = "/update", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  @ApiOperation(value = "Alter the managed knowledge graph using corresponding SPARQL operations (INSERT/DELETE).")
  @ApiResponses({
      @ApiResponse(code = 200, message = "Update was executed successfully."),
      @ApiResponse(code = 400, message = "Bad request is issued, if invalid parameters have been specified (see documentation)."),
      @ApiResponse(code = 500, message = "This response indicates that the update query failed. Reason can be found in the response body."),
  })
  public void update(
      @ApiParam(value = "Update query that shall be executed.", required = true) @RequestParam(name = "update") String query)
      throws KnowledgeGraphSPARQLException {
    logger.info("SPARQL Update request with query '{}'.", query);
    sparqlService.update(query);
  }

  /* Exception handler for SPARQL controller */

  @ExceptionHandler(SPARQLResultSerializationException.class)
  public ResponseEntity<String> handleSerializationException(
      SPARQLResultSerializationException ex, HttpServletRequest request) {
    logger.error("Request '{}' accepting '{}' caused a serialization exception: {}.",
        request.getRequestURI(), request.getHeader("Accept"), ex.getMessage());
    return new ResponseEntity<>(
        "Result of SPARQL query could not be served, due to internal server error.",
        HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(SPARQLResultFormatException.class)
  public ResponseEntity<String> handleResultFormatException(
      SPARQLResultFormatException ex, HttpServletRequest request) {
    logger.error("Request '{}' has specified unsupported mime types. {}", request.getRequestURI(),
        ex.getMessage());
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_ACCEPTABLE);
  }

  @ExceptionHandler(SPARQLExecutionException.class)
  public ResponseEntity<String> handleSPARQLExecutionException(
      SPARQLExecutionException ex, HttpServletRequest request) {
    logger.error("Request '{}' could not be executed successfully. {}", request.getRequestURI(),
        ex.getMessage());
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(MalformedSPARQLQueryException.class)
  public ResponseEntity<String> handleMalformedSPARQLQueryException(
      MalformedSPARQLQueryException ex, HttpServletRequest request) {
    logger.error("Request '{}' has handed over malformed query. {}", request.getRequestURI(),
        ex.getMessage());
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
  }

}
