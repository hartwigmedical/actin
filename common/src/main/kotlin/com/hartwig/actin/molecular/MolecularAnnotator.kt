package com.hartwig.actin.molecular

import com.hartwig.actin.datamodel.molecular.MolecularTest

interface MolecularAnnotator<I> {

    fun annotate(input: I): MolecularTest
}
