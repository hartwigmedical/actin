package com.hartwig.actin.datamodel.clinical

import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry

data class ClinicalRecord(
    val patientId: String,
    val patient: PatientDetails,
    val tumor: TumorDetails,
    val clinicalStatus: ClinicalStatus,
    val oncologicalHistory: List<TreatmentHistoryEntry>,
    val priorSecondPrimaries: List<PriorSecondPrimary>,
    val comorbidities: List<Comorbidity>,
    val priorIHCTests: List<PriorIHCTest>,
    val priorSequencingTests: List<PriorSequencingTest>,
    val labValues: List<LabValue>,
    val surgeries: List<Surgery>,
    val bodyWeights: List<BodyWeight>,
    val bodyHeights: List<BodyHeight>,
    val vitalFunctions: List<VitalFunction>,
    val bloodTransfusions: List<BloodTransfusion>,
    val medications: List<Medication>?,
) {

    val priorOtherConditions: List<PriorOtherCondition>
        get() = comorbidities.filterIsInstance<PriorOtherCondition>()

    val complications: List<Complication>
        get() = comorbidities.filterIsInstance<Complication>()

    val toxicities: List<Toxicity>
        get() = comorbidities.filterIsInstance<Toxicity>()

    val intolerances: List<Intolerance>
        get() = comorbidities.filterIsInstance<Intolerance>()
}
