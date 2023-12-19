package com.hartwig.actin.molecular.sort.driver

import com.hartwig.actin.molecular.datamodel.driver.Virus

class VirusComparator : Comparator<Virus> {
    override fun compare(virus1: Virus, virus2: Virus): Int {
        val driverCompare = DRIVER_COMPARATOR.compare(virus1, virus2)
        if (driverCompare != 0) {
            return driverCompare
        }
        val typeCompare = virus1.type().toString().compareTo(virus2.type().toString())
        return if (typeCompare != 0) {
            typeCompare
        } else virus1.name().compareTo(virus2.name())
    }

    companion object {
        private val DRIVER_COMPARATOR = DriverComparator()
    }
}
