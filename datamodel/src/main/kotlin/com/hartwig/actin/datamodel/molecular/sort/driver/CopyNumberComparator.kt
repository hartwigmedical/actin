package com.hartwig.actin.datamodel.molecular.sort.driver

import com.hartwig.actin.datamodel.molecular.driver.CopyNumber

class CopyNumberComparator : Comparator<CopyNumber> {

    private val comparator = Comparator.comparing<CopyNumber, CopyNumber>({ it }, DriverComparator())
        .thenComparing({ it }, GeneAlterationComparator())
    
    override fun compare(copyNumber1: CopyNumber, copyNumber2: CopyNumber): Int {
        return comparator.compare(copyNumber1, copyNumber2)
    }
}
