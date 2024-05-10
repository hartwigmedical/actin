package com.hartwig.actin.molecular

import com.hartwig.actin.molecular.datamodel.MolecularTest

open class MolecularInterpreter<I, O : MolecularTest>(
    private val extractor: MolecularExtractor<I, O>,
    private val annotator: MolecularAnnotator<O>,
    val inputPredicate: (I) -> Boolean = { true }
) {
    fun run(input: List<I>): List<O> {
        return extractor.interpret(input.filter { inputPredicate.invoke(it) }).map { annotator.annotate(it) }
    }
}

