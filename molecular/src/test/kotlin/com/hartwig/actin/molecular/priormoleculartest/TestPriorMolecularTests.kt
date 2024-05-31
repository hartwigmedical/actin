package com.hartwig.actin.molecular.priormoleculartest

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import java.time.LocalDate

const val GENE_UP = "geneUp"
const val GENE_DOWN = "geneDown"
const val GENE = "gene"
const val HGVS_CODING = "c.1A>T"
const val HGVS_PROTEIN = "p.M1L"

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
        measure = HGVS_CODING,
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

fun freetextPriorMolecularVariantRecord(gene: String, hgvs: String, date: LocalDate? = null): PriorMolecularTest {
    return PriorMolecularTest(
        test = "Freetext",
        item = gene,
        measure = hgvs,
        measureDate = date,
        impliesPotentialIndeterminateStatus = false
    )
}

fun archerPriorMolecularVariantRecord(gene: String? = GENE, hgvs: String? = HGVS_CODING, date: LocalDate? = null): PriorMolecularTest {
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
        item = null,
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
