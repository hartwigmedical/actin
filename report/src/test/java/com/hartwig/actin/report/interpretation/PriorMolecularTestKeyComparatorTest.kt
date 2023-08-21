package com.hartwig.actin.report.interpretation

import com.google.common.collect.Lists
import org.junit.Assert
import org.junit.Test

class PriorMolecularTestKeyComparatorTest {
    @Test
    fun canSortPriorMolecularTestKeys() {
        val key1 = create("text 1", "test 1")
        val key2 = create("text 1", "test 2")
        val key3 = create("text 2", "test 1")
        val keys: List<PriorMolecularTestKey> = Lists.newArrayList(key2, key3, key1)
        keys.sort(PriorMolecularTestKeyComparator())
        Assert.assertEquals(key1, keys[0])
        Assert.assertEquals(key2, keys[1])
        Assert.assertEquals(key3, keys[2])
    }

    private fun create(scoreText: String, test: String): PriorMolecularTestKey {
        return ImmutablePriorMolecularTestKey.builder().scoreText(scoreText).test(test).build()
    }
}