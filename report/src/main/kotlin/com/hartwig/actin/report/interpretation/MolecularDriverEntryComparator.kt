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
        } else entry1.driver.compareTo(entry2.driver)
    }

    private class DriverTypeComparator : Comparator<String> {
        override fun compare(string1: String, string2: String): Int {
            return compareValuesBy(
                string1.lowercase(), string2.lowercase(),
                { it.startsWith("mutation") },
                { it.startsWith("amplification") },
                { it.startsWith("loss") },
                { it.contains("fusion") },
                { it.contains("disruption") },
                { it.startsWith("virus") },
                { it }
            )
        }
    }

    companion object {
        private val DRIVER_LIKELIHOOD_COMPARATOR = DriverLikelihoodComparator()
        private val DRIVER_TYPE_COMPARATOR = DriverTypeComparator()
    }
}