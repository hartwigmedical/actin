package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.molecular.datamodel.driver.TestLossFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory;

import org.junit.Test;

public class IsHomologousRepairDeficientTest {

    @Test
    public void canEvaluate() {
        IsHomologousRepairDeficient function = new IsHomologousRepairDeficient();

        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(MolecularTestFactory.withHomologousRepairDeficiencyAndVariant(null,
                        TestVariantFactory.builder().gene(IsHomologousRepairDeficient.HRD_GENES.iterator().next()).isReportable(false).build())));

        assertEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(MolecularTestFactory.withHomologousRepairDeficiencyAndVariant(null,
                        TestVariantFactory.builder().gene(IsHomologousRepairDeficient.HRD_GENES.iterator().next()).isReportable(true).build())));

        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(MolecularTestFactory.withHomologousRepairDeficiencyAndVariant(true,
                        TestVariantFactory.builder().gene(IsHomologousRepairDeficient.HRD_GENES.iterator().next()).isReportable(true).build())));


        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(MolecularTestFactory.withHomologousRepairDeficiencyAndLoss(true,
                        TestLossFactory.builder().gene(IsHomologousRepairDeficient.HRD_GENES.iterator().next()).build())));

        assertEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withHomologousRepairDeficiencyAndVariant(true,
                        TestVariantFactory.builder().gene(IsHomologousRepairDeficient.HRD_GENES.iterator().next()).isReportable(false).build())));

        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(MolecularTestFactory.withHomologousRepairDeficiencyAndVariant(false,
                        TestVariantFactory.builder().gene(IsHomologousRepairDeficient.HRD_GENES.iterator().next()).isReportable(true).build())));
    }
}