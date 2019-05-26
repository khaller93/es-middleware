package at.ac.tuwien.ifs.es.middleware.service.integration.explorationflow.aggregation;

import static org.hamcrest.CoreMatchers.hasItems;
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
import at.ac.tuwien.ifs.es.middleware.common.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.common.exploration.context.resources.ResourceList;
import at.ac.tuwien.ifs.es.middleware.common.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload.aggregation.OrderByPayload;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload.aggregation.OrderByPayload.ORDER_STRATEGY;
import at.ac.tuwien.ifs.es.middleware.service.exception.ExplorationFlowServiceExecutionException;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.aggregation.OrderBy;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.SimpleSPARQLService;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This class tests the operator {@link OrderBy}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SimpleSPARQLService.class, RDF4JLuceneFullTextSearchDAO.class,
    RDF4JMemoryStoreWithLuceneSparqlDAO.class, ClonedInMemoryGremlinDAO.class,
    ThreadPoolConfig.class, KGDAOConfig.class, RDF4JDAOConfig.class, OrderBy.class})
@TestPropertySource(properties = {
    "esm.db.choice=RDF4J",
    "esm.db.sparql.choice=RDF4JMemoryStoreWithLucene",
    "esm.db.fts.choice=RDF4JLucene",
    "esm.db.gremlin.choice=ClonedInMemoryGremlin"
})
public class OrderByTests {

  private List<Resource> resourceList;
  private ResourceList resourceListContext;
  private Map<JsonPointer, List<Double>> resourceValueMap;

  @Before
  public void setUpClass() {
    resourceList = Lists
        .newArrayList(new Resource("http://dbpedia.org/resource/Violin"),
            new Resource("http://dbpedia.org/resource/Harp"),
            new Resource("http://dbpedia.org/resource/Ukulele"),
            new Resource("http://dbpedia.org/resource/Timpani"),
            new Resource("http://dbpedia.org/resource/Banjo"));
    resourceValueMap = new HashMap<>();
    resourceValueMap
        .put(JsonPointer.compile("/x/val"), Lists.newArrayList(-1.5, 0.5, 0.8, 2.0, 1.0));
    resourceValueMap
        .put(JsonPointer.compile("/y/val"), Lists.newArrayList(1.0, -1.0, null, null, 0.5));
    resourceListContext = new ResourceList(resourceList);
    for (Map.Entry<JsonPointer, List<Double>> entry : resourceValueMap.entrySet()) {
      int n = 0;
      for (Resource resource : resourceList) {
        resourceListContext
            .values().put(resource.getId(), entry.getKey(),
                JsonNodeFactory.instance.numberNode(entry.getValue().get(n)));
        n++;
      }
    }
  }

  public List<Resource> mapToResource(List<String> iriStrings) {
    return iriStrings.stream().map(Resource::new).collect(Collectors.toList());
  }

  public List<Resource> mapToResource(String... iriStrings) {
    return Stream.of(iriStrings).map(Resource::new).collect(Collectors.toList());
  }

  @Autowired
  private OrderBy orderBy;

  @Test
  public void orderByDESC_mustReturnContextWithOrderedResourceList() {
    ExplorationContext context = orderBy.apply(resourceListContext,
        new OrderByPayload(JsonPointer.compile("/x/val"), ORDER_STRATEGY.DESC));
    assertNotNull(context);
    assertThat(context, instanceOf(ResourceList.class));
    ResourceList resourceListContextResponse = (ResourceList) context;
    assertThat(resourceListContextResponse.asResourceList(), contains(
        mapToResource("http://dbpedia.org/resource/Timpani", "http://dbpedia.org/resource/Banjo",
            "http://dbpedia.org/resource/Ukulele", "http://dbpedia.org/resource/Harp",
            "http://dbpedia.org/resource/Violin").toArray(new Resource[0])));
  }

