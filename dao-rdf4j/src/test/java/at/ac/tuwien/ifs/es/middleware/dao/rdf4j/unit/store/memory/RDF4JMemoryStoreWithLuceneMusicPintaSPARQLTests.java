package at.ac.tuwien.ifs.es.middleware.dao.rdf4j.unit.store.memory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.ThreadPoolConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.ClonedInMemoryGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JMemoryStoreWithLuceneSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JLuceneFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.testutil.AbstractMusicPintaGremlinTests;
import at.ac.tuwien.ifs.es.middleware.testutil.AbstractMusicPintaSPARQLTests;
import at.ac.tuwien.ifs.es.middleware.testutil.MusicPintaInstrumentsResource;
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
    RDF4JMemoryStoreWithLuceneSparqlDAO.class, ClonedInMemoryGremlinDAO.class, KGDAOConfig.class,
    RDF4JDAOConfig.class, RDF4JLuceneFullTextSearchDAO.class, ThreadPoolConfig.class,
    MusicPintaInstrumentsResource.class
})
@TestPropertySource(properties = {
    "esm.db.choice=RDF4J",
    "esm.db.sparql.choice=RDF4JMemoryStoreWithLucene",
    "esm.db.fts.choice=RDF4JLucene",
    "esm.db.gremlin.choice=ClonedInMemoryGremlin"
})
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class RDF4JMemoryStoreWithLuceneMusicPintaSPARQLTests extends AbstractMusicPintaSPARQLTests {

  @Autowired
  @Qualifier("getSparqlDAO")
  private KGSparqlDAO sparqlDAO;

  @Test
  public void correctlyAutowireSPARQLBean_mustBeMemoryStoreWithLucene() {
    assertThat(sparqlDAO, instanceOf(RDF4JMemoryStoreWithLuceneSparqlDAO.class));
  }
}
