package com.hartwig.actin.molecular.sort.driver

import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption

class HomozygousDisruptionComparator : Comparator<HomozygousDisruption> {
    override fun compare(homozygousDisruption1: HomozygousDisruption, homozygousDisruption2: HomozygousDisruption): Int {
        val driverCompare = DRIVER_COMPARATOR.compare(homozygousDisruption1, homozygousDisruption2)
        return if (driverCompare != 0) {
            driverCompare
        } else GENE_ALTERATION_COMPARATOR.compare(homozygousDisruption1, homozygousDisruption2)
    }

    companion object {
        private val DRIVER_COMPARATOR = DriverComparator()
        private val GENE_ALTERATION_COMPARATOR = GeneAlterationComparator()
    }
}
