package at.ac.tuwien.ifs.es.middleware.tests.integration;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import at.ac.tuwien.ifs.es.middleware.ExploratorySearchApplication;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ResourceList;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.testutil.MusicPintaInstrumentsResource;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.rdf.api.IRI;
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
    "esm.db.choice=IndexedMemoryDB",
    "esm.fts.choice=IndexedMemoryDB"})
public class ExploratoryControllerMusicPintaFTSTest {

  @Autowired
  private TestRestTemplate restTemplate;
  @Autowired
  private ObjectMapper payloadMapper;

  @Rule
  @Autowired
  public MusicPintaInstrumentsResource musicPintaResource;

  @Test
  public void test_searchForInstrument_mustReturnCorrespondingMatchesForGuitar()
      throws IOException {
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

  @Test
  public void test_searchForInstrument_mustReturnExactly5ResultsWithOffset() throws Exception {
    ResponseEntity<String> guitarFTSearchResponse = restTemplate
        .getForEntity("/explore/with/fts/guitar?offset={offset}&limit={limit}", String.class, 7, 6);
    ResourceList result = payloadMapper
        .readValue(guitarFTSearchResponse.getBody(), ResourceList.class);
    assertTrue(guitarFTSearchResponse.getStatusCode().is2xxSuccessful());

    List<String> resourceIds = result.asResourceList().stream().map(Resource::getId)
        .collect(Collectors.toList());
    assertThat(resourceIds, hasSize(6));
    assertThat(resourceIds,
        hasItems("http://dbtune.org/musicbrainz/resource/instrument/206",
            "http://dbtune.org/musicbrainz/resource/instrument/468",
            "http://dbtune.org/musicbrainz/resource/instrument/469"));
    assertThat(resourceIds, not(hasItem("http://dbtune.org/musicbrainz/resource/instrument/76")));
    assertThat(resourceIds, not(hasItem("http://dbtune.org/musicbrainz/resource/instrument/475")));
  }

  @Test
  public void test_searchForPerformance_mustReturnNoResult() throws Exception {
    ResponseEntity<String> guitarFTSearchResponse = restTemplate
        .getForEntity("/explore/with/fts/guitar?classes={classes}", String.class,
            "http://purl.org/ontology/mo/MusicArtist");
    assertTrue(guitarFTSearchResponse.getStatusCode().is2xxSuccessful());

    ResourceList result = payloadMapper
        .readValue(guitarFTSearchResponse.getBody(), ResourceList.class);
    assertThat(result.asResourceList(), hasSize(0));
  }

  @Test
  public void test_searchForPerformanceAndInstrument_mustReturnTop5GuitarMatches()
      throws Exception {
    ResponseEntity<String> guitarFTSearchResponse = restTemplate
        .getForEntity("/explore/with/fts/guitar?classes={classes}&limit={limit}", String.class,
            String.join(",", Arrays.asList("http://purl.org/ontology/mo/Performance",
                "http://purl.org/ontology/mo/Instrument")), 20);
    assertTrue(guitarFTSearchResponse.getStatusCode().is2xxSuccessful());

    ResourceList result = payloadMapper
        .readValue(guitarFTSearchResponse.getBody(), ResourceList.class);
    assertThat(result.asResourceList(), hasSize(20));
    List<String> resourceIds = result.asResourceList().stream().map(Resource::getId)
        .collect(Collectors.toList());
    assertThat(resourceIds,
        hasItems("http://dbtune.org/musicbrainz/resource/instrument/323",
            "http://dbtune.org/musicbrainz/resource/instrument/377",
            "http://dbtune.org/musicbrainz/resource/instrument/399",
            "http://dbtune.org/musicbrainz/resource/instrument/467",
            "http://dbtune.org/musicbrainz/resource/instrument/76",
            "http://dbtune.org/musicbrainz/resource/instrument/468"));
  }
}
