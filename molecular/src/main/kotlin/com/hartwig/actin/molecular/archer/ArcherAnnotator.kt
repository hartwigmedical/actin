package com.hartwig.actin.molecular.archer

import com.hartwig.actin.molecular.MolecularAnnotator
import com.hartwig.actin.molecular.datamodel.ArcherMolecularTest
import com.hartwig.actin.molecular.datamodel.MolecularTest
import com.hartwig.actin.molecular.datamodel.driver.CodingEffect
import com.hartwig.actin.molecular.datamodel.driver.FusionDriverType
import com.hartwig.actin.molecular.datamodel.driver.VariantType
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherPanel
import com.hartwig.actin.molecular.evidence.EvidenceDatabase
import com.hartwig.actin.molecular.evidence.matching.FusionMatchCriteria
import com.hartwig.actin.molecular.evidence.matching.VariantMatchCriteria
import com.hartwig.actin.molecular.orange.interpretation.ActionableEvidenceFactory
import com.hartwig.actin.molecular.orange.interpretation.GeneAlterationFactory

class ArcherAnnotator(private val evidenceDatabase: EvidenceDatabase) : MolecularAnnotator<ArcherPanel> {
    override fun annotate(input: MolecularTest<ArcherPanel>): MolecularTest<ArcherPanel> {
        val annotatedVariants = input.result.variants.map {
            val criteria = VariantMatchCriteria(
                true,
                it.gene,
                CodingEffect.MISSENSE,
                VariantType.SNV,
                "1",
                123,
                "C",
                "T"
            )
            val evidence = ActionableEvidenceFactory.create(
                evidenceDatabase.evidenceForVariant(
                    criteria
                )
            )
            val geneAlteration = GeneAlterationFactory.convertAlteration(
                it.gene, evidenceDatabase.geneAlterationForVariant(criteria)
            )
            it.copy(evidence = evidence, geneAlteration = geneAlteration)
        }

        val annotatedFusions = input.result.fusions.map {
            val criteria =
                FusionMatchCriteria(geneStart = it.gene, geneEnd = it.gene, driverType = FusionDriverType.NONE, isReportable = true)
            val evidence = ActionableEvidenceFactory.create(
                evidenceDatabase.evidenceForFusion(
                    criteria
                )
            )
            it.copy(evidence = evidence)
        }

        return ArcherMolecularTest(input.date, input.result.copy(variants = annotatedVariants, fusions = annotatedFusions))
    }
}