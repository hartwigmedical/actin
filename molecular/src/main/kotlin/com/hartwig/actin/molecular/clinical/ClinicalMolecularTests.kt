package com.hartwig.actin.molecular.clinical

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.molecular.MolecularAnnotator
import com.hartwig.actin.molecular.MolecularInterpreter
import com.hartwig.actin.molecular.MolecularPipeline
import com.hartwig.actin.molecular.datamodel.IHCMolecularTest
import com.hartwig.actin.molecular.datamodel.MolecularTest
import com.hartwig.actin.molecular.datamodel.OtherPriorMolecularTest
import com.hartwig.actin.molecular.evidence.EvidenceDatabase

const val ARCHER_FP_LUNG_TARGET = "Archer FP Lung Target"
const val AVL_PANEL = "AvL Panel"
const val FREE_TEXT_PANEL = "Freetext"

private fun identityAnnotator() = object : MolecularAnnotator<PriorMolecularTest> {
    override fun annotate(input: MolecularTest<PriorMolecularTest>): MolecularTest<PriorMolecularTest> {
        return input
    }
}

private fun otherInterpreter() = object : MolecularInterpreter<PriorMolecularTest, PriorMolecularTest> {
    override fun interpret(input: List<PriorMolecularTest>): List<MolecularTest<PriorMolecularTest>> {
        return input.map { OtherPriorMolecularTest(it.measureDate, it) }
    }
}

private fun ihcInterpreter() = object : MolecularInterpreter<PriorMolecularTest, PriorMolecularTest> {
    override fun interpret(input: List<PriorMolecularTest>): List<MolecularTest<PriorMolecularTest>> {
        return input.map { IHCMolecularTest(it.measureDate, it) }
    }
}

class ClinicalMolecularTests(private val pipelines: Set<MolecularPipeline<PriorMolecularTest, out Any>>) {

    fun process(clinicalTests: List<PriorMolecularTest>): List<MolecularTest<out Any>> {
        val otherPipeline = MolecularPipeline(
            otherInterpreter(),
            identityAnnotator()
        ) { test -> pipelines.none { it.inputPredicate.invoke(test) } }
        return (pipelines + otherPipeline).flatMap { it.run(clinicalTests) }
    }

    companion object {
        fun create(evidenceDatabase: EvidenceDatabase) = ClinicalMolecularTests(
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