package at.ac.tuwien.ifs.es.middleware.service.integration.explorationflow.aggregation;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.ThreadPoolConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.ClonedInMemoryGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JLuceneFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JMemoryStoreWithLuceneSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.resources.ResourceList;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.resources.Resource;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload.aggregation.OffsetPayload;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.aggregation.Limit;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.aggregation.Offset;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.SimpleSPARQLService;
import java.util.List;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This class tests the operator {@link Limit}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SimpleSPARQLService.class, RDF4JLuceneFullTextSearchDAO.class,
    RDF4JMemoryStoreWithLuceneSparqlDAO.class, ClonedInMemoryGremlinDAO.class,
    ThreadPoolConfig.class, KGDAOConfig.class, RDF4JDAOConfig.class, Offset.class})
@TestPropertySource(properties = {
    "esm.db.choice=RDF4J",
    "esm.db.sparql.choice=RDF4JMemoryStoreWithLucene",
    "esm.db.fts.choice=RDF4JLucene",
    "esm.db.gremlin.choice=ClonedInMemoryGremlin"
})
public class OffsetTests {

  @Autowired
  private Offset offset;

  private List<Resource> resourceList;
  private ResourceList resourceListContext;

  @Before
  public void setUp() throws Exception {
    resourceList = Lists
        .newArrayList(new Resource("http://dbpedia.org/resource/Violin"),
            new Resource("http://dbpedia.org/resource/Harp"),
            new Resource("http://dbpedia.org/resource/Ukulele"),
            new Resource("http://dbpedia.org/resource/Timpani"),
            new Resource("http://dbtune.org/musicbrainz/resource/performance/1334"),
            new Resource("http://dbtune.org/musicbrainz/resource/performance/14579"),
            new Resource("http://dbtune.org/musicbrainz/resource/performance/22643"),
            new Resource("http://dbtune.org/musicbrainz/resource/performance/33363"),
            new Resource("http://dbpedia.org/resource/Mbira"),
            new Resource("http://dbpedia.org/resource/Banjo"));
    resourceListContext = new ResourceList(resourceList);
  }

  @Test
  public void offsetOf4_mustReturnAlteredContextWithSkippingFirst4Entries() {
    ExplorationContext context = offset.apply(resourceListContext, new OffsetPayload(4L));
    assertNotNull(context);
    assertThat(context, instanceOf(ResourceList.class));
    ResourceList resourceListContextResponse = (ResourceList) context;
    assertThat(resourceListContextResponse.asResourceList(), hasSize(6));
    assertThat(resourceListContextResponse.asResourceList(),
        contains(resourceList.subList(4, 10).toArray(new Resource[0])));
  }

  @Test
  public void offsetOf10_mustReturnEmptyContext() {
    ExplorationContext context = offset.apply(resourceListContext, new OffsetPayload(10L));
    assertNotNull(context);
    assertThat(context, instanceOf(ResourceList.class));
    ResourceList resourceListContextResponse = (ResourceList) context;
    assertThat(resourceListContextResponse.asResourceList(), hasSize(0));
  }

  @Test
  public void offsetOf0_mustReturnCompleteContext() {
    ExplorationContext context = offset.apply(resourceListContext, new OffsetPayload(0L));
    assertNotNull(context);
    assertThat(context, instanceOf(ResourceList.class));
    ResourceList resourceListContextResponse = (ResourceList) context;
    assertThat(resourceListContextResponse.asResourceList(), hasSize(10));
    assertThat(resourceListContextResponse.asResourceList(),
        contains(resourceList.toArray(new Resource[0])));
  }

}
