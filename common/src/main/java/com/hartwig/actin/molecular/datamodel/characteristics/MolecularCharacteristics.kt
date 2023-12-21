package com.hartwig.actin.molecular.datamodel.characteristics

import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence

data class MolecularCharacteristics(
    val purity: Double?,
    val ploidy: Double?,
    val predictedTumorOrigin: PredictedTumorOrigin?,
    val isMicrosatelliteUnstable: Boolean?,
    val microsatelliteEvidence: ActionableEvidence?,
    val homologousRepairScore: Double?,
    val isHomologousRepairDeficient: Boolean?,
    val homologousRepairEvidence: ActionableEvidence?,
    val tumorMutationalBurden: Double?,
    val hasHighTumorMutationalBurden: Boolean?,
    val tumorMutationalBurdenEvidence: ActionableEvidence?,
    val tumorMutationalLoad: Int?,
    val hasHighTumorMutationalLoad: Boolean?,
    val tumorMutationalLoadEvidence: ActionableEvidence?,
)
