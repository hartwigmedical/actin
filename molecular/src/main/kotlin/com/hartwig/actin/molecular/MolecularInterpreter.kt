package com.hartwig.actin.molecular

import com.hartwig.actin.datamodel.molecular.MolecularTest

open class MolecularInterpreter<I, O, T : MolecularTest>(
    private val extractor: MolecularExtractor<I, O>,
    private val annotator: MolecularAnnotator<O, T>
) {

    fun run(input: List<I>): List<T> {
        return extractor.extract(input).map(annotator::annotate)
    }
}