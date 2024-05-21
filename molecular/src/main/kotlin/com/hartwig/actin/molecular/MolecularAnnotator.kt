package com.hartwig.actin.molecular

import com.hartwig.actin.molecular.datamodel.MolecularTest

interface MolecularAnnotator<T : MolecularTest> {

    fun annotate(input: T): T
}
