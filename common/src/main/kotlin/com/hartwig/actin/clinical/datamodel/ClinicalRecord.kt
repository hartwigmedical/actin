package com.hartwig.actin.clinical.datamodel

import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry

data class ClinicalRecord(
    val patientId: String,
    val patient: PatientDetails,
    val tumor: TumorDetails,
    val clinicalStatus: ClinicalStatus,
    val oncologicalHistory: List<TreatmentHistoryEntry>,
    val priorSecondPrimaries: List<PriorSecondPrimary>,
    val priorOtherConditions: List<PriorOtherCondition>,
    val priorIHCTests: List<PriorIHCTest>,
    val priorSequencingTests: List<PriorSequencingTest>,
    val complications: List<Complication>?,
    val labValues: List<LabValue>,
    val toxicities: List<Toxicity>,
    val intolerances: List<Intolerance>,
    val surgeries: List<Surgery>,
    val bodyWeights: List<BodyWeight>,
    val bodyHeights: List<BodyHeight>,
    val vitalFunctions: List<VitalFunction>,
    val bloodTransfusions: List<BloodTransfusion>,
    val medications: List<Medication>?,
)
