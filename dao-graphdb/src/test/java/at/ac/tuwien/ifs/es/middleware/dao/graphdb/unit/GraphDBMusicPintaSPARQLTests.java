package at.ac.tuwien.ifs.es.middleware.dao.graphdb.unit;

import at.ac.tuwien.ifs.es.middleware.dao.graphdb.EmbeddedGraphDbDAO;
import at.ac.tuwien.ifs.es.middleware.dao.graphdb.GraphDbDAO;
import at.ac.tuwien.ifs.es.middleware.testutil.AbstractMusicPintaSPARQLTests;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

/**
 * This is a test class implementing {@link AbstractMusicPintaSPARQLTests} for an embedded {@link
 * GraphDbDAO}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@ContextConfiguration(classes = {EmbeddedGraphDbDAO.class}, inheritLocations = true)
@TestPropertySource(properties = {
    "graphdb.embedded.location=graphdb/",
    "graphdb.embedded.config.path=graphdb/configuration/graphdb-musicpinta-instruments.ttl"
})
public class GraphDBMusicPintaSPARQLTests extends AbstractMusicPintaSPARQLTests {

  private static final File graphDbDir = new File("graphdb/");

  @BeforeClass
  public static void setUpClass() throws IOException {
    graphDbDir.mkdirs();
    File graphConfigDir = new File(graphDbDir, "configuration");
    graphConfigDir.mkdir();
    try (InputStream configIn = GraphDBMusicPintaSPARQLTests.class
        .getResourceAsStream("/graphdb-musicpinta-instruments.ttl")) {
      FileUtils.copyInputStreamToFile(configIn,
          new File(graphConfigDir, "graphdb-musicpinta-instruments.ttl"));
    }
  }

  private static void destroyDir(File graphDbDir) throws IOException {
    FileUtils.deleteDirectory(graphDbDir);
  }

  @AfterClass
  public static void tearDownClass() throws IOException {
    destroyDir(graphDbDir);
  }
}
