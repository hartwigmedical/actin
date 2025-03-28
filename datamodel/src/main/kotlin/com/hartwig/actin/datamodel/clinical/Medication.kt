package com.hartwig.actin.datamodel.clinical

import com.hartwig.actin.datamodel.clinical.treatment.Drug
import java.time.LocalDate

data class Medication(
    val name: String,
    val status: MedicationStatus? = null,
    val administrationRoute: String? = null,
    val dosage: Dosage,
    val startDate: LocalDate? = null,
    val stopDate: LocalDate? = null,
    val drug: Drug? = null,
    val cypInteractions: List<DrugInteraction> = emptyList(),
    val transporterInteractions: List<DrugInteraction> = emptyList(),
    val qtProlongatingRisk: QTProlongatingRisk,
    val atc: AtcClassification? = null,
    val isSelfCare: Boolean,
    val isTrialMedication: Boolean
) {
    
    fun allLevels(): Set<AtcLevel> {
        return atc?.allLevels() ?: emptySet()
    }
}
