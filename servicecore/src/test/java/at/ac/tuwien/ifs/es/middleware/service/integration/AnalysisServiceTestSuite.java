package at.ac.tuwien.ifs.es.middleware.service.integration;

import at.ac.tuwien.ifs.es.middleware.service.integration.analysis.ClassEntropyServiceTests;
import at.ac.tuwien.ifs.es.middleware.service.integration.analysis.ClassInformationServiceTests;
import at.ac.tuwien.ifs.es.middleware.service.integration.analysis.DegreeCentralityMetricServiceTests;
import at.ac.tuwien.ifs.es.middleware.service.integration.analysis.PageRankCentralityMetricServiceTests;
import at.ac.tuwien.ifs.es.middleware.service.integration.analysis.SameAsResourceServiceTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * This test suite is testing analysis services on the knowledge graph.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@RunWith(Suite.class)
@SuiteClasses({ClassEntropyServiceTests.class, DegreeCentralityMetricServiceTests.class,
    ClassInformationServiceTests.class, PageRankCentralityMetricServiceTests.class,
    SameAsResourceServiceTests.class})
public class AnalysisServiceTestSuite {

}
