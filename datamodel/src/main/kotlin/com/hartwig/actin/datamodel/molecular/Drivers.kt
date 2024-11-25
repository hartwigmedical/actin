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
