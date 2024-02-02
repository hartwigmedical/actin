package com.hartwig.actin.molecular.datamodel.driver

data class MolecularDrivers(
    val variants: Set<Variant>,
    val copyNumbers: Set<CopyNumber>,
    val geneCopyNumbers: Set<CopyNumber>,
    val homozygousDisruptions: Set<HomozygousDisruption>,
    val disruptions: Set<Disruption>,
    val fusions: Set<Fusion>,
    val viruses: Set<Virus>
)