  @Test
  public void orderByEmpytContext_mustReturnEmptyContext() {
    ExplorationContext context = orderBy.apply(new ResourceList(),
        new OrderByPayload(JsonPointer.compile("/x/val"), ORDER_STRATEGY.DESC));
    assertNotNull(context);
    assertThat(context, instanceOf(ResourceList.class));
    ResourceList resourceListContextResponse = (ResourceList) context;
    assertThat(resourceListContextResponse.asResourceList(), hasSize(0));
  }

  @Test
  public void orderByASC_mustReturnContextWithOrderedResourceList() {
    ExplorationContext context = orderBy.apply(resourceListContext,
        new OrderByPayload(JsonPointer.compile("/x/val"), ORDER_STRATEGY.ASC));
    assertNotNull(context);
    assertThat(context, instanceOf(ResourceList.class));
    ResourceList resourceListContextResponse = (ResourceList) context;
    assertThat(resourceListContextResponse.asResourceList(), contains(
        mapToResource("http://dbpedia.org/resource/Violin", "http://dbpedia.org/resource/Harp",
            "http://dbpedia.org/resource/Ukulele", "http://dbpedia.org/resource/Banjo",
            "http://dbpedia.org/resource/Timpani").toArray(new Resource[0])));
  }

  @Test
  public void orderByDefault_mustOrderASC() {
    ExplorationContext context = orderBy.apply(resourceListContext,
        new OrderByPayload(JsonPointer.compile("/x/val")));
    assertNotNull(context);
    assertThat(context, instanceOf(ResourceList.class));
    ResourceList resourceListContextResponse = (ResourceList) context;
    assertThat(resourceListContextResponse.asResourceList(), contains(
        mapToResource("http://dbpedia.org/resource/Violin", "http://dbpedia.org/resource/Harp",
            "http://dbpedia.org/resource/Ukulele", "http://dbpedia.org/resource/Banjo",
            "http://dbpedia.org/resource/Timpani").toArray(new Resource[0])));
  }

  @Test
  public void orderByASCWithNullValues_mustReturnContextWithOrderedResourceList() {
    ExplorationContext context = orderBy.apply(resourceListContext,
        new OrderByPayload(JsonPointer.compile("/y/val")));
    assertNotNull(context);
    assertThat(context, instanceOf(ResourceList.class));
    ResourceList resourceListContextResponse = (ResourceList) context;
    assertThat(resourceListContextResponse.asResourceList().subList(2, 5), contains(
        mapToResource("http://dbpedia.org/resource/Harp", "http://dbpedia.org/resource/Banjo",
            "http://dbpedia.org/resource/Violin").toArray(new Resource[0])));
    assertThat(resourceListContextResponse.asResourceList().subList(0, 2),
        hasItems(mapToResource("http://dbpedia.org/resource/Ukulele",
            "http://dbpedia.org/resource/Timpani").toArray(new Resource[0])));
  }

  @Test
  public void orderByDESCWithNullValues_mustReturnContextWithOrderedResourceList() {
    ExplorationContext context = orderBy.apply(resourceListContext,
        new OrderByPayload(JsonPointer.compile("/y/val"), ORDER_STRATEGY.DESC));
    assertNotNull(context);
    assertThat(context, instanceOf(ResourceList.class));
    ResourceList resourceListContextResponse = (ResourceList) context;
    assertThat(resourceListContextResponse.asResourceList().subList(0, 3), contains(
        mapToResource("http://dbpedia.org/resource/Violin", "http://dbpedia.org/resource/Banjo",
            "http://dbpedia.org/resource/Harp").toArray(new Resource[0])));
    assertThat(resourceListContextResponse.asResourceList().subList(3, 5),
        hasItems(mapToResource("http://dbpedia.org/resource/Ukulele",
            "http://dbpedia.org/resource/Timpani").toArray(new Resource[0])));
  }

  @Test(expected = ExplorationFlowServiceExecutionException.class)
  public void orderByMissingProp_mustThrowException() {
    orderBy.apply(resourceListContext, new OrderByPayload(JsonPointer.compile("/z/val")));
  }

}
