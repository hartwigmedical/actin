package com.hartwig.actin.algo.evaluation.tumor;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.Collections;
import java.util.List;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails;

import org.junit.Test;

public class HasMinimumSitesWithLesionsTest {

    private final PatientRecord testPatient = patient(true, false, false, false, false, true, List.of("Prostate", "Subcutaneous"), null);

    @Test
    public void shouldPassWhenNumberOfCategorizedLesionsEqualThresholdAndNoOtherLesionsArePresent() {
        assertEvaluation(EvaluationResult.PASS,
                new HasMinimumSitesWithLesions(6).evaluate(patientWithConsistentLesionFlags(true, Collections.emptyList(), null)));
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
    public void shouldNotCountAdditionalLesionDetailsOrBiopsyLocationContainingLymphWhenLymphNodeLesionsPresent() {
        PatientRecord patient = TumorTestFactory.withTumorDetails(ImmutableTumorDetails.copyOf(testPatient.clinical().tumor())
                .withOtherLesions("lymph node")
                .withBiopsyLocation("lymph"));
        assertEvaluation(EvaluationResult.FAIL, new HasMinimumSitesWithLesions(6).evaluate(patient));
    }

    @Test
    public void shouldNotCountNullBooleanFieldsOrEmptyOtherLesionsAsSites() {
        PatientRecord patient = patientWithConsistentLesionFlags(null, Collections.emptyList(), null);
        assertEvaluation(EvaluationResult.UNDETERMINED, new HasMinimumSitesWithLesions(1).evaluate(patient));
        assertEvaluation(EvaluationResult.FAIL, new HasMinimumSitesWithLesions(2).evaluate(patient));
    }

    @Test
    public void shouldCountBiopsyLocationTowardsUpperLimitOfLesionSiteCount() {
        PatientRecord patient = patientWithConsistentLesionFlags(null, Collections.emptyList(), "Kidney");
        assertEvaluation(EvaluationResult.UNDETERMINED, new HasMinimumSitesWithLesions(2).evaluate(patient));
        assertEvaluation(EvaluationResult.FAIL, new HasMinimumSitesWithLesions(3).evaluate(patient));
    }

    private static PatientRecord patientWithConsistentLesionFlags(Boolean lesionFlag, List<String> otherLesions, String biopsyLocation) {
        return patient(lesionFlag, lesionFlag, lesionFlag, lesionFlag, lesionFlag, lesionFlag, otherLesions, biopsyLocation);
    }

    private static PatientRecord patient(Boolean hasBoneLesions, Boolean hasBrainLesions, Boolean hasCnsLesions, Boolean hasLiverLesions,
            Boolean hasLungLesions, Boolean hasLymphNodeLesions, List<String> otherLesions, String biopsyLocation) {
        return TumorTestFactory.withTumorDetails(TumorTestFactory.builder()
                .hasBoneLesions(hasBoneLesions)
                .hasBrainLesions(hasBrainLesions)
                .hasCnsLesions(hasCnsLesions)
                .hasLiverLesions(hasLiverLesions)
                .hasLungLesions(hasLungLesions)
                .hasLymphNodeLesions(hasLymphNodeLesions)
                .otherLesions(otherLesions)
                .biopsyLocation(biopsyLocation)
                .build());
    }
}