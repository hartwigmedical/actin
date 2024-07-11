package com.hartwig.actin.molecular

import com.hartwig.actin.molecular.datamodel.MolecularTest

open class MolecularInterpreter<I, O, T : MolecularTest>(
    private val extractor: MolecularExtractor<I, O>,
    private val annotator: MolecularAnnotator<O, T>,
    val inputPredicate: (I) -> Boolean = { true }
) {
    fun run(input: List<I>): List<T> {
        return extractor.extract(input.filter(inputPredicate::invoke)).map(annotator::annotate)
    }
}