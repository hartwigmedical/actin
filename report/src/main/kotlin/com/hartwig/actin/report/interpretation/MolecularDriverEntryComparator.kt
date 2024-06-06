package com.hartwig.actin.report.interpretation

import com.hartwig.actin.molecular.sort.driver.DriverLikelihoodComparator

class MolecularDriverEntryComparator : Comparator<MolecularDriverEntry> {
    override fun compare(entry1: MolecularDriverEntry, entry2: MolecularDriverEntry): Int {
        val driverLikelihoodCompare = DRIVER_LIKELIHOOD_COMPARATOR.compare(entry1.driverLikelihood, entry2.driverLikelihood)
        if (driverLikelihoodCompare != 0) {
            return driverLikelihoodCompare
        }
        val driverTypeCompare = DRIVER_TYPE_COMPARATOR.compare(entry1.driverType, entry2.driverType)
        return if (driverTypeCompare != 0) {
            driverTypeCompare
        } else entry1.name.compareTo(entry2.name)
    }

    private class DriverTypeComparator : Comparator<String> {
        override fun compare(string1: String, string2: String): Int {
            return compareBy<String> { it.startsWith("mutation") }
                .thenBy { it.startsWith("amplification") }
                .thenBy { it.startsWith("loss") }
                .thenBy { it.contains("fusion") }
                .thenBy { it.contains("disruption") }
                .thenBy { it.startsWith("virus") }
                .thenByDescending { it }
                .compare(string2.lowercase(), string1.lowercase())
        }
    }

    companion object {
        private val DRIVER_LIKELIHOOD_COMPARATOR = DriverLikelihoodComparator()
        private val DRIVER_TYPE_COMPARATOR = DriverTypeComparator()
    }
}