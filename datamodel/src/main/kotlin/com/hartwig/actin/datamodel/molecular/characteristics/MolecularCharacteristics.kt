package com.hartwig.actin.datamodel.molecular.characteristics

data class MolecularCharacteristics(
    val purity: Double? = null,
    val ploidy: Double? = null,
    val predictedTumorOrigin: PredictedTumorOrigin? = null,
    val microsatelliteStability: MicrosatelliteStability? = null,
    val homologousRecombination: HomologousRecombination? = null,
    val tumorMutationalBurden: TumorMutationalBurden? = null,
    val tumorMutationalLoad: TumorMutationalLoad? = null
)
