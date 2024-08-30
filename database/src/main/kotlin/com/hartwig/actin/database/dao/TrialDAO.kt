package com.hartwig.actin.database.dao

import com.hartwig.actin.database.Tables
import com.hartwig.actin.datamodel.trial.Cohort
import com.hartwig.actin.datamodel.trial.CohortMetadata
import com.hartwig.actin.datamodel.trial.CriterionReference
import com.hartwig.actin.datamodel.trial.Eligibility
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.Trial
import com.hartwig.actin.datamodel.trial.TrialIdentification
import com.hartwig.actin.trial.input.composite.CompositeRules
import com.hartwig.actin.trial.util.EligibilityFunctionDisplay
import org.jooq.DSLContext

internal class TrialDAO(private val context: DSLContext) {

    fun clear() {
        context.execute("SET FOREIGN_KEY_CHECKS = 0;")
        context.truncate(Tables.TRIAL).execute()
        context.truncate(Tables.COHORT).execute()
        context.truncate(Tables.ELIGIBILITY).execute()
        context.truncate(Tables.REFERENCE).execute()
        context.truncate(Tables.ELIGIBILITYREFERENCE).execute()
        context.execute("SET FOREIGN_KEY_CHECKS = 1;")
    }

    fun writeTrial(trial: Trial) {
        val trialId = writeTrialIdentification(trial.identification)
        val referenceIdMap = writeReferences(trial)
        writeEligibilities(trialId, null, trial.generalEligibility, referenceIdMap)
        for (cohort in trial.cohorts) {
            val cohortId = writeCohortMetadata(trialId, cohort.metadata)
            writeEligibilities(trialId, cohortId, cohort.eligibility, referenceIdMap)
        }
    }

    private fun writeTrialIdentification(identification: TrialIdentification): Int {
        return context.insertInto(Tables.TRIAL, Tables.TRIAL.CODE, Tables.TRIAL.OPEN, Tables.TRIAL.ACRONYM, Tables.TRIAL.TITLE)
            .values(identification.trialId, identification.open, identification.acronym, identification.title)
            .returning(Tables.TRIAL.ID)
            .fetchOne()!!
            .getValue(Tables.TRIAL.ID)
    }

    private fun writeReferences(trial: Trial): Map<CriterionReference, Int> {
        return listOf(trial.generalEligibility, trial.cohorts.flatMap(Cohort::eligibility))
            .flatMap { it.flatMap(Eligibility::references) }
            .distinct()
            .associateWith { reference ->
                context.insertInto(Tables.REFERENCE, Tables.REFERENCE.CODE, Tables.REFERENCE.TEXT)
                    .values(reference.id, reference.text)
                    .returning(Tables.REFERENCE.ID)
                    .fetchOne()!!
                    .getValue(Tables.REFERENCE.ID)
            }
    }

    private fun writeCohortMetadata(trialId: Int, metadata: CohortMetadata): Int {
        return context.insertInto(
            Tables.COHORT,
            Tables.COHORT.TRIALID,
            Tables.COHORT.CODE,
            Tables.COHORT.EVALUABLE,
            Tables.COHORT.OPEN,
            Tables.COHORT.SLOTSAVAILABLE,
            Tables.COHORT.BLACKLIST,
            Tables.COHORT.DESCRIPTION
        )
            .values(
                trialId,
                metadata.cohortId,
                metadata.evaluable,
                metadata.open,
                metadata.slotsAvailable,
                metadata.blacklist,
                metadata.description
            )
            .returning(Tables.COHORT.ID)
            .fetchOne()!!
            .getValue(Tables.COHORT.ID)
    }

    private fun writeEligibilities(
        trialId: Int, cohortId: Int?, eligibilities: List<Eligibility>,
        referenceIdMap: Map<CriterionReference, Int>
    ) {
        for (eligibility in eligibilities) {
            val id = writeEligibilityFunction(trialId, cohortId, null, eligibility.function)
            for (reference in eligibility.references) {
                context.insertInto(
                    Tables.ELIGIBILITYREFERENCE,
                    Tables.ELIGIBILITYREFERENCE.ELIGIBILITYID,
                    Tables.ELIGIBILITYREFERENCE.REFERENCEID
                )
                    .values(id, referenceIdMap[reference])
                    .execute()
            }
        }
    }

    private fun writeEligibilityFunction(
        trialId: Int, cohortId: Int?, parentId: Int?,
        function: EligibilityFunction
    ): Int {
        val isComposite = CompositeRules.isComposite(function.rule)
        val parameters = if (isComposite) "" else DataUtil.concatObjects(function.parameters) ?: ""
        val id = context.insertInto(
            Tables.ELIGIBILITY,
            Tables.ELIGIBILITY.TRIALID,
            Tables.ELIGIBILITY.COHORTID,
            Tables.ELIGIBILITY.PARENTID,
            Tables.ELIGIBILITY.RULE,
            Tables.ELIGIBILITY.PARAMETERS,
            Tables.ELIGIBILITY.DISPLAY
        )
            .values(trialId, cohortId, parentId, function.rule.toString(), parameters, EligibilityFunctionDisplay.format(function))
            .returning(Tables.ELIGIBILITY.ID)
            .fetchOne()!!
            .getValue(Tables.ELIGIBILITY.ID)
        if (isComposite) {
            for (input in function.parameters) {
                writeEligibilityFunction(trialId, cohortId, id, input as EligibilityFunction)
            }
        }
        return id
    }
}