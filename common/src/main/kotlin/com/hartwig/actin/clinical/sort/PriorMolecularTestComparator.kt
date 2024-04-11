package com.hartwig.actin.clinical.sort

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest

class PriorMolecularTestComparator : Comparator<PriorMolecularTest> {

    override fun compare(priorMolecularTest1: PriorMolecularTest, priorMolecularTest2: PriorMolecularTest): Int {
        val firstPriorMolecularTestItem = priorMolecularTest1.item ?: ""
        val secondPriorMolecularTestItem = priorMolecularTest2.item ?: ""
        return firstPriorMolecularTestItem.compareTo(secondPriorMolecularTestItem)
    }
}
