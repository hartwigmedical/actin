package com.hartwig.actin.datamodel.clinical

import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry

data class ClinicalRecord(
    val patientId: String,
    val patient: PatientDetails,
    val tumor: TumorDetails,
    val clinicalStatus: ClinicalStatus,
    val oncologicalHistory: List<TreatmentHistoryEntry>,
    val priorPrimaries: List<PriorPrimary>,
    val comorbidities: List<Comorbidity>,
    val ihcTests: List<IhcTest>,
    val sequencingTests: List<SequencingTest>,
    val labValues: List<LabValue>,
    val surgeries: List<Surgery>,
    val bodyWeights: List<BodyWeight>,
    val bodyHeights: List<BodyHeight>,
    val vitalFunctions: List<VitalFunction>,
    val bloodTransfusions: List<BloodTransfusion>,
    val medications: List<Medication>?,
    val pathologyReports: List<PathologyReport>?
) {

    val otherConditions: List<OtherCondition>
        get() = comorbidities.filterIsInstance<OtherCondition>()

    val complications: List<Complication>
        get() = comorbidities.filterIsInstance<Complication>()

    val toxicities: List<Toxicity>
        get() = comorbidities.filterIsInstance<Toxicity>()

    val intolerances: List<Intolerance>
        get() = comorbidities.filterIsInstance<Intolerance>()

    val ecgs: List<Ecg>
        get() = comorbidities.filterIsInstance<Ecg>()
}
