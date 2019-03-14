package at.ac.tuwien.ifs.es.middleware.dao.rdf4j.unit.store.nativerdf;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.ThreadPoolConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.ClonedInMemoryGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JLuceneFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JMemoryStoreWithLuceneSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JNativeStoreWithLuceneSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.testutil.AbstractMusicPintaGremlinTests;
import at.ac.tuwien.ifs.es.middleware.testutil.MusicPintaInstrumentsResource;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
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
 * This test class implements the {@link AbstractMusicPintaGremlinTests} for {@link
 * RDF4JMemoryStoreWithLuceneSparqlDAO}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
    RDF4JNativeStoreWithLuceneSparqlDAO.class, ClonedInMemoryGremlinDAO.class, KGDAOConfig.class,
    RDF4JDAOConfig.class, RDF4JLuceneFullTextSearchDAO.class, ThreadPoolConfig.class,
    MusicPintaInstrumentsResource.class
})
@TestPropertySource(properties = {
    "esm.db.choice=RDF4J",
    "esm.db.sparql.choice=RDF4JNativeStoreWithLucene",
    "esm.db.fts.choice=RDF4JLucene",
    "esm.db.gremlin.choice=ClonedInMemoryGremlin",
    "rdf4j.dir=nativerdf"
})
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class RDF4JNativeStoreWithLuceneMusicPintaGremlinTests extends
    AbstractMusicPintaGremlinTests {

  @Autowired
  @Qualifier("getGremlinDAO")
  private KGGremlinDAO gremlinDAO;

  private static File dataDir = new File("nativerdf");

  @BeforeClass
  public static void setUpClass() throws IOException {
    FileUtils.deleteDirectory(dataDir);
  }

  @AfterClass
  public static void tearDownClass() throws IOException {
    FileUtils.deleteDirectory(dataDir);
  }

  @Test
  public void correctlyAutowireGremlinBean_mustBeInMemoryGremlin() {
    assertThat(gremlinDAO, instanceOf(ClonedInMemoryGremlinDAO.class));
  }
}
