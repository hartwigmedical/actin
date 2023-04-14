package com.hartwig.actin.soc.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.treatment.ProgressiveDiseaseFunctions.treatmentResultedInPDOption
import java.util.*

class HasHadPDFollowingSpecificTreatment internal constructor(private val names: Set<String>, warnCategory: TreatmentCategory?) : EvaluationFunction {
    private val warnCategory: TreatmentCategory?

    init {
        this.warnCategory = warnCategory
    }

    fun evaluate(record: PatientRecord): Evaluation {
        val treatmentsWithPD: MutableSet<String> = Sets.newHashSet()
        val treatmentsWithExactType: MutableSet<String> = Sets.newHashSet()
        var hasHadTreatmentWithUnclearPDStatus = false
        var hasHadTreatmentWithWarnType = false
        for (treatment in record.clinical().priorTumorTreatments()) {
            val isWarnCategory = warnCategory != null && treatment.categories().contains(warnCategory)
            val isTrial = treatment.categories().contains(TreatmentCategory.TRIAL)
            if (isWarnCategory || isTrial) {
                hasHadTreatmentWithWarnType = true
            }
            for (name in names) {
                if (treatment.name().lowercase(Locale.getDefault()).contains(name.lowercase(Locale.getDefault()))) {
                    treatmentsWithExactType.add(treatment.name())
                    val treatmentResultedInPDOption: Optional<Boolean> = treatmentResultedInPDOption(treatment)
                    if (treatmentResultedInPDOption.orElse(false)) {
                        treatmentsWithPD.add(treatment.name())
                    } else if (treatmentResultedInPDOption.isEmpty) {
                        hasHadTreatmentWithUnclearPDStatus = true
                    }
                }
            }
        }
        val result: EvaluationResult
        result = if (!treatmentsWithPD.isEmpty()) {
            EvaluationResult.PASS
        } else if (hasHadTreatmentWithUnclearPDStatus || hasHadTreatmentWithWarnType) {
            EvaluationResult.UNDETERMINED
        } else {
            EvaluationResult.FAIL
        }
        val builder: ImmutableEvaluation.Builder = EvaluationFactory.unrecoverable().result(result)
        if (result == EvaluationResult.FAIL) {
            if (!treatmentsWithExactType.isEmpty()) {
                builder.addFailSpecificMessages("Patient has received " + Format.concat(treatmentsWithExactType) + " treatment, but no PD")
            } else {
                builder.addFailSpecificMessages("Patient has not received specific " + Format.concat(names) + "treatment")
            }
            builder.addFailGeneralMessages("No treatment with PD")
        } else if (result == EvaluationResult.UNDETERMINED) {
            if (hasHadTreatmentWithWarnType) {
                builder.addUndeterminedSpecificMessages(
                        "Undetermined whether patient has received specific " + Format.concat(names) + "treatment")
            } else {
                builder.addUndeterminedSpecificMessages(
                        "Patient has received " + Format.concat(treatmentsWithExactType) + " treatment but undetermined if PD occurred")
            }
            builder.addUndeterminedGeneralMessages("Undetermined treatment or PD")
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Patient has received specific " + Format.concat(treatmentsWithPD) + " treatment with PD")
            builder.addPassGeneralMessages("Treatment with PD")
        }
        return builder.build()
    }
}