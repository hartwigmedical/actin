package com.hartwig.actin.molecular

import com.hartwig.actin.molecular.datamodel.MolecularTest

interface MolecularAnnotator<I, O : MolecularTest<*>> {

    fun annotate(input: I): O
}
