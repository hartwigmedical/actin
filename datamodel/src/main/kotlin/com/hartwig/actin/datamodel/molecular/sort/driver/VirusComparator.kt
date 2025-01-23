package com.hartwig.actin.datamodel.molecular.sort.driver

import com.hartwig.actin.datamodel.molecular.driver.Virus

class VirusComparator : Comparator<Virus> {

    private val comparator = Comparator.comparing<Virus, Virus>({ it }, DriverComparator())
        .thenComparing({ it.type.toString() }, String::compareTo)
        .thenComparing(Virus::name)
    
    override fun compare(virus1: Virus, virus2: Virus): Int {
        return comparator.compare(virus1, virus2)
    }
}
