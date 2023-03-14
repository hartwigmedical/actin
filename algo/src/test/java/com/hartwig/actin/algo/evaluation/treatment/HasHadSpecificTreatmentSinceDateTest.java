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
    private final HasHadSpecificTreatmentSinceDate function = new HasHadSpecificTreatmentSinceDate(TREATMENT_QUERY,
            LocalDate.now().minusYears(1));

    private final PriorTumorTreatment nonMatchingTreatment = TreatmentTestFactory.builder()
            .name("other")
            .addCategories(TreatmentCategory.RADIOTHERAPY)
            .startYear(LocalDate.now().getYear())
            .startMonth(LocalDate.now().getMonthValue())
            .build();

    @Test
    public void shouldFailWhenTreatmentNotFound() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(List.of(nonMatchingTreatment))));
    }

    @Test
    public void shouldFailWhenMatchingTreatmentIsOlder() {
        List<PriorTumorTreatment> priorTumorTreatments = List.of(nonMatchingTreatment,
                matchingTreatment(LocalDate.now().minusYears(3).getYear(), null));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments)));
    }

    @Test
    public void shouldPassWhenMatchingTreatmentWithinRange() {
        List<PriorTumorTreatment> priorTumorTreatments = List.of(nonMatchingTreatment,
                matchingTreatment(LocalDate.now().getYear(), LocalDate.now().minusMonths(4).getMonthValue()));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments)));
    }

    @Test
    public void shouldWarnWhenMatchingTreatmentHasUnknownDate() {
        List<PriorTumorTreatment> priorTumorTreatments = List.of(nonMatchingTreatment, matchingTreatment(null, null));
        assertEvaluation(EvaluationResult.WARN, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments)));
    }

    private PriorTumorTreatment matchingTreatment(Integer stopYear, Integer stopMonth) {
        return TreatmentTestFactory.builder()
                .name("specific " + TREATMENT_QUERY)
                .addCategories(TreatmentCategory.CHEMOTHERAPY)
                .stopYear(stopYear)
                .stopMonth(stopMonth)
                .build();
    }
}