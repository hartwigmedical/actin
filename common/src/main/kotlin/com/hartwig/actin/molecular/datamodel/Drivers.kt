package com.hartwig.actin.molecular.datamodel

import com.hartwig.actin.molecular.datamodel.orange.driver.CopyNumber
import com.hartwig.actin.molecular.datamodel.orange.driver.Disruption
import com.hartwig.actin.molecular.datamodel.orange.driver.HomozygousDisruption
import com.hartwig.actin.molecular.datamodel.orange.driver.Virus

data class Drivers(
    val variants: Set<Variant> = emptySet(),
    val fusions: Set<Fusion> = emptySet(),
    val copyNumbers: Set<CopyNumber> = emptySet(),
    val homozygousDisruptions: Set<HomozygousDisruption> = emptySet(),
    val disruptions: Set<Disruption> = emptySet(),
    val viruses: Set<Virus> = emptySet()
)
