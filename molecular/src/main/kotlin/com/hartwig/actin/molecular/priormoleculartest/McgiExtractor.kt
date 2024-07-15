package com.hartwig.actin.molecular.priormoleculartest

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.molecular.MolecularExtractor
import com.hartwig.actin.molecular.datamodel.panel.PanelAmplificationExtraction
import com.hartwig.actin.molecular.datamodel.panel.PanelExtraction
import com.hartwig.actin.molecular.datamodel.panel.PanelVariantExtraction
import com.hartwig.actin.molecular.datamodel.panel.mcgi.McgiExtraction

class McgiExtractor : MolecularExtractor<PriorMolecularTest, PanelExtraction> {
    override fun extract(input: List<PriorMolecularTest>): List<PanelExtraction> {
        return input.groupBy { it.measureDate }
            .map { (date, results) ->
                val variants =
                    results.filter { it.scoreText == "variant" }.map { PanelVariantExtraction(it.item!!, it.measure!!) }
                val amplification =
                    results.filter { it.scoreText == "amplification" }.map { PanelAmplificationExtraction(it.item!!, it.measure!!) }
                val msi = results.filter { it.scoreText == "msi" }.map { it.measure?.toBoolean() }.firstOrNull()
                val tmb = results.filter { it.scoreText == "tmb" }.map { it.measure?.toDouble() }.firstOrNull()
                McgiExtraction(
                    panelType = results.map { it.test }.first(),
                    date = date,
                    variants = variants,
                    amplifications = amplification,
                    msi = msi,
                    tmb = tmb
                )
            }
    }
}