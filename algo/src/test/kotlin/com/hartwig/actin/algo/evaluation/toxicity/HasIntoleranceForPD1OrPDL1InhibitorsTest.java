package com.hartwig.actin.algo.evaluation.toxicity;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.Collection;
import java.util.Collections;

import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.doid.DoidConstants;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.Intolerance;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;
import com.hartwig.actin.clinical.datamodel.TestPriorOtherConditionFactory;
import com.hartwig.actin.doid.TestDoidModelFactory;

import org.junit.Test;

public class HasIntoleranceForPD1OrPDL1InhibitorsTest {

    private static final String DOID_AUTOIMMUNE_DISEASE_OF_CARDIOVASCULAR_SYSTEM = "0060051";

    @Test
    public void shouldPassWhenPatientHasIntoleranceMatchingList() {
        HasIntoleranceForPD1OrPDL1Inhibitors.INTOLERANCE_TERMS.forEach(term -> {
            PatientRecord record = patient(Collections.singletonList(ToxicityTestFactory.intolerance()
                    .name("intolerance to " + term.toUpperCase())
                    .build()), DOID_AUTOIMMUNE_DISEASE_OF_CARDIOVASCULAR_SYSTEM);
            assertEvaluation(EvaluationResult.PASS, function().evaluate(record));
        });
    }

    @Test
    public void shouldWarnWhenPatientHasPriorConditionBelongingToAutoimmuneDiseaseDoid() {
        assertEvaluation(EvaluationResult.WARN,
                function().evaluate(patient(Collections.emptyList(), DOID_AUTOIMMUNE_DISEASE_OF_CARDIOVASCULAR_SYSTEM)));
    }

    @Test
    public void shouldFailWhenPatientHasNoMatchingIntoleranceOrAutoimmuneDiseaseCondition() {
        assertEvaluation(EvaluationResult.FAIL,
                function().evaluate(patient(Collections.singletonList(ToxicityTestFactory.intolerance().name("other").build()), "123")));
    }

    private static HasIntoleranceForPD1OrPDL1Inhibitors function() {
        return new HasIntoleranceForPD1OrPDL1Inhibitors(TestDoidModelFactory.createWithOneParentChild(DoidConstants.AUTOIMMUNE_DISEASE_DOID,
                DOID_AUTOIMMUNE_DISEASE_OF_CARDIOVASCULAR_SYSTEM));
    }

    private static PatientRecord patient(Collection<Intolerance> intolerances, String priorConditionDoid) {
        PatientRecord minimalPatient = TestDataFactory.createMinimalTestPatientRecord();
        PriorOtherCondition priorCondition =
                TestPriorOtherConditionFactory.builder().addDoids(priorConditionDoid).isContraindicationForTherapy(true).build();
        return ImmutablePatientRecord.copyOf(minimalPatient)
                .withClinical(ImmutableClinicalRecord.copyOf(minimalPatient.clinical())
                        .withIntolerances(intolerances)
                        .withPriorOtherConditions(priorCondition));
    }
}