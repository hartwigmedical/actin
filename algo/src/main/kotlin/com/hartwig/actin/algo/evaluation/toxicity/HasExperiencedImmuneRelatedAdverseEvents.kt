package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.intolerance.IntoleranceFunctions
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.StopReason
import com.hartwig.actin.icd.IcdModel

class HasExperiencedImmuneRelatedAdverseEvents(private val icdModel: IcdModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val immunotherapyTreatmentList = record.oncologicalHistory.filter { it.categories().contains(TreatmentCategory.IMMUNOTHERAPY) }
        val immunotherapyTreatmentsByStopReason = immunotherapyTreatmentList.groupBy { it.treatmentHistoryDetails?.stopReason }
        val stopReasonUnknown = immunotherapyTreatmentsByStopReason.keys == setOf(null)
        val hasHadImmunotherapyWithStopReasonToxicity = StopReason.TOXICITY in immunotherapyTreatmentsByStopReason

        val intoleranceIcdCheck = IntoleranceFunctions.findIntoleranceMatchingAnyIcdCode(
            icdModel,
            record,
            IcdConstants.DRUG_ALLERGY_SET.flatMap { icdCode ->
                IcdConstants.IMMUNOTHERAPY_DRUG_SET.map { extension -> IcdCode(icdCode, extension) }
            }.toSet()
        )

        val immunotherapyAllergies = intoleranceIcdCheck.fullMatches
        val undeterminedDrugAllergies = intoleranceIcdCheck.mainCodeMatchesWithUnknownExtension

        val warnMessageStart =
            "Possible immunotherapy related adverse events in history"

        return when {
            immunotherapyTreatmentList.isNotEmpty() && immunotherapyAllergies.isNotEmpty() -> {
                val allergyString = immunotherapyAllergies.joinToString(", ", prefix = " (", postfix = ")") { it.name }
                EvaluationFactory.warn(warnMessageStart + allergyString)
            }

            hasHadImmunotherapyWithStopReasonToxicity -> {
                EvaluationFactory.warn("$warnMessageStart (prior immunotherapy with stop reason toxicity)")
            }

            (immunotherapyTreatmentList.isNotEmpty() && stopReasonUnknown) -> {
                EvaluationFactory.recoverableUndetermined(
                    "Undetermined prior immunotherapy related adverse events",
                    "Undetermined prior immunotherapy related adverse events"
                )
            }

            immunotherapyTreatmentList.isNotEmpty() && undeterminedDrugAllergies.isNotEmpty() -> {
                val allergyString = undeterminedDrugAllergies.joinToString(", ", prefix = " (", postfix = ")") { it.name }
                EvaluationFactory.recoverableUndetermined(
                    "Drug allergy$allergyString in history but undetermined if immunotherapy-related AE (drug type unknown)"
                )
            }

            else -> {
                EvaluationFactory.fail(
                    "Patient has not experienced immunotherapy related adverse events",
                    "No experience of immunotherapy related adverse events"
                )
            }
        }
    }
}