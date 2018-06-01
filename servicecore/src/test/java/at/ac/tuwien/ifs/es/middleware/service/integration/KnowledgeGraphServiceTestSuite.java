package at.ac.tuwien.ifs.es.middleware.service.integration;

import at.ac.tuwien.ifs.es.middleware.service.integration.knowledegraph.CentralityMetricsServiceTest;
import at.ac.tuwien.ifs.es.middleware.service.integration.knowledegraph.InformationContentServiceTest;
import at.ac.tuwien.ifs.es.middleware.service.integration.knowledegraph.SimilarityMetricsServiceTest;
import at.ac.tuwien.ifs.es.middleware.service.integration.knowledegraph.SimpleSPARQLServiceTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * This test suite is testing services on the knowledge graph.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@RunWith(Suite.class)
@SuiteClasses({CentralityMetricsServiceTest.class, InformationContentServiceTest.class,
    SimilarityMetricsServiceTest.class, SimpleSPARQLServiceTest.class})
public class KnowledgeGraphServiceTestSuite {



}
