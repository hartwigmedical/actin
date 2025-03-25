package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.datamodel.PatientRecord
import java.time.LocalDate

object IHCMessagesFunctions {

    fun additionalMessageWhenGeneIsWildType(gene: String, record: PatientRecord, maxTestAge: LocalDate?): String {
        val geneIsWildType = MolecularRuleEvaluator.geneIsWildTypeForPatient(gene, record, maxTestAge)
        return if (geneIsWildType) " though $gene is wild-type in recent molecular test" else ""
    }
}