package com.hartwig.actin.molecular.datamodel.orange.driver

import com.hartwig.actin.molecular.datamodel.Fusion
import com.hartwig.actin.molecular.datamodel.Variant

data class MolecularDrivers(
    val variants: Set<Variant> = emptySet(),
    val fusions: Set<Fusion> = emptySet(),
    val copyNumbers: Set<CopyNumber> = emptySet(),
    val homozygousDisruptions: Set<HomozygousDisruption> = emptySet(),
    val disruptions: Set<Disruption> = emptySet(),
    val viruses: Set<Virus> = emptySet()
)
