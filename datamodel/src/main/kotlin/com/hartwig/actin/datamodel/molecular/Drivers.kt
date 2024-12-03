package com.hartwig.actin.datamodel.molecular

import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.orange.driver.Disruption
import com.hartwig.actin.datamodel.molecular.orange.driver.HomozygousDisruption
import com.hartwig.actin.datamodel.molecular.orange.driver.Virus

data class Drivers(
    val variants: List<Variant> = emptyList(),
    val copyNumbers: List<CopyNumber> = emptyList(),
    val homozygousDisruptions: List<HomozygousDisruption> = emptyList(),
    val disruptions: List<Disruption> = emptyList(),
    val fusions: List<Fusion> = emptyList(),
    val viruses: List<Virus> = emptyList()
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
