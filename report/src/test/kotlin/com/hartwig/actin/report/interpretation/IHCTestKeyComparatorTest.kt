package com.hartwig.actin.report.interpretation

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class IHCTestKeyComparatorTest {

    @Test
    fun `Should sort molecular test keys`() {
        val key1 = create("text 1", "test 1")
        val key2 = create("text 1", "test 2")
        val key3 = create("text 2", "test 1")
        val keys = listOf(key2, key3, key1).sortedWith(MolecularTestKeyComparator())

        assertThat(keys[0]).isEqualTo(key1)
        assertThat(keys[1]).isEqualTo(key2)
        assertThat(keys[2]).isEqualTo(key3)
    }

    private fun create(scoreText: String, test: String): MolecularTestKey {
        return MolecularTestKey(scoreText = scoreText, test = test)
    }
}