package com.hartwig.actin.report.interpretation

class PriorMolecularTestKeyComparator : Comparator<PriorMolecularTestKey> {

    override fun compare(key1: PriorMolecularTestKey, key2: PriorMolecularTestKey): Int {
        return compareBy(PriorMolecularTestKey::scoreText).thenBy(PriorMolecularTestKey::test).compare(key1, key2)
    }
}