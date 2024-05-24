package com.hartwig.actin.molecular.datamodel.wgs.driver

import com.hartwig.actin.molecular.datamodel.Drivers

data class MolecularDrivers(
    override val variants: Set<WgsVariant>,
    override val fusions: Set<WgsFusion>,
    val copyNumbers: Set<CopyNumber>,
    val homozygousDisruptions: Set<HomozygousDisruption>,
    val disruptions: Set<Disruption>,
    val viruses: Set<Virus>
) : Drivers<WgsVariant, WgsFusion>
