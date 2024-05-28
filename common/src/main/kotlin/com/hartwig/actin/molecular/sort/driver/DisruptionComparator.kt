package com.hartwig.actin.molecular.sort.driver

import com.hartwig.actin.molecular.datamodel.hmf.driver.Disruption

class DisruptionComparator : Comparator<Disruption> {

    private val comparator = Comparator.comparing<Disruption, Disruption>({ it }, DriverComparator())
        .thenComparing({ it }, GeneAlterationComparator())
        .thenComparing({ it.type.toString() }, String::compareTo)
        .thenComparing(Disruption::junctionCopyNumber, reverseOrder())
        .thenComparing(Disruption::undisruptedCopyNumber)
    
    override fun compare(disruption1: Disruption, disruption2: Disruption): Int {
        return comparator.compare(disruption1, disruption2)
    }
}
