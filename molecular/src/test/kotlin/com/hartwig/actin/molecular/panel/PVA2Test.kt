package com.hartwig.actin.molecular.panel

import org.junit.Test

class PVA2Test {

    @Test
    fun `Should figure out flatMapIndexed`() {

        val input = listOf(1, 2, 3, 4, 5)

        val output = input.flatMap { it -> if (it % 2 == 0) listOf(it) else listOf(it + 100, it + 200) }
        println(output)

        val output2 = input.flatMapIndexed { index, it -> if (it % 2 == 0) listOf(it) else listOf(it + 100, it + 200) }
        println(output2)
    }
}