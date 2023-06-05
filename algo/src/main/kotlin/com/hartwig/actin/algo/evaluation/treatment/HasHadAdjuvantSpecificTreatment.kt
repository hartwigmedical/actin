package com.hartwig.actin.algo.evaluation.treatment

import com.google.common.collect.Lists
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.util.ApplicationConfig
import java.util.Locale

class HasHadAdjuvantSpecificTreatment internal constructor(private val names: Set<String>, private val warnCategory: TreatmentCategory) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val adjuvantTreatments = record.clinical().priorTumorTreatments()
            .filter { it.name().lowercase(ApplicationConfig.LOCALE).replace("neoadjuvant", "").contains("adjuvant") }

        val matchTreatments: MutableList<String> = Lists.newArrayList()
        val warnTreatments: MutableList<String> = Lists.newArrayList()

        for (treatment in adjuvantTreatments) {
            val isTrial = treatment.categories().contains(TreatmentCategory.TRIAL)
            if (treatment.categories().contains(warnCategory) || isTrial) {
                warnTreatments.add(treatment.name())
            }

            for (name in names) {
                if (treatment.name().lowercase(Locale.getDefault()).contains(name.lowercase(Locale.getDefault()))) {
                    matchTreatments.add(treatment.name())
                }
            }
        }

        return if (matchTreatments.isNotEmpty()) {
            val treatmentsString = Format.concatLowercaseWithAnd(matchTreatments)
            EvaluationFactory.pass("Patient has received adjuvant $treatmentsString", "Received adjuvant $treatmentsString")
        } else if (warnTreatments.isNotEmpty()) {
            val categoryString = warnCategory.display()
            EvaluationFactory.warn(
                "Patient has received adjuvant $categoryString but not of specific type",
                "Received adjuvant $categoryString but not of specific type)"
            )
        } else {
            val namesString = Format.concatLowercaseWithAnd(names)
            EvaluationFactory.fail(
                "Patient has not received adjuvant treatment with name $namesString",
                "Not received adjuvant $namesString"
            )
        }
    }
}