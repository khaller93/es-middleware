package at.ac.tuwien.ifs.es.middleware.tests.integration;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import at.ac.tuwien.ifs.es.middleware.ExploratorySearchApplication;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ResourceList;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.testutil.MusicPintaInstrumentsResource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;
import org.apache.commons.rdf.api.IRI;
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

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ExploratorySearchApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "esm.db.choice=IndexedMemoryDB",
    "esm.fts.choice=IndexedMemoryDB"})
public class DynamicExploratoryMusicPintaFlowTest {

  @Autowired
  private TestRestTemplate restTemplate;
  @Autowired
  private ObjectMapper parameterMapper;

  @Rule
  @Autowired
  public MusicPintaInstrumentsResource musicPintaResource;

  private static Map<String, String> jsonTestMap = new HashMap<>();

  @BeforeClass
  public static void setUp() throws Exception {
    for (Entry<String, String> e : ImmutableMap.<String, String>builder()
        .put("simpleAllSource", "/dynamicflow/simpleAllSource.json")
        .put("simpleExcludingAllSource", "/dynamicflow/simpleExcludingAllSource.json")
        .put("naiveLimit", "/dynamicflow/naiveLimit.json")
        .put("simpleDescribe", "/dynamicflow/simpleDescribe.json")
        .put("simpleGuitarFTS", "/dynamicflow/simpleGuitarFTS.json")
        .put("guitarFTSWithNiceDescription", "/dynamicflow/customGuitarFTSDescriber.json")
        .build().entrySet()) {
      try (InputStream in = DynamicExploratoryMusicPintaFlowTest.class
          .getResourceAsStream(e.getValue())) {
        jsonTestMap.put(e.getKey(), IOUtils.toString(in, "utf-8"));
      }
    }
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
    List<String> resourceIRIs = resources.getResultsCollection().stream()
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
    List<String> resourceIRIs = resources.getResultsCollection().stream()
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
    assertTrue(limit5Response.getStatusCode().is2xxSuccessful());
    ResourceList resources = parameterMapper
        .readValue(limit5Response.getBody(), ResourceList.class);
    assertThat(resources.getResultsCollection(), hasSize(100));
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
        .get("http://dbpedia.org/resource/Santur",
            Arrays.asList("describe", "label", "values", "en"))
        .get().get(0).asText(), is("Santur"));
    assertThat(resources
        .get("http://dbpedia.org/resource/Santur",
            Arrays.asList("describe", "label", "values", "en"))
        .get().get(0).asText(), is("Santur"));
    assertThat(resources
            .get("http://dbpedia.org/resource/Tembor",
                Arrays.asList("describe", "description", "values", "en")).get().get(0)
            .asText(),
        is("The Tembor is a stringed musical instrument from the Uyghur region, Western China. It has 5 strings in 3 courses and is tuned A A, D, G G. The strings are made of Steel."));
    assertFalse("The 'Tambura' resource has no description.",
        resources.get("http://dbtune.org/musicbrainz/resource/instrument/473",
            Arrays.asList("describe", "description", "value")).isPresent());
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
    List<String> resourceIRIs = resources.getResultsCollection().stream()
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
    List<String> resourceIRIs = resources.getResultsCollection().stream().map(Resource::getId)
        .collect(Collectors.toList());
    assertThat(resourceIRIs, hasItems("http://dbpedia.org/resource/Classical_guitar",
        "http://dbpedia.org/resource/Bass_guitar",
        "http://dbpedia.org/resource/Electric_guitar",
        "http://dbtune.org/musicbrainz/resource/instrument/377"));

    Optional<JsonNode> electricGuitarLabelOptional = resources
        .get("http://dbtune.org/musicbrainz/resource/instrument/78",
            Arrays.asList("describe", "label", "values", "en"));
    assertTrue(electricGuitarLabelOptional.isPresent());
    assertThat(electricGuitarLabelOptional.get().get(0).asText(), is(equalTo("Electric guitar")));
    Optional<JsonNode> electricGuitarDescriptionOptional = resources
        .get("http://dbtune.org/musicbrainz/resource/instrument/78",
            Arrays.asList("describe", "description"));
    assertFalse(
        "The description must not be given, because the custom describer did not specify this content.",
        electricGuitarDescriptionOptional.isPresent());
    Optional<JsonNode> electricGuitarThumbOptional = resources
        .get("http://dbtune.org/musicbrainz/resource/instrument/78",
            Arrays.asList("describe", "thumb", "values"));
    assertTrue(electricGuitarThumbOptional.isPresent());
    assertThat(electricGuitarThumbOptional.get().get(0).asText(), is(equalTo(
        "http://upload.wikimedia.org/wikipedia/commons/thumb/d/dc/Godin_LG-Squier_Strat.jpg/200px-Godin_LG-Squier_Strat.jpg")));

    Optional<JsonNode> guitaleleLabelOptional = resources
        .get("http://dbpedia.org/resource/Guitalele",
            Arrays.asList("describe", "label", "values", "en"));
    assertTrue(guitaleleLabelOptional.isPresent());
    assertThat("http://dbpedia.org/resource/Guitalele",
        guitaleleLabelOptional.get().get(0).asText(),
        is(equalTo("Guitalele")));
    Optional<JsonNode> guitaleleThumbOptional = resources
        .get("http://dbpedia.org/resource/Guitalele", Arrays.asList("describe", "thumb", "values"));
    assertFalse("There is no thumbnail of a 'guitalele' in the data",
        guitaleleThumbOptional.isPresent());
  }

}
