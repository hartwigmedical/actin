package com.hartwig.actin.molecular

import com.hartwig.actin.molecular.datamodel.MolecularTest

interface MolecularExtractor<I, O : MolecularTest> {
    fun extract(input: List<I>): List<O>
}