package com.hartwig.actin.report.interpretation

class MolecularTestKeyComparator : Comparator<MolecularTestKey> {

    override fun compare(key1: MolecularTestKey, key2: MolecularTestKey): Int {
        return compareBy(MolecularTestKey::scoreText).thenBy(MolecularTestKey::test).compare(key1, key2)
    }
}