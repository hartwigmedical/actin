package com.hartwig.actin.datamodel.molecular

import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence

data class MolecularCharacteristics(
    val purity: Double? = null,
    val ploidy: Double? = null,
    val predictedTumorOrigin: PredictedTumorOrigin? = null,
    val isMicrosatelliteUnstable: Boolean? = null,
    val microsatelliteEvidence: ClinicalEvidence? = null,
    val homologousRepairScore: Double? = null,
    val isHomologousRepairDeficient: Boolean? = null,
    val homologousRepairEvidence: ClinicalEvidence? = null,
    val tumorMutationalBurden: Double? = null,
    val hasHighTumorMutationalBurden: Boolean? = null,
    val tumorMutationalBurdenEvidence: ClinicalEvidence? = null,
    val tumorMutationalLoad: Int? = null,
    val hasHighTumorMutationalLoad: Boolean? = null,
    val tumorMutationalLoadEvidence: ClinicalEvidence? = null,
)
