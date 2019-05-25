package at.ac.tuwien.ifs.es.middleware.tests.integration.explorationflow;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import at.ac.tuwien.ifs.es.middleware.ExploratorySearchApplication;
import at.ac.tuwien.ifs.es.middleware.common.exploration.context.resources.ResourceList;
import at.ac.tuwien.ifs.es.middleware.common.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.testutil.MusicPintaInstrumentsResource;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.rdf.api.IRI;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * This class integration tests {@link at.ac.tuwien.ifs.es.middleware.service.exploration.factory.DynamicExplorationFlowFactory}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ExploratorySearchApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "esm.db.choice=RDF4J",
    "esm.db.sparql.choice=RDF4JMemoryStoreWithLucene",
    "esm.db.fts.choice=RDF4JLucene",
    "esm.db.gremlin.choice=ClonedInMemoryGremlin"
})
public class DynamicExploratoryMusicPintaFlowTest {

  @Rule
  @Autowired
  public MusicPintaInstrumentsResource musicPintaResource;
  @Autowired
  private TestRestTemplate restTemplate;
  @Autowired
  private ObjectMapper parameterMapper;

  private final static Map<String, String> jsonTestMap = new HashMap<>();

  @BeforeClass
  public static void setUpClass() throws Exception {
    for (Entry<String, String> e : ImmutableMap.<String, String>builder()
        .put("simpleAllSource", "/dynamicflow/simpleAllSource.json")
        .put("simpleExcludingAllSource", "/dynamicflow/simpleExcludingAllSource.json")
        .put("naiveLimit", "/dynamicflow/naiveLimit.json")
        .put("simpleDescribe", "/dynamicflow/simpleDescribe.json")
        .put("simpleGuitarFTS", "/dynamicflow/simpleGuitarFTS.json")
        .put("guitarFTSWithNiceDescription", "/dynamicflow/customGuitarFTSDescriber.json")
        .put("orderByTest", "/dynamicflow/orderByTest.json")
        .put("biggerFTSFLow", "/dynamicflow/biggerFTSFlow.json")
        .put("minmaxFTSScore", "/dynamicflow/minmaxFTSScore.json")
        .build().entrySet()) {
      try (InputStream in = DynamicExploratoryMusicPintaFlowTest.class
          .getResourceAsStream(e.getValue())) {
        jsonTestMap.put(e.getKey(), IOUtils.toString(in, "utf-8"));
      }
    }
  }

  @Before
  public void setUp() throws Exception {
    musicPintaResource.waitForAllDAOsBeingReady();
  }

