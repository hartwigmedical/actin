package com.hartwig.actin.clinical.sort

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest

class PriorMolecularTestComparator : Comparator<PriorMolecularTest> {

    override fun compare(priorMolecularTest1: PriorMolecularTest, priorMolecularTest2: PriorMolecularTest): Int {
        return priorMolecularTest1.item.compareTo(priorMolecularTest2.item)
    }
}
