package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent

class HasHadDefinitiveLocoregionalTherapyWithCurativeIntent: EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentHistory = record.oncologicalHistory
        if (treatmentHistory.isEmpty()) return EvaluationFactory.warn("Patient has no treatment history (we do not always receive the original surgery)")

        var patientHasHadLocoRegionalTherapy = false
        var treatmentHistoryEntryCurativeIntentIsNull = false

        for(treatmentHistoryEntry in treatmentHistory){
            for(category in treatmentHistoryEntry.categories()){
                if(category == TreatmentCategory.RADIOTHERAPY || category ==  TreatmentCategory.SURGERY){
                    patientHasHadLocoRegionalTherapy = true
                    val intents = treatmentHistoryEntry.intents
                    if (intents.isNullOrEmpty()) treatmentHistoryEntryCurativeIntentIsNull = true
                    else {
                        for(intent in intents){
                            if (intent == Intent.CURATIVE) return EvaluationFactory.pass("Patient has received locoregional therapy with curative intent")
                        }
                    }
                }
            }
        }

        return when{
            !patientHasHadLocoRegionalTherapy -> EvaluationFactory.fail("Patient has not had locoregional surgery")
            treatmentHistoryEntryCurativeIntentIsNull-> EvaluationFactory.undetermined("Patient has received locoregional therapy with unknown intent")
            else -> EvaluationFactory.fail("Patient has received locoregional therapy without curative intent")
        }
    }
}