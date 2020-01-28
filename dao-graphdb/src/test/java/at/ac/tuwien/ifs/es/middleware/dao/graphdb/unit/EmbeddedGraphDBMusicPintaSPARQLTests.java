package at.ac.tuwien.ifs.es.middleware.dao.graphdb.unit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

import at.ac.tuwien.ifs.es.middleware.dao.graphdb.GraphDbConfig;
import at.ac.tuwien.ifs.es.middleware.dao.graphdb.GraphDbLucene;
import at.ac.tuwien.ifs.es.middleware.dao.graphdb.GraphDbLuceneConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.DAODependencyGraphService;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.DAOScheduler;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.config.PrimaryKGDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.ThreadPoolConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.ClonedInMemoryGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.scheduler.SchedulerPipeline;
import at.ac.tuwien.ifs.es.middleware.testutil.AbstractMusicPintaSPARQLTests;
import at.ac.tuwien.ifs.es.middleware.testutil.MapDBDummy;
import at.ac.tuwien.ifs.es.middleware.testutil.MusicPintaInstrumentsResource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This is a test class implementing {@link AbstractMusicPintaSPARQLTests} for {@link
 * EmbeddedGraphDbDAO}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {EmbeddedGraphDbDAO.class, ClonedInMemoryGremlinDAO.class,
    GraphDbLucene.class, PrimaryKGDAOConfig.class, GraphDbConfig.class, GraphDbLuceneConfig.class,
    DAOScheduler.class, SchedulerPipeline.class, MapDBDummy.class, DAODependencyGraphService.class,
    ThreadPoolConfig.class, MusicPintaInstrumentsResource.class})
@TestPropertySource(properties = {
    "esm.db.choice=GraphDB",
    "esm.db.sparql.choice=EmbeddedGraphDB",
    "esm.db.fts.choice=GraphDBLucene",
    "esm.db.gremlin.choice=ClonedInMemoryGremlin",
    "graphdb.embedded.location=db/",
    "graphdb.embedded.config.path=db/conf/graphdb-musicpinta-instruments.ttl",
})
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@Ignore("Problems with missing Ontotext dependencies")
public class EmbeddedGraphDBMusicPintaSPARQLTests extends AbstractMusicPintaSPARQLTests {

  @Autowired
  @Qualifier("getGremlinDAO")
  private KGGremlinDAO gremlinDAO;

  private static final File graphDbDir = new File("db");

  @BeforeClass
  public static void setUpClass() throws IOException {
    FileUtils.deleteDirectory(graphDbDir);
    File graphConfigDir = new File(graphDbDir, "conf");
    graphConfigDir.mkdir();
    try (InputStream configIn = EmbeddedGraphDBMusicPintaSPARQLTests.class
        .getResourceAsStream("/graphdb-musicpinta-instruments.ttl")) {
      FileUtils.copyInputStreamToFile(configIn,
          new File(graphConfigDir, "graphdb-musicpinta-instruments.ttl"));
    }
  }

  @AfterClass
  public static void tearDownClass() throws IOException {
    FileUtils.deleteDirectory(graphDbDir);
  }

  @Test
  public void correctlyAutowireGremlinBean_mustBeInMemoryGremlin() {
    assertThat(gremlinDAO, instanceOf(ClonedInMemoryGremlinDAO.class));
  }

}
