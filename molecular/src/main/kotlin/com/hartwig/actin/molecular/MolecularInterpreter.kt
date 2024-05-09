package com.hartwig.actin.molecular

import com.hartwig.actin.molecular.datamodel.MolecularTest

interface MolecularInterpreter<I, O> {

    fun interpret(input: List<I>): List<MolecularTest<O>>

}