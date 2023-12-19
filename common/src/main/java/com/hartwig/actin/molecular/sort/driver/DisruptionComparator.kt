package com.hartwig.actin.molecular.sort.driver

import com.hartwig.actin.molecular.datamodel.driver.Disruption

class DisruptionComparator : Comparator<Disruption> {
    override fun compare(disruption1: Disruption, disruption2: Disruption): Int {
        val driverCompare = DRIVER_COMPARATOR.compare(disruption1, disruption2)
        if (driverCompare != 0) {
            return driverCompare
        }
        val geneAlterationCompare = GENE_ALTERATION_COMPARATOR.compare(disruption1, disruption2)
        if (geneAlterationCompare != 0) {
            return geneAlterationCompare
        }
        val typeCompare = disruption1.type().toString().compareTo(disruption2.type().toString())
        if (typeCompare != 0) {
            return typeCompare
        }
        val junctionCompare = java.lang.Double.compare(disruption2.junctionCopyNumber(), disruption1.junctionCopyNumber())
        return if (junctionCompare != 0) {
            junctionCompare
        } else java.lang.Double.compare(disruption1.undisruptedCopyNumber(), disruption2.undisruptedCopyNumber())
    }

    companion object {
        private val DRIVER_COMPARATOR = DriverComparator()
        private val GENE_ALTERATION_COMPARATOR = GeneAlterationComparator()
    }
}
