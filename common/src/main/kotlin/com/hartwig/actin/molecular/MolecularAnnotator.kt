package com.hartwig.actin.molecular

import com.hartwig.actin.datamodel.molecular.MolecularTest

interface MolecularAnnotator<I, O : MolecularTest> {

    fun annotate(input: I): O
}
