package com.hartwig.actin.clinical.sort

import com.hartwig.actin.datamodel.clinical.PriorIHCTest

class PriorIHCTestComparator : Comparator<PriorIHCTest> {

    override fun compare(priorIHCTest1: PriorIHCTest, priorIHCTest2: PriorIHCTest): Int {
        val firstPriorMolecularTestItem = priorIHCTest1.item ?: ""
        val secondPriorMolecularTestItem = priorIHCTest2.item ?: ""
        return firstPriorMolecularTestItem.compareTo(secondPriorMolecularTestItem)
    }
}
