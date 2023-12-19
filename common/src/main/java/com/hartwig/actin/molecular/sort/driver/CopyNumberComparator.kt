package com.hartwig.actin.molecular.sort.driver

import com.hartwig.actin.molecular.datamodel.driver.CopyNumber

class CopyNumberComparator : Comparator<CopyNumber> {
    override fun compare(copyNumber1: CopyNumber, copyNumber2: CopyNumber): Int {
        val driverCompare = DRIVER_COMPARATOR.compare(copyNumber1, copyNumber2)
        return if (driverCompare != 0) {
            driverCompare
        } else GENE_ALTERATION_COMPARATOR.compare(copyNumber1, copyNumber2)
    }

    companion object {
        private val DRIVER_COMPARATOR = DriverComparator()
        private val GENE_ALTERATION_COMPARATOR = GeneAlterationComparator()
    }
}
