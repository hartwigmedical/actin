package com.hartwig.actin.algo.soc

import com.hartwig.actin.algo.soc.datamodel.DecisionTree
import com.hartwig.actin.algo.soc.datamodel.DecisionTreeLeaf
import com.hartwig.actin.algo.soc.datamodel.DecisionTreeNode
import com.hartwig.actin.algo.soc.datamodel.TreatmentCandidate
import com.hartwig.actin.trial.datamodel.EligibilityFunction
import com.hartwig.actin.trial.datamodel.EligibilityRule

class CrcDecisionTree(treatmentCandidateDatabase: TreatmentCandidateDatabase) : DecisionTreeNode {

    private val treatmentCandidatesForRasWtBrafWtAndLeftSidedTumor = listOf(
        CETUXIMAB,
        PANITUMUMAB,
        FOLFOX_CETUXIMAB,
        FOLFOX_PANITUMUMAB,
        FOLFIRI_CETUXIMAB,
        FOLFIRI_PANITUMUMAB,
        IRINOTECAN_CETUXIMAB,
        IRINOTECAN_PANITUMUMAB
    ).map(treatmentCandidateDatabase::treatmentCandidate)

    private val primaryDecisionTree = DecisionTree(
        decision = EligibilityFunction(EligibilityRule.MUTATION_IN_GENE_X_OF_ANY_PROTEIN_IMPACTS_Y, listOf("BRAF", "V600E")),
        trueBranch = DecisionTreeLeaf(listOf(treatmentCandidateDatabase.treatmentCandidate(ENCORAFENIB_CETUXIMAB))),
        falseBranch = DecisionTree(
            decision = EligibilityFunction(
                EligibilityRule.AND,
                listOf("KRAS", "NRAS", "HRAS", "BRAF").map { EligibilityFunction(EligibilityRule.WILDTYPE_OF_GENE_X, listOf(it)) }
                        + EligibilityFunction(EligibilityRule.HAS_LEFT_SIDED_COLORECTAL_TUMOR)
            ),
            trueBranch = DecisionTreeLeaf(treatmentCandidatesForRasWtBrafWtAndLeftSidedTumor),
            falseBranch = DecisionTreeLeaf(listOf())
        )
    )

    private val msiDecisionTree = DecisionTree(
        decision = EligibilityFunction(EligibilityRule.MSI_SIGNATURE),
        trueBranch = DecisionTreeLeaf(listOf(PEMBROLIZUMAB, NIVOLUMAB).map(treatmentCandidateDatabase::treatmentCandidate)),
        falseBranch = DecisionTreeLeaf(emptyList())
    )

    private val generallyAvailableTreatmentCandidates = listOf(
        commonChemotherapies.map(treatmentCandidateDatabase::treatmentCandidate),
        commonChemotherapies.map(treatmentCandidateDatabase::treatmentCandidateWithBevacizumab),
        listOf(IRINOTECAN, LONSURF).map(treatmentCandidateDatabase::treatmentCandidate)
    ).flatten()

    override fun treatmentCandidates(): List<TreatmentCandidate> {
        return listOf(primaryDecisionTree, msiDecisionTree).flatMap(DecisionTreeNode::treatmentCandidates) +
                generallyAvailableTreatmentCandidates
    }

    companion object {
        val commonChemotherapies = listOf(
            CAPECITABINE, CAPIRI, CAPOX, FOLFIRI, FOLFOXIRI, FOLFOX, FLUOROURACIL
        )
    }
}