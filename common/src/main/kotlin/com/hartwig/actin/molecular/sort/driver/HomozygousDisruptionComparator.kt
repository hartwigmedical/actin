package com.hartwig.actin.molecular.sort.driver

import com.hartwig.actin.molecular.datamodel.wgs.driver.HomozygousDisruption

class HomozygousDisruptionComparator : Comparator<HomozygousDisruption> {

    private val comparator = Comparator.comparing<HomozygousDisruption, HomozygousDisruption>({ it }, DriverComparator())
        .thenComparing({ it }, GeneAlterationComparator())

    override fun compare(homozygousDisruption1: HomozygousDisruption, homozygousDisruption2: HomozygousDisruption): Int {
        return comparator.compare(homozygousDisruption1, homozygousDisruption2)
    }
}
