package com.hartwig.actin.algo.evaluation.treatment;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.time.LocalDate;
import java.util.List;

import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.junit.Test;

public class HasHadSpecificTreatmentSinceDateTest {

    private static final String TREATMENT_QUERY = "treatment";
    private static final int YEARS_TO_SUBTRACT = 3;

    private final LocalDate targetDate = LocalDate.now().minusYears(1);
    private final LocalDate recentDate = LocalDate.now().minusMonths(4);
    private final HasHadSpecificTreatmentSinceDate function = new HasHadSpecificTreatmentSinceDate(TREATMENT_QUERY, targetDate);

    private final PriorTumorTreatment nonMatchingTreatment = TreatmentTestFactory.builder()
            .name("other")
            .addCategories(TreatmentCategory.RADIOTHERAPY)
            .startYear(LocalDate.now().getYear())
            .startMonth(LocalDate.now().getMonthValue())
            .build();

    private final PriorTumorTreatment olderTreatment = matchingTreatment(LocalDate.now().minusYears(YEARS_TO_SUBTRACT).getYear(), null);

    @Test
    public void shouldFailWhenTreatmentNotFound() {
        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(List.of(nonMatchingTreatment))));
    }

    @Test
    public void shouldFailWhenMatchingTreatmentIsOlderByYear() {
        List<PriorTumorTreatment> priorTumorTreatments = List.of(nonMatchingTreatment, olderTreatment);
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments)));
    }

    @Test
    public void shouldFailWhenMatchingTreatmentIsOlderByMonth() {
        LocalDate olderDate = targetDate.minusMonths(1);
        List<PriorTumorTreatment> priorTumorTreatments =
                List.of(nonMatchingTreatment, matchingTreatment(olderDate.getYear(), olderDate.getMonthValue()));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments)));
    }

    @Test
    public void shouldPassWhenPriorTreatmentsIncludeMatchingTreatmentWithinRange() {
        List<PriorTumorTreatment> priorTumorTreatments =
                List.of(nonMatchingTreatment, olderTreatment, matchingTreatment(recentDate.getYear(), recentDate.getMonthValue()));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments)));
    }

    @Test
    public void shouldReturnUndeterminedWhenMatchingTreatmentHasUnknownYear() {
        List<PriorTumorTreatment> priorTumorTreatments = List.of(nonMatchingTreatment, matchingTreatment(null, 10));
        assertEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments)));
    }

    @Test
    public void shouldReturnUndeterminedWhenMatchingTreatmentMatchesYearWithUnknownMonth() {
        List<PriorTumorTreatment> priorTumorTreatments = List.of(nonMatchingTreatment, matchingTreatment(targetDate.getYear(), null));
        assertEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments)));
    }

    @Test
    public void shouldFailWhenPriorTreatmentHasUnknownStopDateButOlderStartDate() {
        LocalDate olderDate = LocalDate.now().minusYears(YEARS_TO_SUBTRACT);
        List<PriorTumorTreatment> priorTumorTreatments = List.of(nonMatchingTreatment,
                olderTreatment,
                matchingTreatment(null, null, olderDate.getYear(), olderDate.getMonthValue()));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments)));
    }

    @Test
    public void shouldPassWhenPriorTreatmentHasUnknownStopDateButStartDateInRange() {
        List<PriorTumorTreatment> priorTumorTreatments = List.of(nonMatchingTreatment,
                matchingTreatment(LocalDate.now().minusYears(YEARS_TO_SUBTRACT).getYear(), null),
                matchingTreatment(LocalDate.now().getYear(), null, recentDate.getYear(), recentDate.getMonthValue()));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments)));
    }

    private PriorTumorTreatment matchingTreatment(Integer stopYear, Integer stopMonth) {
        return matchingTreatment(stopYear, stopMonth, null, null);
    }

    private PriorTumorTreatment matchingTreatment(Integer stopYear, Integer stopMonth, Integer startYear, Integer startMonth) {
        return TreatmentTestFactory.builder()
                .name("specific " + TREATMENT_QUERY)
                .addCategories(TreatmentCategory.CHEMOTHERAPY)
                .stopYear(stopYear)
                .stopMonth(stopMonth)
                .startYear(startYear)
                .startMonth(startMonth)
                .build();
    }
}