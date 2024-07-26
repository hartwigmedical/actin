package com.hartwig.actin.molecular.priormoleculartest

import com.hartwig.actin.clinical.datamodel.PriorIHCTest
import com.hartwig.actin.clinical.datamodel.PriorSequencingTest
import com.hartwig.actin.molecular.MolecularExtractor
import com.hartwig.actin.molecular.datamodel.panel.PanelExtraction

private const val MCGI_PREFIX = "MCGI "

class McgiExtractor : MolecularExtractor<PriorSequencingTest, PanelExtraction> {
    override fun extract(input: List<PriorSequencingTest>): List<PanelExtraction> {
        /* return input.groupBy { it.test to it.measureDate }
             .map { (grouping, results) ->
                 val variants =
                     results.filter { it.scoreText == "variant" }.mapNotNull(::nullSafeItemAndMeasure)
                         .map { PanelVariantExtraction(it.first, it.second) }
                 val amplification =
                     results.filter { it.scoreText == "amplification" }.mapNotNull(::nullSafeItemAndMeasure)
                         .map { PanelAmplificationExtraction(it.first, it.second) }
                 val msi = results.firstOrNull { it.scoreText == "msi" }?.let { it.measure?.toBoolean() }
                 val tmb = results.firstOrNull { it.scoreText == "tmb" }?.let { it.measure?.toDouble() }
                 McgiExtraction(
                     panelType = grouping.first.replace(MCGI_PREFIX, ""),
                     date = grouping.second,
                     variants = variants,
                     amplifications = amplification,
                     isMicrosatelliteUnstable = msi,
                     tumorMutationalBurden = tmb
                 )
             }*/
        return emptyList()
    }

    private fun nullSafeItemAndMeasure(test: PriorIHCTest): Pair<String, String>? {
        val item = test.item
        val measure = test.measure
        return if (item != null && measure != null) {
            item to measure
        } else {
            null
        }
    }
}