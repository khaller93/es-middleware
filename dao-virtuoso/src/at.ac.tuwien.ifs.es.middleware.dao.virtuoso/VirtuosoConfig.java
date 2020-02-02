package at.ac.tuwien.ifs.es.middleware.dao.virtuoso;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * This class maintains the conf for Stardog.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Configuration
public class VirtuosoConfig {

  @Value("${virtuoso.address}")
  private String address;

  /**
   * Gets the specified address for the SPARQL endpoint Virtuoso.
   *
   * @return the specified address of the SPARQL endpoint.
   */
  public String getAddress() {
    return address;
  }

}
