package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.util.ApplicationConfig

class HasHadAdjuvantTreatmentWithCategory(private val category: TreatmentCategory) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val adjuvantTreatmentsMatchingCategory = record.clinical().priorTumorTreatments().filter { it.categories().contains(category) }
            .filter { it.name().lowercase(ApplicationConfig.LOCALE).replace("neoadjuvant", "").contains("adjuvant") }

        return if (adjuvantTreatmentsMatchingCategory.isNotEmpty()) {
            EvaluationFactory.pass(
                "Patient has received adjuvant treatment(s) of category ${category.display()}",
                "Has received adjuvant treatment(s) of ${category.display()}"
            )
        } else {
            EvaluationFactory.fail(
                "Patient has not received adjuvant treatment(s) of ${category.display()}",
                "Has not received adjuvant treatment(s) of ${category.display()}"
            )
        }
    }
}