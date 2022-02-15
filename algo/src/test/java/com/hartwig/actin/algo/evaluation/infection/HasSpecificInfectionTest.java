package com.hartwig.actin.algo.evaluation.infection;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.doid.DoidModel;
import com.hartwig.actin.algo.doid.TestDoidModelFactory;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorOtherCondition;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;
import com.hartwig.actin.clinical.datamodel.TestClinicalDataFactory;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class HasSpecificInfectionTest {

    @Test
    public void canEvaluate() {
        String doidToFind = "parent";
        DoidModel doidModel = TestDoidModelFactory.createWithOneParentChild(doidToFind, "child");
        HasSpecificInfection function = new HasSpecificInfection(doidModel, doidToFind);

        // Test empty doid
        List<PriorOtherCondition> priorOtherConditions = Lists.newArrayList();
        assertEquals(EvaluationResult.FAIL, function.evaluate(withPriorOtherConditions(priorOtherConditions)).result());

        // Add a condition with no DOIDs
        priorOtherConditions.add(builder().build());
        assertEquals(EvaluationResult.FAIL, function.evaluate(withPriorOtherConditions(priorOtherConditions)).result());

        // Add a condition with not the correct DOID
        priorOtherConditions.add(builder().addDoids("not the correct doid").build());
        assertEquals(EvaluationResult.FAIL, function.evaluate(withPriorOtherConditions(priorOtherConditions)).result());

        // Add a condition with child DOID
        priorOtherConditions.add(builder().addDoids("child", "some other doid").build());
        assertEquals(EvaluationResult.PASS, function.evaluate(withPriorOtherConditions(priorOtherConditions)).result());

        // Also pass on the exact DOID
        PriorOtherCondition exact = builder().addDoids(doidToFind).build();
        assertEquals(EvaluationResult.PASS, function.evaluate(withPriorOtherCondition(exact)).result());
    }

    @NotNull
    private static PatientRecord withPriorOtherCondition(@NotNull PriorOtherCondition priorOtherCondition) {
        return withPriorOtherConditions(Lists.newArrayList(priorOtherCondition));
    }

    @NotNull
    private static PatientRecord withPriorOtherConditions(@NotNull List<PriorOtherCondition> priorOtherConditions) {
        return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .clinical(ImmutableClinicalRecord.builder()
                        .from(TestClinicalDataFactory.createMinimalTestClinicalRecord())
                        .priorOtherConditions(priorOtherConditions)
                        .build())
                .build();
    }

    @NotNull
    private static ImmutablePriorOtherCondition.Builder builder() {
        return ImmutablePriorOtherCondition.builder().name(Strings.EMPTY).category(Strings.EMPTY);
    }
}