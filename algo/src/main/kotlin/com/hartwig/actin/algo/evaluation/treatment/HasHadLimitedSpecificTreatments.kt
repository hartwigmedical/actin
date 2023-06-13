package com.hartwig.actin.algo.evaluation.treatment

import com.google.common.collect.Lists
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory.unrecoverable
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import java.util.*

class HasHadLimitedSpecificTreatments internal constructor(
    private val names: Set<String>, private val warnCategory: TreatmentCategory?,
    private val maxTreatmentLines: Int
) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val matchTreatments: MutableList<String> = Lists.newArrayList()
        val warnTreatments: MutableList<String> = Lists.newArrayList()
        for (treatment in record.clinical().priorTumorTreatments()) {
            val isWarnCategory = warnCategory != null && treatment.categories().contains(warnCategory)
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
        return if (matchTreatments.size > maxTreatmentLines) {
            unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages(
                    "Patient has received " + matchTreatments.size + " treatments (" + concat(matchTreatments) + ") "
                            + " which is exceeding the max nr of " + maxTreatmentLines + " lines"
                )
                .addFailGeneralMessages(
                    matchTreatments.size.toString() + " treatments (" + concat(matchTreatments) + ") " + " exceeding the max nr of "
                            + maxTreatmentLines + " lines"
                )
                .build()
        } else if (warnTreatments.size > maxTreatmentLines) {
            val undeterminedMessage =
                if (warnCategory != null) ("Patient has received " + warnCategory.display() + " or trial treatment " + warnTreatments.size
                        + " times") else "Patient has received " + concat(warnTreatments) + " treatments including trials"
            unrecoverable()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages(undeterminedMessage)
                .addUndeterminedGeneralMessages(undeterminedMessage)
                .build()
        } else {
            unrecoverable()
                .result(EvaluationResult.PASS)
                .addPassSpecificMessages("Patient has received " + concat(names) + " less than " + maxTreatmentLines + " times")
                .addPassGeneralMessages("Received " + concat(names) + " less than " + maxTreatmentLines + " times")
                .build()
        }
    }
}