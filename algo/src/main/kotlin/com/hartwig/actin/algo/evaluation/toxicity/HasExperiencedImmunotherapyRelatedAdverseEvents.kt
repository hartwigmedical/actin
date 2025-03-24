package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.StopReason
import com.hartwig.actin.icd.IcdModel

class HasExperiencedImmunotherapyRelatedAdverseEvents(private val icdModel: IcdModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val immunotherapyTreatmentList = record.oncologicalHistory.filter { it.categories().contains(TreatmentCategory.IMMUNOTHERAPY) }
        val immunotherapyTreatmentsByStopReason = immunotherapyTreatmentList.groupBy { it.treatmentHistoryDetails?.stopReason }
        val stopReasonUnknown = immunotherapyTreatmentsByStopReason.keys == setOf(null)
        val hasHadImmunotherapyWithStopReasonToxicity = StopReason.TOXICITY in immunotherapyTreatmentsByStopReason

        val matchingComorbidities = icdModel.findInstancesMatchingAnyIcdCode(
            record.comorbidities,
            IcdConstants.DRUG_ALLERGY_SET.flatMap { icdCode ->
                IcdConstants.IMMUNOTHERAPY_DRUG_SET.map { extension -> IcdCode(icdCode, extension) }
            }.toSet()
        )

        val potentialImmunotherapyAdverseEvents = matchingComorbidities.fullMatches +
                icdModel.findInstancesMatchingAnyExtensionCode(record.intolerances + record.toxicities, IcdConstants.IMMUNOTHERAPY_DRUG_SET)
        val unknownDrugIntolerances = matchingComorbidities.mainCodeMatchesWithUnknownExtension

        val warnMessageStart = "Possible immunotherapy related adverse events in history"

        return when {
            immunotherapyTreatmentList.isNotEmpty() && potentialImmunotherapyAdverseEvents.isNotEmpty() -> {
                val allergyString = potentialImmunotherapyAdverseEvents.joinToString(", ", prefix = " (", postfix = ")") { it.display() }
                EvaluationFactory.warn(warnMessageStart + allergyString)
            }

            hasHadImmunotherapyWithStopReasonToxicity -> {
                EvaluationFactory.warn("$warnMessageStart (prior immunotherapy with stop reason toxicity)")
            }

            (immunotherapyTreatmentList.isNotEmpty() && stopReasonUnknown) -> {
                EvaluationFactory.recoverableUndetermined("Prior immunotherapy related adverse events undetermined")
            }

            immunotherapyTreatmentList.isNotEmpty() && unknownDrugIntolerances.isNotEmpty() -> {
                val allergyString = unknownDrugIntolerances.joinToString(", ", prefix = " (", postfix = ")") { it.display() }
                EvaluationFactory.recoverableUndetermined(
                    "Drug allergy$allergyString in history but undetermined if immunotherapy-related AE (drug type unknown)"
                )
            }

            else -> {
                EvaluationFactory.fail("No experience of immunotherapy related adverse events")
            }
        }
    }
}