package com.hartwig.actin.datamodel.molecular.characteristics

import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence

data class MolecularCharacteristics(
    val purity: Double? = null,
    val ploidy: Double? = null,
    val predictedTumorOrigin: PredictedTumorOrigin? = null,
    val isMicrosatelliteUnstable: Boolean? = null,
    val microsatelliteEvidence: ClinicalEvidence? = null,
    val homologousRecombinationScore: Double? = null,
    val isHomologousRecombinationDeficient: Boolean? = null,
    val brca1Value: Double? = null,
    val brca2Value: Double? = null,
    val hrdType: HrdType? = null,
    val homologousRecombinationEvidence: ClinicalEvidence? = null,
    val tumorMutationalBurden: Double? = null,
    val hasHighTumorMutationalBurden: Boolean? = null,
    val tumorMutationalBurdenEvidence: ClinicalEvidence? = null,
    val tumorMutationalLoad: Int? = null,
    val hasHighTumorMutationalLoad: Boolean? = null,
    val tumorMutationalLoadEvidence: ClinicalEvidence? = null,
)
