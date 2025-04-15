package com.hartwig.actin.datamodel.molecular.driver

data class Drivers(
    val variants: List<Variant>,
    val copyNumbers: List<CopyNumber>,
    val homozygousDisruptions: List<HomozygousDisruption>,
    val disruptions: List<Disruption>,
    val fusions: List<Fusion>,
    val viruses: List<Virus>
) {

    fun combine(other: Drivers): Drivers {
        return Drivers(
            variants = variants + other.variants,
            copyNumbers = copyNumbers + other.copyNumbers,
            homozygousDisruptions = homozygousDisruptions + other.homozygousDisruptions,
            disruptions = disruptions + other.disruptions,
            fusions = fusions + other.fusions,
            viruses = viruses + other.viruses
        )
    }
}