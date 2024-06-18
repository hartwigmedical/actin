package com.hartwig.actin.molecular.datamodel.orange.driver

import com.hartwig.actin.molecular.datamodel.Drivers

data class MolecularDrivers(
    override val variants: Set<ExtendedVariant>,
    override val fusions: Set<ExtendedFusion>,
    val copyNumbers: Set<CopyNumber>,
    val homozygousDisruptions: Set<HomozygousDisruption>,
    val disruptions: Set<Disruption>,
    val viruses: Set<Virus>
) : Drivers<ExtendedVariant, ExtendedFusion>
