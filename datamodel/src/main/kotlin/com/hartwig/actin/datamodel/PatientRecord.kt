package com.hartwig.actin.datamodel

import com.hartwig.actin.datamodel.clinical.BloodTransfusion
import com.hartwig.actin.datamodel.clinical.BodyHeight
import com.hartwig.actin.datamodel.clinical.BodyWeight
import com.hartwig.actin.datamodel.clinical.ClinicalStatus
import com.hartwig.actin.datamodel.clinical.Comorbidity
import com.hartwig.actin.datamodel.clinical.Complication
import com.hartwig.actin.datamodel.clinical.Intolerance
import com.hartwig.actin.datamodel.clinical.LabValue
import com.hartwig.actin.datamodel.clinical.Medication
import com.hartwig.actin.datamodel.clinical.OtherCondition
import com.hartwig.actin.datamodel.clinical.PatientDetails
import com.hartwig.actin.datamodel.clinical.PriorIHCTest
import com.hartwig.actin.datamodel.clinical.PriorSecondPrimary
import com.hartwig.actin.datamodel.clinical.Surgery
import com.hartwig.actin.datamodel.clinical.Toxicity
import com.hartwig.actin.datamodel.clinical.TumorDetails
import com.hartwig.actin.datamodel.clinical.VitalFunction
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.datamodel.molecular.MolecularHistory

data class PatientRecord(
    val patientId: String,
    val patient: PatientDetails,
    val tumor: TumorDetails,
    val clinicalStatus: ClinicalStatus,
    val oncologicalHistory: List<TreatmentHistoryEntry>,
    val priorSecondPrimaries: List<PriorSecondPrimary>,
    val comorbidities: List<Comorbidity>,
    val labValues: List<LabValue>,
    val surgeries: List<Surgery>,
    val bodyWeights: List<BodyWeight>,
    val bodyHeights: List<BodyHeight>,
    val vitalFunctions: List<VitalFunction>,
    val bloodTransfusions: List<BloodTransfusion>,
    val medications: List<Medication>?,
    val priorIHCTests: List<PriorIHCTest>,
    val molecularHistory: MolecularHistory
) {

    val otherConditions: List<OtherCondition>
        get() = comorbidities.filterIsInstance<OtherCondition>()

    val complications: List<Complication>
        get() = comorbidities.filterIsInstance<Complication>()

    val toxicities: List<Toxicity>
        get() = comorbidities.filterIsInstance<Toxicity>()

    val intolerances: List<Intolerance>
        get() = comorbidities.filterIsInstance<Intolerance>()
}
