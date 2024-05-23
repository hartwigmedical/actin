package com.hartwig.actin.molecular.datamodel.characteristics

import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence

data class MolecularCharacteristics(
    val purity: Double? = null,
    val ploidy: Double? = null,
    val predictedTumorOrigin: PredictedTumorOrigin? = null,
    val isMicrosatelliteUnstable: Boolean? = null,
    val microsatelliteEvidence: ActionableEvidence? = null,
    val homologousRepairScore: Double? = null,
    val isHomologousRepairDeficient: Boolean? = null,
    val homologousRepairEvidence: ActionableEvidence? = null,
    val tumorMutationalBurden: Double? = null,
    val hasHighTumorMutationalBurden: Boolean? = null,
    val tumorMutationalBurdenEvidence: ActionableEvidence? = null,
    val tumorMutationalLoad: Int? = null,
    val hasHighTumorMutationalLoad: Boolean? = null,
    val tumorMutationalLoadEvidence: ActionableEvidence? = null,
)
