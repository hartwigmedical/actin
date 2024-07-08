package com.hartwig.actin.molecular.priormoleculartest

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.molecular.MolecularExtractor
import com.hartwig.actin.molecular.datamodel.panel.McgiAmplification
import com.hartwig.actin.molecular.datamodel.panel.McgiExtraction
import com.hartwig.actin.molecular.datamodel.panel.PanelExtraction
import com.hartwig.actin.molecular.datamodel.panel.PanelVariantExtraction

class McgiExtractor : MolecularExtractor<PriorMolecularTest, PanelExtraction> {
    override fun extract(input: List<PriorMolecularTest>): List<PanelExtraction> {
        return input.groupBy { it.measureDate }
            .map { (date, results) ->
                val variants =
                    results.filter { it.scoreText == "variant" }.map { PanelVariantExtraction(it.item!!, it.measure!!) }
                val amplification =
                    results.filter { it.scoreText == "amplification" }.map { McgiAmplification(it.item!!, it.measure!!) }
                val msi = results.filter { it.scoreText == "msi" }.map { it.measure?.toBoolean() }.first()
                val tmb = results.filter { it.scoreText == "tmb" }.map { it.measure?.toDouble() }.first()
                McgiExtraction(date = date, variants = variants, amplifications = amplification, msi = msi!!, tmb = tmb!!)
            }
    }
}