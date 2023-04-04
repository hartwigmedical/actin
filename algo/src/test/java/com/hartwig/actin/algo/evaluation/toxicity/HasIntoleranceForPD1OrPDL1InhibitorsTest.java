package com.hartwig.actin.algo.evaluation.toxicity;

import static org.junit.Assert.assertEquals;

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
    private static final PatientRecord MINIMAL_PATIENT = TestDataFactory.createMinimalTestPatientRecord();

    @Test
    public void shouldPassWhenPatientHasIntoleranceMatchingList() {
        HasIntoleranceForPD1OrPDL1Inhibitors.INTOLERANCE_TERMS.forEach(term -> {
            PatientRecord record = patient(Collections.singletonList(ToxicityTestFactory.intolerance()
                    .name("intolerance to " + term.toUpperCase())
                    .build()), DOID_AUTOIMMUNE_DISEASE_OF_CARDIOVASCULAR_SYSTEM);
            assertEquals(EvaluationResult.PASS, function().evaluate(record).result());
        });
    }

    @Test
    public void shouldWarnWhenPatientHasPriorConditionBelongingToAutoimmuneDiseaseDoid() {
        assertEquals(EvaluationResult.WARN,
                function().evaluate(patient(Collections.emptyList(), DOID_AUTOIMMUNE_DISEASE_OF_CARDIOVASCULAR_SYSTEM)).result());
    }

    @Test
    public void shouldFailWhenPatientHasNoMatchingIntoleranceOrAutoimmuneDiseaseCondition() {
        assertEquals(EvaluationResult.FAIL,
                function().evaluate(patient(Collections.singletonList(ToxicityTestFactory.intolerance().name("other").build()), "123"))
                        .result());
    }

    private static HasIntoleranceForPD1OrPDL1Inhibitors function() {
        return new HasIntoleranceForPD1OrPDL1Inhibitors(TestDoidModelFactory.createWithOneParentChild(DoidConstants.AUTOIMMUNE_DISEASE_DOID,
                DOID_AUTOIMMUNE_DISEASE_OF_CARDIOVASCULAR_SYSTEM));
    }

    private static PatientRecord patient(Collection<Intolerance> intolerances, String priorConditionDoid) {
        PriorOtherCondition priorCondition =
                TestPriorOtherConditionFactory.builder().addDoids(priorConditionDoid).isContraindicationForTherapy(true).build();
        return ImmutablePatientRecord.copyOf(MINIMAL_PATIENT)
                .withClinical(ImmutableClinicalRecord.copyOf(MINIMAL_PATIENT.clinical())
                        .withIntolerances(intolerances)
                        .withPriorOtherConditions(priorCondition));
    }
}