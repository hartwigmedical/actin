package com.hartwig.actin.algo.evaluation.surgery;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.time.LocalDate;
import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.ImmutableSurgery;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.Surgery;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class HasHadAnySurgeryAfterSpecificDateTest {

    @Test
    public void canEvaluate() {
        LocalDate minDate = LocalDate.of(2020, 2, 20);
        HasHadAnySurgeryAfterSpecificDate function = new HasHadAnySurgeryAfterSpecificDate(minDate);

        // No surgeries
        List<Surgery> surgeries = Lists.newArrayList();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(SurgeryTestFactory.withSurgeries(surgeries)));

        // One surgery but too long ago
        surgeries.add(ImmutableSurgery.builder().endDate(minDate.minusWeeks(4)).build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(SurgeryTestFactory.withSurgeries(surgeries)));

        // One surgery within the requested time period.
        surgeries.add(ImmutableSurgery.builder().endDate(minDate.plusWeeks(2)).build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(SurgeryTestFactory.withSurgeries(surgeries)));

        // No prior tumor treatments
        List<PriorTumorTreatment> treatments = Lists.newArrayList();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(SurgeryTestFactory.withPriorTumorTreatments(treatments)));

        // A non-surgery prior treatment
        treatments.add(builder().build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(SurgeryTestFactory.withPriorTumorTreatments(treatments)));

        // A surgery that is too long ago.
        treatments.add(builder().addCategories(TreatmentCategory.SURGERY).startYear(minDate.minusYears(1).getYear()).build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(SurgeryTestFactory.withPriorTumorTreatments(treatments)));

        // A surgery with just the same year (and no month).
        treatments.add(builder().addCategories(TreatmentCategory.SURGERY).startYear(minDate.getYear()).build());
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(SurgeryTestFactory.withPriorTumorTreatments(treatments)));

        // A surgery prior treatment with no date
        treatments.add(builder().addCategories(TreatmentCategory.SURGERY).build());
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(SurgeryTestFactory.withPriorTumorTreatments(treatments)));

        // A surgery with a month just before min date.
        treatments.add(builder().addCategories(TreatmentCategory.SURGERY)
                .startYear(minDate.getYear())
                .startMonth(minDate.getMonthValue() - 1)
                .build());
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(SurgeryTestFactory.withPriorTumorTreatments(treatments)));

        // A surgery with a month just after min date.
        treatments.add(builder().addCategories(TreatmentCategory.SURGERY)
                .startYear(minDate.getYear())
                .startMonth(minDate.getMonthValue() + 1)
                .build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(SurgeryTestFactory.withPriorTumorTreatments(treatments)));
    }

    @NotNull
    private static ImmutablePriorTumorTreatment.Builder builder() {
        return ImmutablePriorTumorTreatment.builder().name(Strings.EMPTY).isSystemic(true);
    }
}