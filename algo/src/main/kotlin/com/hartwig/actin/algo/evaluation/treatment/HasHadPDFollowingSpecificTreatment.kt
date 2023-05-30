package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.clinical.datamodel.TreatmentCategory
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.treatment.ProgressiveDiseaseFunctions.treatmentResultedInPDOption
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.util.ApplicationConfig

class HasHadPDFollowingSpecificTreatment internal constructor(private val names: Set<String>, warnCategory: TreatmentCategory?) :
    EvaluationFunction {

    private val warnCategory: TreatmentCategory?

    init {
        this.warnCategory = warnCategory
    }

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentsWithPD: MutableSet<String> = mutableSetOf()
        val treatmentsWithExactType: MutableSet<String> = mutableSetOf()
        var hasHadTreatmentWithUnclearPDStatus = false
        var hasHadTreatmentWithWarnType = false
        for (treatment in record.clinical().priorTumorTreatments()) {
            val isWarnCategory = warnCategory != null && treatment.categories().contains(warnCategory)
            val isTrial = treatment.categories().contains(TreatmentCategory.TRIAL)
            if (isWarnCategory || isTrial) {
                hasHadTreatmentWithWarnType = true
            }
            for (name in names) {
                if (treatment.name().lowercase(ApplicationConfig.LOCALE).contains(name.lowercase(ApplicationConfig.LOCALE))) {
                    treatmentsWithExactType.add(treatment.name())
                    when (treatmentResultedInPDOption(treatment)) {
                        true -> treatmentsWithPD.add(treatment.name())
                        null -> hasHadTreatmentWithUnclearPDStatus = true
                        else -> {}
                    }
                }
            }
        }
        return if (treatmentsWithPD.isNotEmpty()) {
            EvaluationFactory.pass(
                "Patient has received specific " + Format.concat(treatmentsWithPD) + " treatment with PD",
                "Has received " + Format.concat(treatmentsWithPD) + " treatment with PD"
            )
        } else if (hasHadTreatmentWithWarnType) {
            EvaluationFactory.undetermined(
                "Undetermined whether patient has received specific " + Format.concat(names) + "treatment",
                "Undetermined if received specific " + Format.concat(names) + " treatment"
            )
        } else if (hasHadTreatmentWithUnclearPDStatus) {
            EvaluationFactory.undetermined(
                "Patient has received " + Format.concat(treatmentsWithExactType) + " treatment but undetermined if PD occurred",
                "Has received " + Format.concat(treatmentsWithExactType) + " treatment but undetermined if PD"
            )
        } else if (treatmentsWithExactType.isNotEmpty()) {
            EvaluationFactory.fail(
                "Patient has received " + Format.concat(treatmentsWithExactType) + " treatment, but no PD",
                "Has received " + Format.concat(treatmentsWithExactType) + " treatment, but no PD"
            )
        } else {
            EvaluationFactory.fail(
                "Patient has not received specific " + Format.concat(names) + "treatment",
                "Has not received specific " + Format.concat(names) + " treatment"
            )
        }
    }
}