package com.hartwig.actin.algo.soc

import com.hartwig.actin.algo.soc.MolecularDecisions.brafV600EMutation
import com.hartwig.actin.algo.soc.MolecularDecisions.ntrkFusion
import com.hartwig.actin.algo.soc.MolecularDecisions.rasWildTypeAndLeftSided
import com.hartwig.actin.algo.soc.datamodel.DecisionTree
import com.hartwig.actin.algo.soc.datamodel.DecisionTreeLeaf
import com.hartwig.actin.algo.soc.datamodel.DecisionTreeNode
import com.hartwig.actin.datamodel.algo.TreatmentCandidate
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.EligibilityRule

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
        decision = brafV600EMutation,
        trueBranch = DecisionTreeLeaf(listOf(treatmentCandidateDatabase.treatmentCandidate(ENCORAFENIB_CETUXIMAB))),
        falseBranch = DecisionTree(
            decision = EligibilityFunction(
                EligibilityRule.AND,
                rasWildTypeAndLeftSided
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
        decision = ntrkFusion,
        trueBranch = DecisionTreeLeaf(listOf(ENTRECTINIB, LAROTRECTINIB).map(treatmentCandidateDatabase::treatmentCandidate)),
        falseBranch = DecisionTreeLeaf(emptyList())
    )

    private val generallyAvailableTreatmentCandidates = listOf(
        commonChemotherapies.map(treatmentCandidateDatabase::treatmentCandidate),
        commonChemotherapies.map(treatmentCandidateDatabase::treatmentCandidateWithBevacizumab),
        listOf(IRINOTECAN, TRIFLURIDINE_TIPIRACIL, TRIFLURIDINE_TIPIRACIL_BEVACIZUMAB).map(treatmentCandidateDatabase::treatmentCandidate),
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