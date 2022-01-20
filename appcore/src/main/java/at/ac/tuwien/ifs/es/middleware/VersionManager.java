package at.ac.tuwien.ifs.es.middleware;

import org.springframework.stereotype.Component;

/**
 * A version manager, which can be used to fetch the current version of this application.
 *
 * @author Kevin Haller
 * @version 1.2
 * @since 1.2
 */
@Component
public class VersionManager {

  /**
   * Gets the current version of this application. The version is read from the manifest file, which
   * only exists in a built jar file.
   *
   * @return the fetched application version, or the string {@code "dev-mode"}, if the application
   * version cannot be fetched.
   */
  public String getVersion() {
    String version = this.getClass().getPackage().getImplementationVersion();
    if (version == null) {
      version = "dev-mode";
    }
    return version;
  }
}
