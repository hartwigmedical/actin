package com.hartwig.actin.report.interpretation

import com.hartwig.actin.molecular.sort.driver.DriverLikelihoodComparator
import java.lang.Boolean
import java.util.*
import kotlin.Comparator
import kotlin.Int
import kotlin.String

class MolecularDriverEntryComparator : Comparator<MolecularDriverEntry> {
    override fun compare(entry1: MolecularDriverEntry, entry2: MolecularDriverEntry): Int {
        val driverLikelihoodCompare = DRIVER_LIKELIHOOD_COMPARATOR.compare(entry1.driverLikelihood(), entry2.driverLikelihood())
        if (driverLikelihoodCompare != 0) {
            return driverLikelihoodCompare
        }
        val driverTypeCompare = DRIVER_TYPE_COMPARATOR.compare(entry1.driverType(), entry2.driverType())
        return if (driverTypeCompare != 0) {
            driverTypeCompare
        } else entry1.driver().compareTo(entry2.driver())
    }

    private class DriverTypeComparator : Comparator<String> {
        override fun compare(string1: String, string2: String): Int {
            val type1 = string1.lowercase(Locale.getDefault())
            val type2 = string2.lowercase(Locale.getDefault())
            val mutationCompare = Boolean.compare(type2.startsWith("mutation"), type1.startsWith("mutation"))
            if (mutationCompare != 0) {
                return mutationCompare
            }
            val amplificationCompare = Boolean.compare(type2.startsWith("amplification"), type1.startsWith("amplification"))
            if (amplificationCompare != 0) {
                return amplificationCompare
            }
            val lossCompare = Boolean.compare(type2.startsWith("loss"), type1.startsWith("loss"))
            if (lossCompare != 0) {
                return lossCompare
            }
            val fusionCompare = Boolean.compare(type2.contains("fusion"), type1.contains("fusion"))
            if (fusionCompare != 0) {
                return fusionCompare
            }
            val disruptionCompare = Boolean.compare(type2.contains("disruption"), type1.contains("disruption"))
            if (disruptionCompare != 0) {
                return disruptionCompare
            }
            val virusCompare = Boolean.compare(type2.startsWith("virus"), type1.startsWith("virus"))
            return if (virusCompare != 0) {
                virusCompare
            } else type1.compareTo(type2)
        }
    }

    companion object {
        private val DRIVER_LIKELIHOOD_COMPARATOR = DriverLikelihoodComparator()
        private val DRIVER_TYPE_COMPARATOR = DriverTypeComparator()
    }
}