package com.hartwig.actin.molecular.priormoleculartest

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import java.time.LocalDate

const val GENE_UP = "geneUp"
const val GENE_DOWN = "geneDown"
const val GENE = "gene"
const val HGVS = "c.1A>T"

fun avlPanelPriorMolecularNoMutationsFoundRecord(): PriorMolecularTest {
    return PriorMolecularTest(
        test = "AvL Panel",
        item = null,
        measure = "GEEN mutaties aangetoond met behulp van het AVL Panel",
        impliesPotentialIndeterminateStatus = false
    )
}

fun avlPanelPriorMolecularVariantRecord(): PriorMolecularTest {
    return PriorMolecularTest(
        test = "AvL Panel",
        item = GENE,
        measure = HGVS,
        impliesPotentialIndeterminateStatus = false
    )
}

fun freetextPriorMolecularFusionRecord(): PriorMolecularTest {
    return PriorMolecularTest(
        test = "Freetext",
        item = "$GENE_UP::$GENE_DOWN",
        impliesPotentialIndeterminateStatus = false
    )
}

fun archerPriorMolecularVariantRecord(gene: String? = GENE, hgvs: String? = HGVS, date: LocalDate? = null): PriorMolecularTest {
    return PriorMolecularTest(
        test = "Archer FP Lung Target",
        item = gene,
        measure = hgvs,
        measureDate = date,
        impliesPotentialIndeterminateStatus = false
    )
}

fun archerPriorMolecularFusionRecord(gene: String?, date: LocalDate? = null): PriorMolecularTest {
    return PriorMolecularTest(
        test = "Archer FP Lung Target",
        item = gene,
        measure = "$gene fusie aangetoond",
        measureDate = date,
        impliesPotentialIndeterminateStatus = false
    )
}

fun archerExonSkippingRecord(gene: String, skippingRange: String): PriorMolecularTest {
    return PriorMolecularTest(
        test = "Archer FP Lung Target",
        item = gene,
        measure = "$gene exon $skippingRange skipping aangetoond",
        impliesPotentialIndeterminateStatus = false
    )
}
