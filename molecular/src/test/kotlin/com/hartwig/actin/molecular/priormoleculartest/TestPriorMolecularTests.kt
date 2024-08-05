package com.hartwig.actin.molecular.priormoleculartest

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
        impliesPotentialIndeterminateStatus = false
    )
}

fun avlPanelPriorMolecularVariantRecord(): PriorIHCTest {
    return PriorIHCTest(
        test = "AvL Panel",
        item = GENE,
        measure = HGVS_CODING,
        impliesPotentialIndeterminateStatus = false
    )
}

fun freetextPriorMolecularFusionRecord(): PriorIHCTest {
    return PriorIHCTest(
        test = "Freetext",
        item = "$GENE_UP::$GENE_DOWN",
        impliesPotentialIndeterminateStatus = false
    )
}

fun ampliseqPriorMolecularFusionRecord(): PriorIHCTest {
    return PriorIHCTest(
        test = "NGS/Ampliseq",
        item = GENE,
        measure = HGVS_CODING,
        impliesPotentialIndeterminateStatus = false
    )
}

fun freetextPriorMolecularVariantRecord(gene: String, hgvs: String, date: LocalDate? = null): PriorIHCTest {
    return PriorIHCTest(
        test = "Freetext",
        item = gene,
        measure = hgvs,
        measureDate = date,
        impliesPotentialIndeterminateStatus = false
    )
}

fun freetextPriorMolecularNegativeGeneRecord(gene: String): PriorIHCTest {
    return PriorIHCTest(
        test = "Freetext",
        item = gene,
        scoreText = "Negative",
        impliesPotentialIndeterminateStatus = false
    )
}

fun archerPriorMolecularVariantRecord(gene: String? = GENE, hgvs: String? = HGVS_CODING, date: LocalDate? = null): PriorIHCTest {
    return PriorIHCTest(
        test = "Archer FP Lung Target",
        item = gene,
        measure = hgvs,
        measureDate = date,
        impliesPotentialIndeterminateStatus = false
    )
}

fun archerPriorMolecularFusionRecord(gene: String?, date: LocalDate? = null): PriorIHCTest {
    return PriorIHCTest(
        test = "Archer FP Lung Target",
        item = null,
        measure = "$gene fusie aangetoond",
        measureDate = date,
        impliesPotentialIndeterminateStatus = false
    )
}

fun archerExonSkippingRecord(gene: String, skippingRange: String): PriorIHCTest {
    return PriorIHCTest(
        test = "Archer FP Lung Target",
        item = gene,
        measure = "$gene exon $skippingRange skipping aangetoond",
        impliesPotentialIndeterminateStatus = false
    )
}
