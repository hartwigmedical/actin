package com.hartwig.actin.clinical.sort

import com.hartwig.actin.datamodel.clinical.IHCTest

class IHCTestComparator : Comparator<IHCTest> {

    override fun compare(ihcTests1: IHCTest, ihcTests2: IHCTest): Int {
        val firstMolecularTestItem = ihcTests1.item ?: ""
        val secondMolecularTestItem = ihcTests2.item ?: ""
        return firstMolecularTestItem.compareTo(secondMolecularTestItem)
    }
}
