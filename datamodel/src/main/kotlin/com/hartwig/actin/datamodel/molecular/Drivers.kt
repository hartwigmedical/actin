package com.hartwig.actin.datamodel.molecular

import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.orange.driver.Disruption
import com.hartwig.actin.datamodel.molecular.orange.driver.HomozygousDisruption
import com.hartwig.actin.datamodel.molecular.orange.driver.Virus

data class Drivers(
    val variants: Set<Variant> = emptySet(),
    val copyNumbers: Set<CopyNumber> = emptySet(),
    val homozygousDisruptions: Set<HomozygousDisruption> = emptySet(),
    val disruptions: Set<Disruption> = emptySet(),
    val fusions: Set<Fusion> = emptySet(),
    val viruses: Set<Virus> = emptySet()
)
