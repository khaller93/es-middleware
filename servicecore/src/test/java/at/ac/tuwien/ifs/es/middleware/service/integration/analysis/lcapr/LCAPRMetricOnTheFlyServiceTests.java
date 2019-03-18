package at.ac.tuwien.ifs.es.middleware.service.integration.analysis.lcapr;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.ThreadPoolConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.ClonedInMemoryGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JLuceneFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JMemoryStoreWithLuceneSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.service.analysis.centrality.pagerank.PageRankCentralityMetricWithGremlinService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes.AllClassesService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes.AllClassesWithSPARQLService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes.hierarchy.ClassHierarchyService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes.hierarchy.ClassHierarchyWithSPARQLService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes.hierarchy.lca.LCAOnTheFlyService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.AllResourcesService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.AllResourcesWithSPARQLService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.ResourceClassService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.ResourceClassWithSPARQLService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.SameAsResourceService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.SameAsResourceWithSPARQLService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.icpr.LCAPRMetricImpl;
import at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.icpr.LCAPRMetricOnTheFlyService;
import at.ac.tuwien.ifs.es.middleware.service.caching.SpringCacheConfig;
import at.ac.tuwien.ifs.es.middleware.service.integration.MapDBDummy;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.SimpleGremlinService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.SimpleSPARQLService;
import at.ac.tuwien.ifs.es.middleware.testutil.WineOntologyDatasetResource;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SimpleSPARQLService.class, RDF4JLuceneFullTextSearchDAO.class,
    RDF4JMemoryStoreWithLuceneSparqlDAO.class, ClonedInMemoryGremlinDAO.class,
    ThreadPoolConfig.class, KGDAOConfig.class, RDF4JDAOConfig.class, ThreadPoolConfig.class,
    SimpleGremlinService.class, SameAsResourceWithSPARQLService.class, SpringCacheConfig.class,
    AllResourcesWithSPARQLService.class, AllClassesWithSPARQLService.class, MapDBDummy.class,
    WineOntologyDatasetResource.class, ClassHierarchyWithSPARQLService.class,
    ResourceClassWithSPARQLService.class, LCAOnTheFlyService.class,
    PageRankCentralityMetricWithGremlinService.class, LCAPRMetricOnTheFlyService.class})
@TestPropertySource(properties = {
    "esm.db.choice=RDF4J",
    "esm.db.sparql.choice=RDF4JMemoryStoreWithLucene",
    "esm.db.fts.choice=RDF4JLucene",
    "esm.db.gremlin.choice=ClonedInMemoryGremlin",
})
public class LCAPRMetricOnTheFlyServiceTests extends LCAPRMetricServiceTests {

  @Autowired
  private AllResourcesService allResourcesService;
  @Autowired
  private AllClassesService allClassesService;
  @Autowired
  private SameAsResourceService sameAsResourceService;
  @Autowired
  private ClassHierarchyService classHierarchyService;
  @Autowired
  private ResourceClassService resourceClassService;

  @Override
  @Before
  public void setUp() throws Exception {
    allResourcesService.compute();
    allClassesService.compute();
    sameAsResourceService.compute();
    classHierarchyService.compute();
    resourceClassService.compute();
    super.setUp();
  }

}