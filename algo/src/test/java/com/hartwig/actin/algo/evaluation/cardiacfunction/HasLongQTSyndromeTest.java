package com.hartwig.actin.algo.evaluation.cardiacfunction;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.doid.TestDoidModelFactory;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorOtherCondition;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class HasLongQTSyndromeTest {

    @Test
    public void canEvaluate() {
        HasLongQTSyndrome function = new HasLongQTSyndrome(TestDoidModelFactory.createMinimalTestDoidModel());

        // No conditions
        List<PriorOtherCondition> conditions = Lists.newArrayList();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withPriorOtherConditions(conditions)));

        // A different condition
        conditions.add(builder().addDoids("wrong doid").build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withPriorOtherConditions(conditions)));

        // The correct condition
        conditions.add(builder().addDoids(HasLongQTSyndrome.LONG_QT_SYNDROME_DOID).build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withPriorOtherConditions(conditions)));
    }

    @NotNull
    private static ImmutablePriorOtherCondition.Builder builder() {
        return ImmutablePriorOtherCondition.builder().name(Strings.EMPTY).category(Strings.EMPTY);
    }

    @NotNull
    private static PatientRecord withPriorOtherConditions(@NotNull List<PriorOtherCondition> conditions) {
        return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .clinical(ImmutableClinicalRecord.builder()
                        .from(TestClinicalFactory.createMinimalTestClinicalRecord())
                        .priorOtherConditions(conditions)
                        .build())
                .build();
    }

}