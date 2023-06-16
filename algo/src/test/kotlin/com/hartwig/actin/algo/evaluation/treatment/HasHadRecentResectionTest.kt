package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import org.junit.Test
import java.time.LocalDate

class HasHadRecentResectionTest {
    @Test
    fun canEvaluate() {
        val minDate = LocalDate.of(2022, 10, 12)
        val function = HasHadRecentResection(minDate)

        // FAIL without any prior tumor treatments.
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(emptyList())))

        // PASS on a recent resection
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                TreatmentTestFactory.withPriorTumorTreatment(
                    TreatmentTestFactory.builder()
                        .name("some of form " + HasHadRecentResection.RESECTION_KEYWORD)
                        .startYear(2023)
                        .build()
                )
            )
        )

        // WARN on a resection close to min date
        assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                TreatmentTestFactory.withPriorTumorTreatment(
                    TreatmentTestFactory.builder()
                        .name("some of form " + HasHadRecentResection.RESECTION_KEYWORD)
                        .startYear(2022)
                        .startMonth(10)
                        .build()
                )
            )
        )

        // UNDETERMINED in case date is unknown
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                TreatmentTestFactory.withPriorTumorTreatment(
                    TreatmentTestFactory.builder()
                        .name("some of form " + HasHadRecentResection.RESECTION_KEYWORD)
                        .build()
                )
            )
        )

        // UNDETERMINED in case resection is unsure
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                TreatmentTestFactory.withPriorTumorTreatment(
                    TreatmentTestFactory.builder()
                        .addCategories(TreatmentCategory.SURGERY)
                        .startYear(2022)
                        .startMonth(11)
                        .build()
                )
            )
        )
    }
}