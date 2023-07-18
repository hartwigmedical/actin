package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.treatment.ProgressiveDiseaseFunctions.treatmentResultedInPDOption
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory

class HasHadPDFollowingSpecificTreatment(private val treatments: List<Treatment>) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {

        val treatmentNamesToMatch = treatments.map { it.name() }.toSet()

        val (treatmentsWithPD, treatmentsWithExactType, hasHadTreatmentWithUnclearPDStatus, hasHadTreatmentWithWarnType) =
            evaluateTreatmentHistory(record, treatmentNamesToMatch)

        return if (treatmentsWithPD.isNotEmpty()) {
            EvaluationFactory.pass(
                "Has received " + Format.concat(treatmentsWithPD.map(Treatment::name)) + " treatment with PD"
            )
        } else if (hasHadTreatmentWithWarnType) {
            EvaluationFactory.undetermined("Undetermined if received specific " + Format.concat(treatmentNamesToMatch) + " treatment")
        } else if (hasHadTreatmentWithUnclearPDStatus) {
            EvaluationFactory.undetermined(
                "Has received " + Format.concat(treatmentsWithExactType.map(Treatment::name)) + " treatment but undetermined if PD"
            )
        } else if (treatmentsWithExactType.isNotEmpty()) {
            EvaluationFactory.fail("Has received " + Format.concat(treatmentsWithExactType.map(Treatment::name)) + " treatment, but no PD")
        } else {
            EvaluationFactory.fail("Has not received specific " + Format.concat(treatmentNamesToMatch) + " treatment")
        }
    }

    private fun evaluateTreatmentHistory(record: PatientRecord, treatmentNamesToMatch: Set<String>): TreatmentHistoryEvaluation {
        val treatmentHistory = record.clinical().treatmentHistory()

        return treatmentHistory.map { entry ->
            val categories = entry.treatments().flatMap(Treatment::categories)
            val isWarnType = categories.contains(TreatmentCategory.TRIAL)
            val isPD = treatmentResultedInPDOption(entry)
            if (treatmentsMatchNameListExactly(entry.treatments(), treatmentNamesToMatch)) {
                TreatmentHistoryEvaluation(
                    matchesWithPD = if (isPD == true) entry.treatments() else emptySet(),
                    typeMatches = entry.treatments(),
                    matchesWithUnclearPD = isPD == null,
                    hasWarnType = isWarnType
                )
            } else {
                TreatmentHistoryEvaluation(hasWarnType = isWarnType)
            }
        }.fold(TreatmentHistoryEvaluation()) { acc, result ->
            TreatmentHistoryEvaluation(
                matchesWithPD = acc.matchesWithPD + result.matchesWithPD,
                typeMatches = acc.typeMatches + result.typeMatches,
                matchesWithUnclearPD = acc.matchesWithUnclearPD || result.matchesWithUnclearPD,
                hasWarnType = acc.hasWarnType || result.hasWarnType
            )
        }
    }

    companion object {
        private fun treatmentsMatchNameListExactly(treatments: Set<Treatment>, treatmentNamesToMatch: Set<String>): Boolean {
            return treatments.map(Treatment::name).intersect(treatmentNamesToMatch).isNotEmpty()
        }
    }

    private data class TreatmentHistoryEvaluation(
        val matchesWithPD: Set<Treatment> = emptySet(),
        val typeMatches: Set<Treatment> = emptySet(),
        val matchesWithUnclearPD: Boolean = false,
        val hasWarnType: Boolean = false
    )
}