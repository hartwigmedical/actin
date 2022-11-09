package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.molecular.datamodel.driver.TestLossFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory;

import org.junit.Test;

public class IsHomologousRepairDeficientTest {

    @Test
    public void canEvaluate() {
        IsHomologousRepairDeficient function = new IsHomologousRepairDeficient();

        assertMolecularEvaluation(EvaluationResult.FAIL,
                function.evaluate(MolecularTestFactory.withHomologousRepairDeficiencyAndVariant(null,
                        TestVariantFactory.builder()
                                .gene(MolecularConstants.HRD_GENES.iterator().next())
                                .isReportable(false)
                                .build())));

        assertMolecularEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(MolecularTestFactory.withHomologousRepairDeficiencyAndVariant(null,
                        TestVariantFactory.builder()
                                .gene(MolecularConstants.HRD_GENES.iterator().next())
                                .isReportable(true)
                                .build())));

        assertMolecularEvaluation(EvaluationResult.PASS,
                function.evaluate(MolecularTestFactory.withHomologousRepairDeficiencyAndVariant(true,
                        TestVariantFactory.builder()
                                .gene(MolecularConstants.HRD_GENES.iterator().next())
                                .isReportable(true)
                                .build())));

        assertMolecularEvaluation(EvaluationResult.PASS,
                function.evaluate(MolecularTestFactory.withHomologousRepairDeficiencyAndLoss(true,
                        TestLossFactory.builder().gene(MolecularConstants.HRD_GENES.iterator().next()).build())));

        assertMolecularEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withHomologousRepairDeficiencyAndVariant(true,
                        TestVariantFactory.builder()
                                .gene(MolecularConstants.HRD_GENES.iterator().next())
                                .isReportable(false)
                                .build())));

        assertMolecularEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withHomologousRepairDeficiencyAndVariant(true,
                        TestVariantFactory.builder().gene("other gene").isReportable(true).build())));

        assertMolecularEvaluation(EvaluationResult.FAIL,
                function.evaluate(MolecularTestFactory.withHomologousRepairDeficiencyAndVariant(false,
                        TestVariantFactory.builder()
                                .gene(MolecularConstants.HRD_GENES.iterator().next())
                                .isReportable(true)
                                .build())));
    }
}