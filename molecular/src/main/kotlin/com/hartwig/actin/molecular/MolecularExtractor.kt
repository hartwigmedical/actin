package com.hartwig.actin.molecular

interface MolecularExtractor<I, O> {

    fun extract(input: List<I>): List<O>
}