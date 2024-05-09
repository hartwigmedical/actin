package com.hartwig.actin.molecular

import com.hartwig.actin.molecular.datamodel.MolecularTest

interface MolecularAnnotator<T> {

    fun annotate(input: MolecularTest<T>): MolecularTest<T>
}
