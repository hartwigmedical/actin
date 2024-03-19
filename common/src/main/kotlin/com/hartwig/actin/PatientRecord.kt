package com.hartwig.actin

import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.molecular.datamodel.MolecularRecord

data class PatientRecord(
    val patientId: String,
    val patient: PatientDetails,
    val tumor: TumorDetails,
    val clinicalStatus: ClinicalStatus,
    val oncologicalHistory: List<TreatmentHistoryEntry>,
    val priorSecondPrimaries: List<PriorSecondPrimary>,
    val priorOtherConditions: List<PriorOtherCondition>,
    val priorMolecularTests: List<PriorMolecularTest>,
    val complications: List<Complication>?,
    val labValues: List<LabValue>,
    val toxicities: List<Toxicity>,
    val intolerances: List<Intolerance>,
    val surgeries: List<Surgery>,
    val bodyWeights: List<BodyWeight>,
    val vitalFunctions: List<VitalFunction>,
    val bloodTransfusions: List<BloodTransfusion>,
    val medications: List<Medication>,
    val molecular: MolecularRecord
)
