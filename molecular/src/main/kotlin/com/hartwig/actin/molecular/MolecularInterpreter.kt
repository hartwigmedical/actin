package com.hartwig.actin.molecular

import com.hartwig.actin.datamodel.molecular.MolecularTest

open class MolecularInterpreter<I, O, T : MolecularTest>(
    private val extractor: MolecularExtractor<I, O>,
    private val annotator: MolecularAnnotator<O, T>,
    private val postAnnotators: List<MolecularAnnotator<T, T>> = emptyList()
) {

    fun run(input: List<I>): List<T> {
        val extracted = extractor.extract(input).map(annotator::annotate)
        return extracted.map { item ->
            postAnnotators.fold(item) { intermediate, annotator ->
                annotator.annotate(intermediate)
            }
        }
    }
}