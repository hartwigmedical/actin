package com.hartwig.actin.molecular.datamodel.hmf.driver

import com.hartwig.actin.molecular.datamodel.Drivers

data class MolecularDrivers(
    override val variants: Set<ExtendedVariant>,
    override val fusions: Set<ExhaustiveFusion>,
    val copyNumbers: Set<CopyNumber>,
    val homozygousDisruptions: Set<HomozygousDisruption>,
    val disruptions: Set<Disruption>,
    val viruses: Set<Virus>
) : Drivers<ExtendedVariant, ExhaustiveFusion>
