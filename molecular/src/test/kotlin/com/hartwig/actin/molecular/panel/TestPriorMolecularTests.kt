package com.hartwig.actin.molecular.panel

import com.hartwig.actin.clinical.datamodel.PriorIHCTest
import java.time.LocalDate

const val GENE_UP = "geneUp"
const val GENE_DOWN = "geneDown"
const val GENE = "gene"
const val HGVS_CODING = "c.1A>T"
const val HGVS_PROTEIN = "p.M1L"

fun avlPanelPriorMolecularNoMutationsFoundRecord(): PriorIHCTest {
    return PriorIHCTest(
        test = "AvL Panel",
        item = null,
        measure = "GEEN mutaties aangetoond met behulp van het AVL Panel",
    )
}

fun avlPanelPriorMolecularVariantRecord(): PriorIHCTest {
    return PriorIHCTest(
        test = "AvL Panel",
        item = GENE,
        measure = HGVS_CODING,
    )
}

fun freetextPriorMolecularFusionRecord(): PriorIHCTest {
    return PriorIHCTest(
        test = "Freetext",
        item = "$GENE_UP::$GENE_DOWN",
    )
}

fun ampliseqPriorMolecularVariantRecord(): PriorIHCTest {
    return PriorIHCTest(
        test = "NGS/Ampliseq",
        item = GENE,
        measure = HGVS_CODING,
    )
}

fun freetextPriorMolecularVariantRecord(gene: String, hgvs: String, date: LocalDate? = null): PriorIHCTest {
    return PriorIHCTest(
        test = "Freetext",
        item = gene,
        measure = hgvs,
        measureDate = date,
    )
}

fun freetextPriorMolecularNegativeGeneRecord(gene: String): PriorIHCTest {
    return PriorIHCTest(
        test = "Freetext",
        item = gene,
        scoreText = "Negative",
    )
}

fun archerPriorMolecularVariantRecord(gene: String? = GENE, hgvs: String? = HGVS_CODING, date: LocalDate? = null): PriorIHCTest {
    return PriorIHCTest(
        test = "Archer FP Lung Target",
        item = gene,
        measure = hgvs,
        measureDate = date,
    )
}

fun archerPriorMolecularFusionRecord(gene: String?, date: LocalDate? = null): PriorIHCTest {
    return PriorIHCTest(
        test = "Archer FP Lung Target",
        item = null,
        measure = "$gene fusie aangetoond",
        measureDate = date,
    )
}

fun archerExonSkippingRecord(gene: String, skippingRange: String): PriorIHCTest {
    return PriorIHCTest(
        test = "Archer FP Lung Target",
        item = gene,
        measure = "$gene exon $skippingRange skipping aangetoond",
    )
}
