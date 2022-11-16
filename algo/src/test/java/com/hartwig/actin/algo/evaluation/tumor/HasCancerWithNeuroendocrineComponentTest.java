package com.hartwig.actin.algo.evaluation.tumor;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.doid.DoidModel;
import com.hartwig.actin.doid.TestDoidModelFactory;
import com.hartwig.actin.molecular.datamodel.ImmutableMolecularRecord;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableMolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.TestHomozygousDisruptionFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestLossFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class HasCancerWithNeuroendocrineComponentTest {

    @Test
    public void canEvaluate() {
        String matchDoid = "matching doid";
        DoidModel doidModel = TestDoidModelFactory.createWithOneDoidAndTerm(matchDoid,
                HasCancerWithNeuroendocrineComponent.NEUROENDOCRINE_TERMS.iterator().next());
        HasCancerWithNeuroendocrineComponent function = new HasCancerWithNeuroendocrineComponent(doidModel);

        // Can't determine when nothing known about tumor
        assertEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(TumorTestFactory.withTumorDetails(TumorTestFactory.builder().build())));

        // Fail when tumor is of non-neuroendocrine type.
        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(TumorTestFactory.withTumorDetails(TumorTestFactory.builder().addDoids("other").build())));

        // Can't be sure when tumor has a small cell component.
        assertEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(TumorTestFactory.withTumorDetails(TumorTestFactory.builder()
                        .addDoids(HasCancerWithSmallCellComponent.SMALL_CELL_DOIDS.iterator().next())
                        .build())));

        // Can't be sure if tumor has a neuroendocrine profile
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(createWithNeuroendocrineProfile()));

        // Pass when tumor has a doid with a neuroendocrine term
        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(TumorTestFactory.withTumorDetails(TumorTestFactory.builder().addDoids(matchDoid).build())));

        // Pass when tumor has a doid that is configured as neuroendocrine
        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(TumorTestFactory.withTumorDetails(TumorTestFactory.builder()
                        .addDoids(HasCancerWithNeuroendocrineComponent.NEUROENDOCRINE_DOIDS.iterator().next())
                        .build())));

        // Pass when tumor has been annotated as neuroendocrine
        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(TumorTestFactory.withTumorDetails(TumorTestFactory.builder()
                        .primaryTumorExtraDetails(
                                HasCancerWithNeuroendocrineComponent.NEUROENDOCRINE_EXTRA_DETAILS.iterator().next() + " tumor")
                        .build())));
    }

    @NotNull
    private static PatientRecord createWithNeuroendocrineProfile() {
        PatientRecord base = TestDataFactory.createMinimalTestPatientRecord();
        return ImmutablePatientRecord.builder()
                .from(base)
                .molecular(ImmutableMolecularRecord.builder()
                        .from(base.molecular())
                        .drivers(ImmutableMolecularDrivers.builder()
                                .from(base.molecular().drivers())
                                .addLosses(TestLossFactory.builder().isReportable(true).gene("TP53").build())
                                .addHomozygousDisruptions(TestHomozygousDisruptionFactory.builder().isReportable(true).gene("RB1").build())
                                .build())
                        .build())
                .build();

    }
}