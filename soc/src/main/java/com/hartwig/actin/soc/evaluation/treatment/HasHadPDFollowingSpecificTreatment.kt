package com.hartwig.actin.soc.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.clinical.datamodel.TreatmentCategory
import com.hartwig.actin.soc.evaluation.EvaluationFactory
import com.hartwig.actin.soc.evaluation.EvaluationFunction
import com.hartwig.actin.soc.evaluation.treatment.ProgressiveDiseaseFunctions.treatmentResultedInPDOption
import com.hartwig.actin.soc.evaluation.util.Format
import com.hartwig.actin.util.ApplicationConfig

private const val UNDETERMINED_GENERAL_MESSAGE = "Undetermined treatment or PD"
private const val FAIL_GENERAL_MESSAGE = "No treatment with PD"

class HasHadPDFollowingSpecificTreatment internal constructor(private val names: Set<String>, warnCategory: TreatmentCategory?) : EvaluationFunction {

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
            EvaluationFactory.pass("Patient has received specific " + Format.concat(treatmentsWithPD) + " treatment with PD",
                    "Treatment with PD")
        } else if (hasHadTreatmentWithWarnType) {
            EvaluationFactory.undetermined("Undetermined whether patient has received specific " + Format.concat(names) + "treatment",
                    UNDETERMINED_GENERAL_MESSAGE)
        } else if (hasHadTreatmentWithUnclearPDStatus) {
            EvaluationFactory.undetermined(
                    "Patient has received " + Format.concat(treatmentsWithExactType) + " treatment but undetermined if PD occurred",
                    UNDETERMINED_GENERAL_MESSAGE)
        } else if (treatmentsWithExactType.isNotEmpty()) {
            EvaluationFactory.fail("Patient has received " + Format.concat(treatmentsWithExactType) + " treatment, but no PD",
                    FAIL_GENERAL_MESSAGE)
        } else {
            EvaluationFactory.fail("Patient has not received specific " + Format.concat(names) + "treatment", FAIL_GENERAL_MESSAGE)
        }
    }
}