package at.ac.tuwien.ifs.es.middleware.service.integration;

import at.ac.tuwien.ifs.es.middleware.service.integration.analysis.ClassEntropyServiceTests;
import at.ac.tuwien.ifs.es.middleware.service.integration.analysis.AllClassesServiceTests;
import at.ac.tuwien.ifs.es.middleware.service.integration.analysis.DegreeCentralityMetricServiceTests;
import at.ac.tuwien.ifs.es.middleware.service.integration.analysis.LeastCommonSubsumerServiceTests;
import at.ac.tuwien.ifs.es.middleware.service.integration.analysis.PageRankCentralityMetricServiceTests;
import at.ac.tuwien.ifs.es.middleware.service.integration.analysis.PeerPressureClusteringMetricServiceTests;
import at.ac.tuwien.ifs.es.middleware.service.integration.analysis.ResnikSimilarityMetricServiceTests;
import at.ac.tuwien.ifs.es.middleware.service.integration.analysis.SameAsResourceServiceTests;
import at.ac.tuwien.ifs.es.middleware.service.integration.analysis.UnweightedLDSDMetricServiceTests;
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
    AllClassesServiceTests.class, PageRankCentralityMetricServiceTests.class,
    LeastCommonSubsumerServiceTests.class, PeerPressureClusteringMetricServiceTests.class,
    ResnikSimilarityMetricServiceTests.class, SameAsResourceServiceTests.class,
    UnweightedLDSDMetricServiceTests.class})
public class AnalysisServiceTestSuite {

}
