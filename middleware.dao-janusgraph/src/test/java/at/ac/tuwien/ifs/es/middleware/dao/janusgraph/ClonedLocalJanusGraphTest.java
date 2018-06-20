package at.ac.tuwien.ifs.es.middleware.dao.janusgraph;


import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.AbstractClonedGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.InMemoryGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.IndexedMemoryKnowledgeGraph;
import at.ac.tuwien.ifs.es.middleware.testutil.AbstractMusicPintaGremlinTests;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.annotation.PostConstruct;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
    IndexedMemoryKnowledgeGraph.class,
    ClonedLocalJanusGraph.class
})
@TestPropertySource(properties = {
    "esm.db.choice=IndexedMemoryDB",
    "esm.db.gremlin.choice=LocalSyncingJanusGraph",
    "janusgraph.dir=janusgraph/",
})
public class ClonedLocalJanusGraphTest extends AbstractMusicPintaGremlinTests {

  @Autowired
  private KGSparqlDAO sparqlDAO;
  @Autowired
  private KGGremlinDAO gremlinDAO;

  private static final File janusGraphDir = new File("janusgraph/");

  @Override
  protected KGSparqlDAO getSparqlDAO() {
    return sparqlDAO;
  }

  @Override
  protected KGGremlinDAO getGremlinDAO() {
    return gremlinDAO;
  }

  @BeforeClass
  public static void setUpClass() throws IOException {
    janusGraphDir.mkdirs();
  }

  private static void destroyDir(File janusGraphDir) throws IOException {
    FileUtils.deleteDirectory(janusGraphDir);
  }

  @AfterClass
  public static void tearDownClass() throws IOException {
    destroyDir(janusGraphDir);
  }

}
