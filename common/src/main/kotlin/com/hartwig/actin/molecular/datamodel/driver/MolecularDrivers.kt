package com.hartwig.actin.molecular.datamodel.driver

import com.hartwig.actin.molecular.interpreted.InterpretedDrivers

data class MolecularDrivers(
    override val variants: Set<Variant>,
    override val fusions: Set<Fusion>,
    val copyNumbers: Set<CopyNumber>,
    val homozygousDisruptions: Set<HomozygousDisruption>,
    val disruptions: Set<Disruption>,
    val viruses: Set<Virus>
) : InterpretedDrivers<Variant, Fusion>
