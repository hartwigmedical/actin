package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.treatment.ProgressiveDiseaseFunctions.treatmentResultedInPDOption
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory

class HasHadPDFollowingSpecificTreatment(
    private val treatments: List<Treatment>?, private val names: Set<String>?,
    private val warnCategory: TreatmentCategory?
) : EvaluationFunction {

    init {
        if ((treatments == null && names == null) || (treatments != null && names != null)) {
            throw IllegalStateException("Treatments or names must be provided, but not both")
        }
    }

    override fun evaluate(record: PatientRecord): Evaluation {

        val treatmentNamesToMatch = when {
            treatments != null -> treatments.map { it.name() }.toSet()

            names != null -> names.map(String::lowercase).toSet()

            else -> emptySet()
        }

        val matchCondition = if (treatments != null) ::treatmentsMatchNameListExactly else ::anyTreatmentOrSynonymContainsANameFromList

        val (treatmentsWithPD, treatmentsWithExactType, hasHadTreatmentWithUnclearPDStatus, hasHadTreatmentWithWarnType) =
            evaluateTreatmentHistory(record, treatmentNamesToMatch, matchCondition)

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

    private fun evaluateTreatmentHistory(
        record: PatientRecord, treatmentNamesToMatch: Set<String>,
        matchCondition: (Set<Treatment>, Set<String>) -> Boolean
    ): TreatmentHistoryEvaluation {
        val treatmentHistory = record.clinical().treatmentHistory()

        return treatmentHistory.map { entry ->
            val categories = entry.treatments().flatMap(Treatment::categories)
            val isWarnType = (warnCategory != null && categories.contains(warnCategory)) || categories.contains(TreatmentCategory.TRIAL)
            val isPD = treatmentResultedInPDOption(entry)
            if (matchCondition(entry.treatments(), treatmentNamesToMatch)) {
                TreatmentHistoryEvaluation(
                    matchesWithPD = if (isPD == true) entry.treatments() else emptySet(),
                    typeMatches = entry.treatments(),
                    matchesWithUnclearPD = isPD == null,
                    hasWarnType = isWarnType
                )
            } else {
                TreatmentHistoryEvaluation(emptySet(), emptySet(), false, isWarnType)
            }
        }.fold(TreatmentHistoryEvaluation(emptySet(), emptySet(), matchesWithUnclearPD = false, hasWarnType = false)) { acc, result ->
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

        private fun anyTreatmentOrSynonymContainsANameFromList(treatments: Set<Treatment>, treatmentNamesToMatch: Set<String>): Boolean {
            return treatments.flatMap { it.synonyms() + it.name() }.any { treatmentEntryName ->
                treatmentNamesToMatch.any { treatmentEntryName.lowercase().contains(it) }
            }
        }
    }

    private data class TreatmentHistoryEvaluation(
        val matchesWithPD: Set<Treatment>,
        val typeMatches: Set<Treatment>,
        val matchesWithUnclearPD: Boolean,
        val hasWarnType: Boolean
    )
}