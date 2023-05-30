package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.clinical.datamodel.TreatmentCategory
import com.hartwig.actin.util.ApplicationConfig

class HasHadAdjuvantTreatmentWithCategory(val category: TreatmentCategory) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val matchingTreatmentNames =
            record.clinical().priorTumorTreatments().filter { it.categories().contains(category) }.map { it.name() }
                .filter { it.lowercase(ApplicationConfig.LOCALE).replace("neoadjuvant", "").contains("adjuvant") }

        return when {
            matchingTreatmentNames.isNotEmpty() -> EvaluationFactory.pass(
                "Patient has received adjuvant treatment(s) of ${category.display()}: ${Format.concat(matchingTreatmentNames)}",
                "Has received adjuvant treatment(s) of ${category.display()}: ${Format.concat(matchingTreatmentNames)}"
            )

            else -> EvaluationFactory.fail(
                "Patient has not received adjuvant treatment(s) of ${category.display()}",
                "Has not received adjuvant treatment(s) of ${category.display()}"
            )

        }
    }
}