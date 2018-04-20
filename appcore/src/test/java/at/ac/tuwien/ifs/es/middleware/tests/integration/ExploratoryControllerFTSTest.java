package at.ac.tuwien.ifs.es.middleware.tests.integration;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import at.ac.tuwien.ifs.es.middleware.ExploratorySearchApplication;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ResourceList;
import at.ac.tuwien.ifs.es.middleware.testutil.MusicPintaInstrumentsResource;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.rdf.api.IRI;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ExploratorySearchApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "esm.knowledgegraph.choice=IndexedMemoryDB",
    "esm.fts.choice=IndexedMemoryDB"})
public class ExploratoryControllerFTSTest {

  @Autowired
  private TestRestTemplate restTemplate;
  @Autowired
  private ObjectMapper payloadMapper;

  @Rule
  @Autowired
  public MusicPintaInstrumentsResource musicPintaResource;

  private static Map<String, String> jsonTestMap = new HashMap<>();

  @BeforeClass
  public static void setUp() throws Exception {

  }

  @Test
  public void test_searchForInstrument_mustReturnCorrespondingMatches() throws IOException {
    ResponseEntity<String> guitarFTSearchResponse = restTemplate
        .getForEntity("/explore/with/fts/guitar", String.class);
    assertThat("The request must be successful.", guitarFTSearchResponse.getStatusCode().value(),
        is(200));
    ResourceList result = payloadMapper
        .readValue(guitarFTSearchResponse.getBody(), ResourceList.class);
    assertThat(result, instanceOf(ResourceList.class));
    List<String> resources = new LinkedList<>();
    result.getResourceIterator().forEachRemaining(
        resource -> {
          if (resource.value() instanceof IRI) {
            resources.add(((IRI) resource.value()).getIRIString());
          }
        });
    assertThat("Testing samples must be included.", resources,
        hasItems("http://dbtune.org/musicbrainz/resource/instrument/76",
            "http://dbtune.org/musicbrainz/resource/instrument/323",
            "http://dbtune.org/musicbrainz/resource/instrument/469"));
  }

}
