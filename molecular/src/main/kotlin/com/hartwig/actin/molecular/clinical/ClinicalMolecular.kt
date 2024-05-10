package com.hartwig.actin.molecular.clinical

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.molecular.MolecularAnnotator
import com.hartwig.actin.molecular.MolecularInterpreter
import com.hartwig.actin.molecular.MolecularPipeline
import com.hartwig.actin.molecular.datamodel.ARCHER_FP_LUNG_TARGET
import com.hartwig.actin.molecular.datamodel.AVL_PANEL
import com.hartwig.actin.molecular.datamodel.FREE_TEXT_PANEL
import com.hartwig.actin.molecular.datamodel.IHCMolecularTest
import com.hartwig.actin.molecular.datamodel.MolecularTest
import com.hartwig.actin.molecular.datamodel.OtherPriorMolecularTest
import com.hartwig.actin.molecular.evidence.EvidenceDatabase

private fun identityAnnotator() = object : MolecularAnnotator<MolecularTest> {
    override fun annotate(input: MolecularTest): MolecularTest {
        return input
    }
}

private fun otherInterpreter() = object : MolecularInterpreter<PriorMolecularTest, MolecularTest> {
    override fun interpret(input: List<PriorMolecularTest>): List<MolecularTest> {
        return input.map { OtherPriorMolecularTest(it) }
    }
}

private fun ihcInterpreter() = object : MolecularInterpreter<PriorMolecularTest, MolecularTest> {
    override fun interpret(input: List<PriorMolecularTest>): List<MolecularTest> {
        return input.map { IHCMolecularTest(it) }
    }
}

class ClinicalMolecular(private val pipelines: Set<MolecularPipeline<PriorMolecularTest, out MolecularTest>>) {

    fun process(clinicalTests: List<PriorMolecularTest>): List<MolecularTest> {
        val otherPipeline = MolecularPipeline(
            otherInterpreter(),
            identityAnnotator()
        ) { test -> pipelines.none { it.inputPredicate.invoke(test) } }
        return (pipelines + otherPipeline).flatMap { it.run(clinicalTests) }
    }

    companion object {
        fun create(evidenceDatabase: EvidenceDatabase) = ClinicalMolecular(
            setOf(
                MolecularPipeline(
                    ArcherInterpreter(),
                    ArcherAnnotator(evidenceDatabase),
                    isArcher()
                ),
                MolecularPipeline(
                    GenericPanelInterpreter(),
                    GenericPanelAnnotator(),
                    isGeneric()
                ),
                MolecularPipeline(
                    ihcInterpreter(),
                    identityAnnotator(),
                    isIHC()
                )
            )
        )

        private fun isIHC(): (PriorMolecularTest) -> Boolean {
            return { it.test == "IHC" || it.item == "PD-L1" }
        }

        private fun isGeneric(): (PriorMolecularTest) -> Boolean =
            { it.test == AVL_PANEL || it.test == FREE_TEXT_PANEL }

        private fun isArcher(): (PriorMolecularTest) -> Boolean = { it.test == ARCHER_FP_LUNG_TARGET }
    }
}