package at.ac.tuwien.ifs.es.middleware.dao.stardog;

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
public class StardogConfig {

  @Value("${stardog.address}")
  private String address;

  @Value("${stardog.db.name}")
  private String dbName;

  @Value("${stardog.username:#{null}}")
  private String username;

  @Value("${stardog.password:#{null}}")
  private String password;


  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  /**
   * Gets the URL for the SPARQL endpoint of Stardog specified in the properties file.
   *
   * @return the URL for the SPARQL endpoint for reading data.
   */
  public String getSPARQLQueryEndpointURL() {
    String formattedAddress = address.endsWith("/") ? address : address + "/";
    return String.format("%s/%s/query", formattedAddress, dbName);
  }

  /**
   * Gets the URL for the SPARQL endpoint of Stardog specified in the properties file.
   *
   * @return the URL for the SPARQL endpoint for manipulating data.
   */
  public String getSPARQLUpdateEndpointURL() {
    String formattedAddress = address.endsWith("/") ? address : address + "/";
    return String.format("%s/%s/update", formattedAddress, dbName);
  }

}
