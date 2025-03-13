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
        val matchingMedications = record.medications?.filter {
            (selector.isActive(it) || selector.isPlanned(it)) &&
                    it.drug?.category in setOf(TreatmentCategory.CHEMOTHERAPY, TreatmentCategory.RADIOTHERAPY) &&
                    it.drug?.drugTypes?.contains(type) == true  &&
                    it.dosage.frequency!= null && it.dosage.frequency!! > minCycles
        }

        val matchingTreatments = record.oncologicalHistory.filter {
            it.categories().intersect(setOf(TreatmentCategory.CHEMOTHERAPY, TreatmentCategory.RADIOTHERAPY)).isNotEmpty() &&
                    it.isOfType(type) == true &&
                    it.treatmentHistoryDetails?.cycles != null &&
                    it.treatmentHistoryDetails?.cycles!! > minCycles
        }

        return when {
            matchingTreatments.isNotEmpty() -> EvaluationFactory.pass("Patient is currently getting chemoradiotherapy with $type chemotherapy and at least $minCycles cycles")
            matchingTreatments.isEmpty() -> EvaluationFactory.fail("No chemo radio therapy with $type with at least $minCycles is currently received")
            else -> EvaluationFactory.undetermined(
                "Undetermined if patient is currently getting chemoradiotherapy with $type chemotherapy and at least $minCycles cycles"
            )
        }
    }
}