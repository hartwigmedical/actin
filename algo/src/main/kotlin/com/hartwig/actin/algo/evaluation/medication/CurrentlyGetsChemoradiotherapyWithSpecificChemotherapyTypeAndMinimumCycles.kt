package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType

class CurrentlyGetsChemoradiotherapyWithSpecificChemotherapyTypeAndMinimumCycles(
    private val selector: MedicationSelector,
    private val type: TreatmentType,
    private val minCycles: Int
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val currentMedications = record.medications?.filter {
            (selector.isActive(it) || selector.isPlanned(it)) &&
                    it.drug?.category in setOf(TreatmentCategory.CHEMOTHERAPY, TreatmentCategory.RADIOTHERAPY) &&
                    it.drug?.drugTypes?.contains(type) == true  &&
                    it.dosage.frequency!= null && it.dosage.frequency!! > minCycles
        }

        return when {
            !currentMedications.isNullOrEmpty() -> EvaluationFactory.pass("Patient is currently getting chemoradiotherapy with $type chemotherapy and at least $minCycles cycles")
            currentMedications.isNullOrEmpty() -> EvaluationFactory.fail("No medications currently received")
            else -> EvaluationFactory.undetermined(
                "Undetermined if patient is currently getting chemoradiotherapy with $type chemotherapy and at least $minCycles cycles"
            )
        }
    }
}