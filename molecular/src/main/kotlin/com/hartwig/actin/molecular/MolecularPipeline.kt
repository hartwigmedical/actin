package com.hartwig.actin.molecular

import com.hartwig.actin.molecular.datamodel.MolecularTest

class MolecularPipeline<I, O>(private val interpreter: MolecularInterpreter<I, O>, private val annotator: MolecularAnnotator<O>) {

    fun run(input: List<I>): List<MolecularTest<O>> {
        return interpreter.interpret(input).map { annotator.annotate(it) }
    }
}