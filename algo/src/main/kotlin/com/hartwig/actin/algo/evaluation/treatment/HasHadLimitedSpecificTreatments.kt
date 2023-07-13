package com.hartwig.actin.algo.evaluation.treatment

import com.google.common.collect.Lists
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import java.util.*

class HasHadLimitedSpecificTreatments(
    private val names: Set<String>, private val warnCategory: TreatmentCategory,
    private val maxTreatmentLines: Int
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val matchTreatments: MutableList<String> = Lists.newArrayList()
        val warnTreatments: MutableList<String> = Lists.newArrayList()
        for (treatment in record.clinical().priorTumorTreatments()) {
            val isWarnCategory = treatment.categories().contains(warnCategory)
            val isTrial = treatment.categories().contains(TreatmentCategory.TRIAL)
            if (isWarnCategory || isTrial) {
                warnTreatments.add(treatment.name())
            }
            for (name in names) {
                if (treatment.name().lowercase(Locale.getDefault()).contains(name.lowercase(Locale.getDefault()))) {
                    matchTreatments.add(treatment.name())
                }
            }
        }
        return when {
            matchTreatments.size > maxTreatmentLines -> {
                EvaluationFactory.fail("${matchTreatments.size} treatments (${concat(matchTreatments)})  exceeding the max nr of $maxTreatmentLines lines")
            }

            warnTreatments.size > maxTreatmentLines -> {
                EvaluationFactory.undetermined("Patient has received ${warnCategory.display()} or trial treatment ${warnTreatments.size} times")
            }

            else -> {
                EvaluationFactory.pass("Received " + concat(names) + " less than " + maxTreatmentLines + " times")
            }
        }
    }
}