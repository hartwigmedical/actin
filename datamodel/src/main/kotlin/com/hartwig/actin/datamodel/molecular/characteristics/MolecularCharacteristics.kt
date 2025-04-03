package com.hartwig.actin.datamodel.molecular.characteristics

data class MolecularCharacteristics(
    val purity: Double?,
    val ploidy: Double?,
    val predictedTumorOrigin: PredictedTumorOrigin?,
    val microsatelliteStability: MicrosatelliteStability?,
    val homologousRecombination: HomologousRecombination?,
    val tumorMutationalBurden: TumorMutationalBurden?,
    val tumorMutationalLoad: TumorMutationalLoad?
)
