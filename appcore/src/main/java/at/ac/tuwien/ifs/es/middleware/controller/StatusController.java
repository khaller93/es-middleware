package at.ac.tuwien.ifs.es.middleware.controller;

import at.ac.tuwien.ifs.es.middleware.VersionManager;
import at.ac.tuwien.ifs.es.middleware.controller.meta.Beat;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.status.KGDAOStatus;
import at.ac.tuwien.ifs.es.middleware.service.systemstatus.BackendObserverService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This {@link RestController} provides methods to check the status beat get this middleware.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/")
@Api(value = "Heart beat", description = "Methods to check the status get this middleware")
public class StatusController {

  private final BackendObserverService backendObserverService;
  private final String appVersion;

  @Autowired
  public StatusController(BackendObserverService backendObserverService,
      VersionManager versionManager) {
    this.backendObserverService = backendObserverService;
    this.appVersion = versionManager.getVersion();
  }

  @GetMapping(value = "/heartbeat")
  @ApiOperation(value = "Requests a short response from this microservice.")
  @ApiResponses({
      @ApiResponse(code = 200, message = "A short response from this middleware including meta information.")
  })
  public Beat beat() {
    return Beat.ok("Exploratory Search Service", appVersion);
  }


  @GetMapping(value = "/health/backend")
  @ApiOperation(value = "Gets the health get the backend service (SPARQL, Full-Text-Search, Gremlin).")
  @ApiResponses({
      @ApiResponse(code = 200, message = "")
  })
  public Map<String, KGDAOStatus> getHealthOfBackend() {
    return backendObserverService.getBackendServiceStatusMap();
  }

}
