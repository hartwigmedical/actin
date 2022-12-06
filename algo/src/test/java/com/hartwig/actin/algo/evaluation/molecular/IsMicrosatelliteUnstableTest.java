package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.molecular.datamodel.driver.CopyNumberType;
import com.hartwig.actin.molecular.datamodel.driver.TestCopyNumberFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestDisruptionFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestHomozygousDisruptionFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory;

import org.junit.Test;

public class IsMicrosatelliteUnstableTest {

    @Test
    public void canEvaluate() {
        IsMicrosatelliteUnstable function = new IsMicrosatelliteUnstable();

        assertMolecularEvaluation(EvaluationResult.FAIL,
                function.evaluate(MolecularTestFactory.withMicrosatelliteInstabilityAndVariant(null,
                        TestVariantFactory.builder().gene(MolecularConstants.MSI_GENES.iterator().next()).isReportable(false).build())));

        assertMolecularEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(MolecularTestFactory.withMicrosatelliteInstabilityAndVariant(null,
                        TestVariantFactory.builder()
                                .gene(MolecularConstants.MSI_GENES.iterator().next())
                                .isReportable(true)
                                .isBiallelic(true)
                                .build())));

        assertMolecularEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(MolecularTestFactory.withMicrosatelliteInstabilityAndVariant(null,
                        TestVariantFactory.builder()
                                .gene(MolecularConstants.MSI_GENES.iterator().next())
                                .isReportable(true)
                                .isBiallelic(false)
                                .build())));

        assertMolecularEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withMicrosatelliteInstabilityAndVariant(true,
                        TestVariantFactory.builder()
                                .gene(MolecularConstants.MSI_GENES.iterator().next())
                                .isReportable(true)
                                .isBiallelic(false)
                                .build())));

        assertMolecularEvaluation(EvaluationResult.PASS,
                function.evaluate(MolecularTestFactory.withMicrosatelliteInstabilityAndVariant(true,
                        TestVariantFactory.builder()
                                .gene(MolecularConstants.MSI_GENES.iterator().next())
                                .isReportable(true)
                                .isBiallelic(true)
                                .build())));

        assertMolecularEvaluation(EvaluationResult.PASS,
                function.evaluate(MolecularTestFactory.withMicrosatelliteInstabilityAndLoss(true,
                        TestCopyNumberFactory.builder()
                                .type(CopyNumberType.LOSS)
                                .gene(MolecularConstants.MSI_GENES.iterator().next())
                                .build())));

        assertMolecularEvaluation(EvaluationResult.PASS,
                function.evaluate(MolecularTestFactory.withMicrosatelliteInstabilityAndHomozygousDisruption(true,
                        TestHomozygousDisruptionFactory.builder().gene(MolecularConstants.MSI_GENES.iterator().next()).build())));

        assertMolecularEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withMicrosatelliteInstabilityAndDisruption(true,
                        TestDisruptionFactory.builder().gene(MolecularConstants.MSI_GENES.iterator().next()).build())));

        assertMolecularEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withMicrosatelliteInstabilityAndVariant(true,
                        TestVariantFactory.builder()
                                .gene(MolecularConstants.MSI_GENES.iterator().next())
                                .isReportable(true)
                                .isBiallelic(false)
                                .build())));

        assertMolecularEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withMicrosatelliteInstabilityAndVariant(true,
                        TestVariantFactory.builder().gene(MolecularConstants.MSI_GENES.iterator().next()).isReportable(false).build())));

        assertMolecularEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withMicrosatelliteInstabilityAndVariant(true,
                        TestVariantFactory.builder().gene("other gene").isReportable(true).isBiallelic(false).build())));

        assertMolecularEvaluation(EvaluationResult.FAIL,
                function.evaluate(MolecularTestFactory.withMicrosatelliteInstabilityAndVariant(false,
                        TestVariantFactory.builder().gene(MolecularConstants.MSI_GENES.iterator().next()).isReportable(true).build())));
    }
}