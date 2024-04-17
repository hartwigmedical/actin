package com.hartwig.actin.algo.soc

import com.hartwig.actin.algo.datamodel.TreatmentCandidate
import com.hartwig.actin.algo.soc.datamodel.DecisionTree
import com.hartwig.actin.algo.soc.datamodel.DecisionTreeLeaf
import com.hartwig.actin.algo.soc.datamodel.DecisionTreeNode
import com.hartwig.actin.trial.datamodel.EligibilityFunction
import com.hartwig.actin.trial.datamodel.EligibilityRule

class CrcDecisionTree(treatmentCandidateDatabase: TreatmentCandidateDatabase) : DecisionTreeNode {

    private val treatmentCandidatesForRasWtBrafV600EWtAndLeftSidedTumor = listOf(
        CETUXIMAB,
        PANITUMUMAB,
        FOLFOX_CETUXIMAB,
        FOLFOX_PANITUMUMAB,
        FOLFIRI_CETUXIMAB,
        FOLFIRI_PANITUMUMAB,
        IRINOTECAN_CETUXIMAB,
        IRINOTECAN_PANITUMUMAB
    ).map(treatmentCandidateDatabase::treatmentCandidate)

    private val molecularDriverDecisionTree = DecisionTree(
        decision = EligibilityFunction(EligibilityRule.MUTATION_IN_GENE_X_OF_ANY_PROTEIN_IMPACTS_Y, listOf("BRAF", "V600E")),
        trueBranch = DecisionTreeLeaf(listOf(treatmentCandidateDatabase.treatmentCandidate(ENCORAFENIB_CETUXIMAB))),
        falseBranch = DecisionTree(
            decision = EligibilityFunction(
                EligibilityRule.AND,
                listOf("KRAS", "NRAS").map { EligibilityFunction(EligibilityRule.WILDTYPE_OF_GENE_X, listOf(it)) }
                        + EligibilityFunction(EligibilityRule.HAS_LEFT_SIDED_COLORECTAL_TUMOR)
            ),
            trueBranch = DecisionTreeLeaf(treatmentCandidatesForRasWtBrafV600EWtAndLeftSidedTumor),
            falseBranch = DecisionTreeLeaf(emptyList())
        )
    )

    private val msiDecisionTree = DecisionTree(
        decision = EligibilityFunction(EligibilityRule.MSI_SIGNATURE),
        trueBranch = DecisionTreeLeaf(listOf(PEMBROLIZUMAB, NIVOLUMAB).map(treatmentCandidateDatabase::treatmentCandidate)),
        falseBranch = DecisionTreeLeaf(emptyList())
    )

    private val socExhaustedTree = DecisionTree(
        decision = EligibilityFunction(EligibilityRule.OR, listOf("NTRK1", "NTRK2", "NTRK3").map {
            EligibilityFunction(EligibilityRule.FUSION_IN_GENE_X, listOf(it))
        }),
        trueBranch = DecisionTreeLeaf(listOf(ENTRECTINIB, LAROTRECTINIB).map(treatmentCandidateDatabase::treatmentCandidate)),
        falseBranch = DecisionTreeLeaf(emptyList())
    )

    private val generallyAvailableTreatmentCandidates = listOf(
        commonChemotherapies.map(treatmentCandidateDatabase::treatmentCandidate),
        commonChemotherapies.map(treatmentCandidateDatabase::treatmentCandidateWithBevacizumab),
        listOf(IRINOTECAN, TRIFLURIDINE_TIPIRACIL).map(treatmentCandidateDatabase::treatmentCandidate)
    ).flatten()

    override fun treatmentCandidates(): List<TreatmentCandidate> {
        return listOf(molecularDriverDecisionTree, msiDecisionTree, socExhaustedTree).flatMap(DecisionTreeNode::treatmentCandidates) +
                generallyAvailableTreatmentCandidates
    }

    companion object {
        val commonChemotherapies = listOf(
            CAPECITABINE, CAPIRI, CAPOX, FOLFIRI, FOLFOXIRI, FOLFOX, FLUOROURACIL
        )
    }
}