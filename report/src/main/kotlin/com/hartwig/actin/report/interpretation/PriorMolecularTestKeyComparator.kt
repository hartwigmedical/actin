package com.hartwig.actin.report.interpretation

class PriorMolecularTestKeyComparator : Comparator<PriorMolecularTestKey> {
    override fun compare(key1: PriorMolecularTestKey, key2: PriorMolecularTestKey): Int {
        val scoreTextCompare = key1.scoreText().compareTo(key2.scoreText())
        return if (scoreTextCompare != 0) {
            scoreTextCompare
        } else key1.test().compareTo(key2.test())
    }
}