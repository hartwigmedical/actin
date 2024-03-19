package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.TreatmentDatabaseFactory
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.ValueComparison
import com.hartwig.actin.clinical.datamodel.Intolerance
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.StopReason

class HasExperiencedImmuneRelatedAdverseEvents internal constructor() : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val immunotherapyTreatmentList = record.clinical.oncologicalHistory.filter { it.categories().contains(TreatmentCategory.IMMUNOTHERAPY) }
        val hasHadImmuneTherapy = immunotherapyTreatmentList.isNotEmpty()
        val stopReasonUnknown = immunotherapyTreatmentList.all { it.treatmentHistoryDetails?.stopReason == null }
        val hasHadImmuneTherapyWithStopReasonToxicity = immunotherapyTreatmentList.any {
            it.treatmentHistoryDetails?.stopReason == StopReason.TOXICITY
        }
        val intolerances = record.clinical.intolerances.filter { it.doids.contains(DoidConstants.DRUG_ALLERGY_DOID) }.map(Intolerance::name)
        val hasImmuneTherapyAllergy = immunotherapyTreatmentList.any { it.treatmentName() in intolerances }

        //TODO: map intolerances from name to drug and check category of drug in database
        //TreatmentDatabase().findDrugByName() .findDrugByName(drugName) ?: throw IllegalStateException("Drug not found in DB: $drugName")

        //TODO: make "anticancer drug" doid -> each drug in intolerances-sheet with this doid has to be in drug database with at least category defined.

        return when {
            (hasHadImmuneTherapy && hasHadImmuneTherapyWithStopReasonToxicity && hasImmuneTherapyAllergy) -> {
                EvaluationFactory.warn(
                    "Patient may have experienced immunotherapy related adverse events",
                    "Probable prior immunotherapy related adverse events"
                )
            }

            (hasHadImmuneTherapy && stopReasonUnknown) -> {
                EvaluationFactory.recoverableUndetermined(
                    "Undetermined prior immunotherapy related adverse events",
                    "Undetermined prior immunotherapy related adverse events"
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