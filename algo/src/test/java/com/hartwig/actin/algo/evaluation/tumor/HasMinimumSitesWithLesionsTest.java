package com.hartwig.actin.algo.evaluation.tumor;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.Collections;
import java.util.List;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasMinimumSitesWithLesionsTest {

    private final PatientRecord testPatient = patient(true, false, false, false, false, true, List.of("Prostate", "Subcutaneous"));

    @Test
    public void shouldPassWhenNumberOfCategorizedLesionsEqualThresholdAndNoOtherLesionsArePresent() {
        assertEvaluation(EvaluationResult.PASS,
                new HasMinimumSitesWithLesions(6).evaluate(patient(true, true, true, true, true, true, Collections.emptyList())));
    }

    @Test
    public void shouldPassWhenNumberOfCategorizedLesionsAreOneLessThanThresholdAndOtherLesionsArePresent() {
        assertEvaluation(EvaluationResult.PASS, new HasMinimumSitesWithLesions(3).evaluate(testPatient));
    }

    @Test
    public void shouldBeUndeterminedWhenThresholdIsBetweenUpperAndLowerLesionSiteLimits() {
        assertEvaluation(EvaluationResult.UNDETERMINED, new HasMinimumSitesWithLesions(4).evaluate(testPatient));
    }

    @Test
    public void shouldBeUndeterminedWhenThresholdIsOneMoreThanCountOfAllCategorizedAndUncategorizedLesionLocations() {
        assertEvaluation(EvaluationResult.UNDETERMINED, new HasMinimumSitesWithLesions(5).evaluate(testPatient));
    }

    @Test
    public void shouldFailWhenLesionSiteUpperLimitIsLessThanThreshold() {
        assertEvaluation(EvaluationResult.FAIL, new HasMinimumSitesWithLesions(6).evaluate(testPatient));
    }

    @Test
    public void shouldNotCountNullBooleanFieldsOrEmptyOtherLesionsAsSites() {
        assertEvaluation(EvaluationResult.UNDETERMINED,
                new HasMinimumSitesWithLesions(1).evaluate(patient(null, null, null, null, null, null, Collections.emptyList())));
    }

    private static PatientRecord patient(Boolean hasBoneLesions, Boolean hasBrainLesions, Boolean hasCnsLesions, Boolean hasLiverLesions,
            Boolean hasLungLesions, Boolean hasLymphNodeLesions, List<String> otherLesions) {
        return TumorTestFactory.withTumorDetails(TumorTestFactory.builder()
                .hasBoneLesions(hasBoneLesions)
                .hasBrainLesions(hasBrainLesions)
                .hasCnsLesions(hasCnsLesions)
                .hasLiverLesions(hasLiverLesions)
                .hasLungLesions(hasLungLesions)
                .hasLymphNodeLesions(hasLymphNodeLesions)
                .otherLesions(otherLesions)
                .build());
    }
}