package com.hartwig.actin.clinical.sort

import com.hartwig.actin.datamodel.clinical.IhcTest

class IhcTestComparator : Comparator<IhcTest> {

    override fun compare(ihcTests1: IhcTest, ihcTests2: IhcTest): Int {
        val firstMolecularTestItem = ihcTests1.item ?: ""
        val secondMolecularTestItem = ihcTests2.item ?: ""
        return firstMolecularTestItem.compareTo(secondMolecularTestItem)
    }
}