  @Test
  public void test_excludingAllFlow_mustReturnNoInstruments() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.valueOf("application/json"));
    HttpEntity<String> entity = new HttpEntity<>(jsonTestMap.get("simpleExcludingAllSource"),
        headers);
    ResponseEntity<String> limit5Response = restTemplate
        .exchange("/explore/with/custom/flow", HttpMethod.POST, entity, String.class);
    assertTrue(limit5Response.getStatusCode().is2xxSuccessful());
    ResourceList resources = parameterMapper
        .readValue(limit5Response.getBody(), ResourceList.class);
    List<String> resourceIRIs = resources.streamOfResults()
        .map(c -> ((IRI) c.value()).getIRIString())
        .collect(Collectors.toList());
    assertThat(resourceIRIs,
        not(hasItems("http://dbtune.org/musicbrainz/resource/instrument/473",
            "http://dbtune.org/musicbrainz/resource/instrument/436",
            "http://dbtune.org/musicbrainz/resource/instrument/331",
            "http://dbpedia.org/resource/Vielle")));
    assertThat(resourceIRIs, hasItems("http://dbtune.org/musicbrainz/resource/performance/35367",
        "http://dbtune.org/musicbrainz/resource/performance/66905"));
  }

  @Test
  public void test_allFlow_mustReturnOnlyAllInstruments() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.valueOf("application/json"));
    HttpEntity<String> entity = new HttpEntity<>(jsonTestMap.get("simpleAllSource"), headers);
    ResponseEntity<String> limit5Response = restTemplate
        .exchange("/explore/with/custom/flow", HttpMethod.POST, entity, String.class);
    assertTrue(limit5Response.getStatusCode().is2xxSuccessful());
    ResourceList resources = parameterMapper
        .readValue(limit5Response.getBody(), ResourceList.class);
    List<String> resourceIRIs = resources.streamOfResults()
        .map(c -> ((IRI) c.value()).getIRIString())
        .collect(Collectors.toList());
    assertThat(resourceIRIs,
        hasItems("http://dbtune.org/musicbrainz/resource/instrument/473",
            "http://dbtune.org/musicbrainz/resource/instrument/436",
            "http://dbtune.org/musicbrainz/resource/instrument/331",
            "http://dbpedia.org/resource/Vielle"));
    assertThat(resourceIRIs,
        not(hasItems("http://dbtune.org/musicbrainz/resource/performance/35367",
            "http://dbtune.org/musicbrainz/resource/performance/66905")));
  }

  @Test
  public void test_limitFlow_mustReturn5Resources() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.valueOf("application/json"));
    HttpEntity<String> entity = new HttpEntity<>(jsonTestMap.get("naiveLimit"), headers);
    ResponseEntity<String> limit5Response = restTemplate
        .exchange("/explore/with/custom/flow", HttpMethod.POST, entity, String.class);
    assertThat(limit5Response.getStatusCode().value(), is(equalTo(200)));
    ResourceList resources = parameterMapper
        .readValue(limit5Response.getBody(), ResourceList.class);
    assertThat(resources.streamOfResults().collect(Collectors.toList()), hasSize(100));
  }

  @Test
  public void test_simpleDescribeFlow_mustReturnDescriptionOfResources() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.valueOf("application/json"));
    HttpEntity<String> entity = new HttpEntity<>(jsonTestMap.get("simpleDescribe"), headers);
    ResponseEntity<String> descriptionResponse = restTemplate
        .exchange("/explore/with/custom/flow", HttpMethod.POST, entity, String.class);
    assertTrue(descriptionResponse.getStatusCode().is2xxSuccessful());
    ResourceList resources = parameterMapper
        .readValue(descriptionResponse.getBody(), ResourceList.class);
    assertThat(resources
        .values().get("http://dbpedia.org/resource/Santur",
            JsonPointer.compile("/describe/label/values/en"))
        .get().get(0).asText(), is("Santur"));
    assertThat(resources
        .values().get("http://dbpedia.org/resource/Santur",
            JsonPointer.compile("/describe/label/values/en"))
        .get().get(0).asText(), is("Santur"));
    assertThat(resources
            .values().get("http://dbpedia.org/resource/Tembor",
                JsonPointer.compile("/describe/description/values/en")).get().get(0)
            .asText(),
        is("The Tembor is a stringed musical instrument from the Uyghur region, Western China. It has 5 strings in 3 courses and is tuned A A, D, G G. The strings are made of Steel."));
    assertFalse("The 'Tambura' resource has no description.",
        resources.values().get("http://dbtune.org/musicbrainz/resource/instrument/473",
            JsonPointer.compile("/describe/description/value")).isPresent());
  }

  @Test
  public void test_guitarFlow_mustReturnAllGuitarRelatedResources() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.valueOf("application/json"));
    HttpEntity<String> entity = new HttpEntity<>(jsonTestMap.get("simpleGuitarFTS"),
        headers);
    ResponseEntity<String> limit5Response = restTemplate
        .exchange("/explore/with/custom/flow", HttpMethod.POST, entity, String.class);
    assertTrue(limit5Response.getStatusCode().is2xxSuccessful());
    ResourceList resources = parameterMapper
        .readValue(limit5Response.getBody(), ResourceList.class);
    List<String> resourceIRIs = resources.streamOfResults()
        .map(c -> ((IRI) c.value()).getIRIString())
        .collect(Collectors.toList());
    assertThat(resourceIRIs, hasItems("http://dbpedia.org/resource/Classical_guitar",
        "http://dbpedia.org/resource/Bass_guitar",
        "http://dbpedia.org/resource/Electric_guitar",
        "http://dbtune.org/musicbrainz/resource/instrument/377"));
  }

  @Test
  public void test_guitarFlow_mustReturnAllGuitarWithNiceDescription() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.valueOf("application/json"));
    HttpEntity<String> entity = new HttpEntity<>(jsonTestMap.get("guitarFTSWithNiceDescription"),
        headers);
    ResponseEntity<String> limit5Response = restTemplate
        .exchange("/explore/with/custom/flow", HttpMethod.POST, entity, String.class);
    assertTrue(limit5Response.getStatusCode().is2xxSuccessful());

    ResourceList resources = parameterMapper
        .readValue(limit5Response.getBody(), ResourceList.class);
    List<String> resourceIRIs = resources.streamOfResults().map(Resource::getId)
        .collect(Collectors.toList());
    assertThat(resourceIRIs, hasItems("http://dbpedia.org/resource/Classical_guitar",
        "http://dbpedia.org/resource/Bass_guitar",
        "http://dbpedia.org/resource/Electric_guitar",
        "http://dbtune.org/musicbrainz/resource/instrument/377"));

    Optional<JsonNode> electricGuitarLabelOptional = resources
        .values().get("http://dbtune.org/musicbrainz/resource/instrument/78",
            JsonPointer.compile("/describe/label/values/en"));
    assertTrue(electricGuitarLabelOptional.isPresent());
    assertThat(electricGuitarLabelOptional.get().get(0).asText(), is(equalTo("Electric guitar")));
    Optional<JsonNode> electricGuitarDescriptionOptional = resources
        .values().get("http://dbtune.org/musicbrainz/resource/instrument/78",
            JsonPointer.compile("/describe/description"));
    assertFalse(
        "The description must not be given, because the custom describer did not specify this content.",
        electricGuitarDescriptionOptional.isPresent());
    Optional<JsonNode> electricGuitarThumbOptional = resources
        .values().get("http://dbtune.org/musicbrainz/resource/instrument/78",
            JsonPointer.compile("/describe/thumb/values"));
    assertTrue(electricGuitarThumbOptional.isPresent());
    assertThat(electricGuitarThumbOptional.get().get(0).asText(), is(equalTo(
        "http://upload.wikimedia.org/wikipedia/commons/thumb/d/dc/Godin_LG-Squier_Strat.jpg/200px-Godin_LG-Squier_Strat.jpg")));

    Optional<JsonNode> guitaleleLabelOptional = resources
        .values().get("http://dbpedia.org/resource/Guitalele",
            JsonPointer.compile("/describe/label/values/en"));
    assertTrue(guitaleleLabelOptional.isPresent());
    assertThat("http://dbpedia.org/resource/Guitalele",
        guitaleleLabelOptional.get().get(0).asText(),
        is(equalTo("Guitalele")));
    Optional<JsonNode> guitaleleThumbOptional = resources
        .values().get("http://dbpedia.org/resource/Guitalele",
            JsonPointer.compile("/describe/thumb/values"));
    assertFalse("There is no thumbnail get a 'guitalele' in the data",
        guitaleleThumbOptional.isPresent());
  }

  @Test
  public void test_orderBy_mustReturnInstrumentWithLowestScoreFirst() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.valueOf("application/json"));
    HttpEntity<String> entity = new HttpEntity<>(jsonTestMap.get("orderByTest"),
        headers);
    ResponseEntity<String> orderByResponse = restTemplate
        .exchange("/explore/with/custom/flow", HttpMethod.POST, entity, String.class);
    assertThat(orderByResponse.getStatusCode().value(), is(equalTo(200)));
    ResourceList resources = parameterMapper
        .readValue(orderByResponse.getBody(), ResourceList.class);
    List<String> top10List = resources.streamOfResults().limit(12).map(Resource::getId)
        .collect(Collectors.toList());
    assertThat(top10List, hasItems("http://dbtune.org/musicbrainz/resource/instrument/475",
        "http://dbpedia.org/resource/Bordonua", "http://dbpedia.org/resource/Cavaquinho"));
    /* it should not contain the resources with biggest score, because ordered by ASC */
    assertThat(top10List, not(hasItem("http://dbtune.org/musicbrainz/resource/instrument/323")));
    assertThat(top10List, not(hasItem("http://dbtune.org/musicbrainz/resource/instrument/399")));
    assertThat(top10List, not(hasItem("http://dbtune.org/musicbrainz/resource/instrument/76")));
  }

  @Test
  public void test_bigFTSFlow_mustReturn10InstrumentWithBiggestSummedScore() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.valueOf("application/json"));
    HttpEntity<String> entity = new HttpEntity<>(jsonTestMap.get("biggerFTSFLow"),
        headers);
    ResponseEntity<String> bigFlowResponse = restTemplate
        .exchange("/explore/with/custom/flow", HttpMethod.POST, entity, String.class);
    assertThat(bigFlowResponse.getStatusCode().value(), is(equalTo(200)));

    ResourceList resourcesContext = parameterMapper
        .readValue(bigFlowResponse.getBody(), ResourceList.class);
    List<Resource> resourceList = resourcesContext.streamOfResults().collect(Collectors.toList());
    assertThat(resourceList, hasSize(10));
    /* get class and check them */
    Iterable<Iterable<? super String>> classes = resourceList.stream().map(r -> {
      Optional<JsonNode> optionalClasses = resourcesContext
          .values().get(r.getId(), JsonPointer.compile("/describe/class/values"));
      if (optionalClasses.isPresent()) {
        List<String> resourceClasses = new LinkedList<>();
        optionalClasses.get().iterator().forEachRemaining(c -> resourceClasses.add(c.asText()));
        return resourceClasses;
      } else {
        return Collections.<String>emptyList();
      }
    }).collect(Collectors.toList());
    assertThat(classes, everyItem(hasItem("http://purl.org/ontology/mo/Instrument")));
    assertThat(classes, everyItem(hasItem("http://purl.org/ontology/mo/Instrument")));
    /* get labels and check them */
    List<String> labels = resourceList.stream().flatMap(r -> {
      Optional<JsonNode> optionalLabels = resourcesContext
          .values().get(r.getId(), JsonPointer.compile("/describe/label/values/en"));
      if (optionalLabels.isPresent()) {
        List<String> labelList = new LinkedList<>();
        optionalLabels.get().forEach(l -> {
          labelList.add(l.asText());
        });
        return labelList.stream();
      } else {
        return Stream.<String>empty();
      }
    }).collect(Collectors.toList());
    assertThat(labels, hasItems("Acoustic guitar", "Electric guitar"));
  }

  @Test
  public void test_minMaxFTSScore_valuesMustBeMappedBetween0And1() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.valueOf("application/json"));
    HttpEntity<String> entity = new HttpEntity<>(jsonTestMap.get("minmaxFTSScore"),
        headers);
    ResponseEntity<String> bigFlowResponse = restTemplate
        .exchange("/explore/with/custom/flow", HttpMethod.POST, entity, String.class);
    assertThat(bigFlowResponse.getStatusCode().value(), is(equalTo(200)));

    ResourceList resourcesContext = parameterMapper
        .readValue(bigFlowResponse.getBody(), ResourceList.class);
    List<Double> scoreNumbers = resourcesContext.streamOfResults()
        .map(r -> {
          Optional<JsonNode> optionalScore = resourcesContext
              .values().get(r.getId(), JsonPointer.compile("/fts/score"));
          return optionalScore.map(JsonNode::asDouble).orElse(null);
        }).collect(Collectors.toList());
    assertThat(scoreNumbers, everyItem(allOf(greaterThanOrEqualTo(0.0), lessThanOrEqualTo(1.0))));
    /* check individual scores */
    Optional<JsonNode> topMostResourceScore = resourcesContext
        .values().get("http://dbtune.org/musicbrainz/resource/instrument/323",
            JsonPointer.compile("/fts/score"));
    assertTrue(topMostResourceScore.isPresent());
    assertThat(topMostResourceScore.get().asDouble(), is(equalTo(1.0)));
    Optional<JsonNode> furthestDownResourceScore = resourcesContext
        .values().get("http://dbtune.org/musicbrainz/resource/instrument/475",
            JsonPointer.compile("/fts/score"));
    assertTrue(furthestDownResourceScore.isPresent());
    assertThat(furthestDownResourceScore.get().asDouble(), is(equalTo(0.0)));
  }
}
