package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.treatment.MedicationFunctions.createTreatmentHistoryEntriesFromMedications
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment

class HasHadTreatmentWithDrugAndDoseReduction(private val drug: Drug) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {

        val hasHadDrug = (record.oncologicalHistory + createTreatmentHistoryEntriesFromMedications(record.medications))
            .any {
                entry -> entry.treatments.any {
                    treatment -> (treatment as? DrugTreatment)?.drugs?.any { it.name.equals(drug.name, ignoreCase = true) } == true
                }
            }

        return when {
            hasHadDrug -> EvaluationFactory.undetermined("Undetermined if patient may have had a dose reduction during $drug treatment")
            else -> EvaluationFactory.fail("Patient did not dose reduction receive $drug during treatment")
        }
    }
}
