package at.ac.tuwien.ifs.es.middleware.dao.janusgraph;


import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JLuceneFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JMemoryStoreWithLuceneSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JDAOConfig;
import at.ac.tuwien.ifs.es.middleware.testutil.AbstractMusicPintaGremlinTests;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
    RDF4JMemoryStoreWithLuceneSparqlDAO.class, ClonedLocalJanusGraph.class, KGDAOConfig.class,
    RDF4JDAOConfig.class, RDF4JLuceneFullTextSearchDAO.class
})
@TestPropertySource(properties = {
    "esm.db.choice=RDF4J",
    "esm.db.sparql.choice=RDF4JMemoryStoreWithLucene",
    "esm.db.fts.choice=RDF4JLucene",
    "esm.db.gremlin.choice=LocalSyncingJanusGraph",
    "janusgraph.dir=janusgraph/",
})
@Ignore
public class ClonedLocalJanusGraphTest extends AbstractMusicPintaGremlinTests {

  @Autowired
  @Qualifier("getSparqlDAO")
  private KGSparqlDAO sparqlDAO;
  @Autowired
  @Qualifier("getGremlinDAO")
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
    FileUtils.deleteDirectory(janusGraphDir);
    janusGraphDir.mkdirs();
  }

  @AfterClass
  public static void tearDownClass() throws IOException {
    FileUtils.deleteDirectory(janusGraphDir);
  }

}
