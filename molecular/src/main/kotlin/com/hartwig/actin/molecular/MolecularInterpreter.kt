package com.hartwig.actin.molecular

open class MolecularInterpreter<I, O>(
    private val extractor: MolecularExtractor<I, O>,
    private val annotator: MolecularAnnotator<O>,
    private val postAnnotators: List<MolecularAnnotator> = emptyList()
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