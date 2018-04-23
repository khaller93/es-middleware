package at.ac.tuwien.ifs.es.middleware.dto.unit;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ResourceList;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;


/**
 * This class should unit test {@link at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ResourceList}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class ResourceListTest extends AbstractExplorationContextTest<Resource> {

  @Override
  protected ExplorationContext<Resource> getContext() {
    return new ResourceList();
  }

  private ResourceList resourceList;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    Map<String, String> resourceMap = new HashMap<>();
    resourceMap.put("http://dbtune.org/musicbrainz/resource/instrument/323", "{\n"
        + "            \"fts\": {\n"
        + "                \"score\": 4.396443\n"
        + "            },\n"
        + "            \"describe\": {\n"
        + "                \"label\": {\n"
        + "                    \"values\": {\n"
        + "                        \"en\": [\n"
        + "                            \"Warr guitar\"\n"
        + "                        ]\n"
        + "                    },\n"
        + "                    \"@type\": \"text\"\n"
        + "                }\n"
        + "            }\n"
        + "        }");
    resourceMap.put("http://dbtune.org/musicbrainz/resource/instrument/377", "{\n"
        + "            \"fts\": {\n"
        + "                \"score\": 4.396443\n"
        + "            },\n"
        + "            \"describe\": {\n"
        + "                \"label\": {\n"
        + "                    \"values\": {\n"
        + "                        \"en\": [\n"
        + "                            \"Baritone guitar\"\n"
        + "                        ]\n"
        + "                    },\n"
        + "                    \"@type\": \"text\"\n"
        + "                }\n"
        + "            }\n"
        + "        }");
    resourceMap.put("http://dbtune.org/musicbrainz/resource/instrument/399", "{\n"
        + "            \"fts\": {\n"
        + "                \"score\": 4.396443\n"
        + "            },\n"
        + "            \"describe\": {\n"
        + "                \"label\": {\n"
        + "                    \"values\": {\n"
        + "                        \"en\": [\n"
        + "                            \"Vietnamese guitar\"\n"
        + "                        ]\n"
        + "                    },\n"
        + "                    \"@type\": \"text\"\n"
        + "                }\n"
        + "            }\n"
        + "        }");
    resourceMap.put("http://dbtune.org/musicbrainz/resource/instrument/467", "{\n"
        + "            \"fts\": {\n"
        + "                \"score\": 4.396443\n"
        + "            },\n"
        + "            \"describe\": {\n"
        + "                \"label\": {\n"
        + "                    \"values\": {\n"
        + "                        \"en\": [\n"
        + "                            \"Resonator guitar\"\n"
        + "                        ]\n"
        + "                    },\n"
        + "                    \"@type\": \"text\"\n"
        + "                }\n"
        + "            }\n"
        + "        }");
    resourceMap.put("http://dbtune.org/musicbrainz/resource/instrument/76", "{\n"
        + "            \"fts\": {\n"
        + "                \"score\": 4.396443\n"
        + "            },\n"
        + "            \"describe\": {\n"
        + "                \"label\": {\n"
        + "                    \"values\": {\n"
        + "                        \"en\": [\n"
        + "                            \"Acoustic guitar\"\n"
        + "                        ]\n"
        + "                    },\n"
        + "                    \"@type\": \"text\"\n"
        + "                }\n"
        + "            }\n"
        + "        }");
    resourceMap.put("http://dbtune.org/musicbrainz/resource/instrument/79", "{\n"
        + "            \"fts\": {\n"
        + "                \"score\": 4.396443\n"
        + "            },\n"
        + "            \"describe\": {\n"
        + "                \"label\": {\n"
        + "                    \"values\": {\n"
        + "                        \"en\": [\n"
        + "                            \"Slide guitar\"\n"
        + "                        ]\n"
        + "                    },\n"
        + "                    \"@type\": \"text\"\n"
        + "                }\n"
        + "            }\n"
        + "        }");
    resourceMap.put("http://dbtune.org/musicbrainz/resource/instrument/80", "{\n"
        + "            \"fts\": {\n"
        + "                \"score\": 4.396443\n"
        + "            },\n"
        + "            \"describe\": {\n"
        + "                \"label\": {\n"
        + "                    \"values\": {\n"
        + "                        \"en\": [\n"
        + "                            \"Steel guitar\"\n"
        + "                        ]\n"
        + "                    },\n"
        + "                    \"@type\": \"text\"\n"
        + "                }\n"
        + "            }\n"
        + "        }");
    resourceMap.put("http://dbtune.org/musicbrainz/resource/instrument/206", "{\n"
        + "            \"fts\": {\n"
        + "                \"score\": 3.5171542\n"
        + "            },\n"
        + "            \"describe\": {\n"
        + "                \"label\": {\n"
        + "                    \"values\": {\n"
        + "                        \"en\": [\n"
        + "                            \"Spanish acoustic guitar\"\n"
        + "                        ]\n"
        + "                    },\n"
        + "                    \"@type\": \"text\"\n"
        + "                }\n"
        + "            }\n"
        + "        }");
    resourceMap.put("http://dbtune.org/musicbrainz/resource/instrument/468", "{\n"
        + "            \"fts\": {\n"
        + "                \"score\": 3.5171542\n"
        + "            },\n"
        + "            \"describe\": {\n"
        + "                \"label\": {\n"
        + "                    \"values\": {\n"
        + "                        \"en\": [\n"
        + "                            \"Table steel guitar\"\n"
        + "                        ]\n"
        + "                    },\n"
        + "                    \"@type\": \"text\"\n"
        + "                }\n"
        + "            }\n"
        + "        }");
    resourceMap.put("http://dbtune.org/musicbrainz/resource/instrument/469", "{\n"
        + "            \"fts\": {\n"
        + "                \"score\": 3.5171542\n"
        + "            },\n"
        + "            \"describe\": {\n"
        + "                \"label\": {\n"
        + "                    \"values\": {\n"
        + "                        \"en\": [\n"
        + "                            \"Pedal steel guitar\"\n"
        + "                        ]\n"
        + "                    },\n"
        + "                    \"@type\": \"text\"\n"
        + "                }\n"
        + "            }\n"
        + "        }");
    resourceList = ResourceList
        .of(resourceMap.keySet().stream().map(BlankOrIRIJsonUtil::valueOf)
            .collect(Collectors.toList()));
    ObjectMapper objectMapper = new ObjectMapper();
    for (Entry<String, String> e : resourceMap.entrySet()) {
      resourceList.putValuesData(e.getKey(), Collections.emptyList(),
          objectMapper.readTree(e.getValue()));
    }
    resourceList.setMetadataFor("test", JsonNodeFactory.instance.textNode("ok"));
  }

  @Test
  public void test_filterSingleResourceSteelGuitar_mustBeMissingInResultContext() throws Exception {
    ResourceList newResourceList = (ResourceList) resourceList.streamOfResults()
        .filter(r -> !"Steel guitar".equals(
            resourceList.getValues(r.getId(), JsonPointer.compile("/describe/label/values/en"))
                .get().iterator().next().asText())).collect(resourceList);
    assertThat(
        newResourceList.asResourceSet().stream().map(Resource::getId).collect(Collectors.toSet()),
        not(hasItem("http://dbtune.org/musicbrainz/resource/instrument/80")));
    assertThat("The value information for the 'steel guitar' must also be removed.",
        newResourceList.getAllValues().keySet(),
        not(hasItem("http://dbtune.org/musicbrainz/resource/instrument/80")));
    Optional<JsonNode> testMetadata = resourceList.getMetadataFor("test");
    assertTrue(testMetadata.isPresent());
    assertThat("Metadata must be still there", testMetadata.get().asText(), is(equalTo("ok")));
  }

  @Test
  public void test_collectUnchangedResultCollection_mustResultInIdenticalContext()
      throws Exception {
    ResourceList newResourceList = (ResourceList) resourceList.streamOfResults()
        .collect(resourceList);
    assertThat(newResourceList.getMetadataEntryNames(),
        containsInAnyOrder(newResourceList.getMetadataEntryNames().toArray(new String[0])));
    assertThat(newResourceList.getAllValues().keySet(),
        containsInAnyOrder(resourceList.getAllValues().keySet().toArray(new String[0])));
    assertThat(resourceList,
        containsInAnyOrder(resourceList.asResourceList().toArray(new Resource[0])));
  }
}
