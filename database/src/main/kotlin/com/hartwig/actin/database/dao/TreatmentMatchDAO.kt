package com.hartwig.actin.database.dao

import com.hartwig.actin.database.Tables
import com.hartwig.actin.datamodel.algo.CohortMatch
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.TreatmentMatch
import com.hartwig.actin.datamodel.algo.TrialMatch
import com.hartwig.actin.datamodel.trial.Eligibility
import com.hartwig.actin.trial.util.EligibilityFunctionDisplay
import org.jooq.DSLContext
import org.jooq.Table
import org.jooq.TableRecord
import org.jooq.impl.DSL

class TreatmentMatchDAO(private val context: DSLContext) {

    fun clear(treatmentMatch: TreatmentMatch) {
        val patientId = treatmentMatch.patientId
        val treatmentMatchResults =
            context.select(Tables.TREATMENTMATCH.ID).from(Tables.TREATMENTMATCH).where(Tables.TREATMENTMATCH.PATIENTID.eq(patientId))
                .fetch()
        for (treatmentMatchResult in treatmentMatchResults) {
            val treatmentMatchId = treatmentMatchResult.getValue(Tables.TREATMENTMATCH.ID)
            val trialResults =
                context.select(Tables.TRIALMATCH.ID).from(Tables.TRIALMATCH).where(Tables.TRIALMATCH.TREATMENTMATCHID.eq(treatmentMatchId))
                    .fetch()
            for (trialResult in trialResults) {
                val trialMatchId = trialResult.getValue(Tables.TRIALMATCH.ID)
                context.delete(Tables.COHORTMATCH).where(Tables.COHORTMATCH.TRIALMATCHID.eq(trialMatchId)).execute()
                context.delete(Tables.EVALUATION).where(Tables.EVALUATION.TRIALMATCHID.eq(trialMatchId)).execute()
            }
            context.delete(Tables.TRIALMATCH).where(Tables.TRIALMATCH.TREATMENTMATCHID.eq(treatmentMatchId)).execute()
        }
        context.delete(Tables.TREATMENTMATCH).where(Tables.TREATMENTMATCH.PATIENTID.eq(patientId)).execute()
    }

    fun writeTreatmentMatch(treatmentMatch: TreatmentMatch) {
        val treatmentMatchId = context.insertInto(
            Tables.TREATMENTMATCH,
            Tables.TREATMENTMATCH.PATIENTID,
            Tables.TREATMENTMATCH.SAMPLEID,
            Tables.TREATMENTMATCH.REFERENCEDATE,
            Tables.TREATMENTMATCH.REFERENCEDATEISLIVE
        )
            .values(
                treatmentMatch.patientId,
                treatmentMatch.sampleId,
                treatmentMatch.referenceDate,
                treatmentMatch.referenceDateIsLive
            )
            .returning(Tables.TREATMENTMATCH.ID)
            .fetchOne()!!
            .getValue(Tables.TREATMENTMATCH.ID)
        for (trialMatch in treatmentMatch.trialMatches) {
            val trialMatchId = writeTrialMatch(treatmentMatchId, trialMatch)
            writeEvaluations(trialMatchId, null, trialMatch.evaluations)
            for (cohortMatch in trialMatch.cohorts) {
                val cohortMatchId = writeCohortMatch(trialMatchId, cohortMatch)
                writeEvaluations(trialMatchId, cohortMatchId, cohortMatch.evaluations)
            }
        }
    }

    private fun writeTrialMatch(treatmentMatchId: Int, trialMatch: TrialMatch): Int {
        return context.insertInto(
            Tables.TRIALMATCH,
            Tables.TRIALMATCH.TREATMENTMATCHID,
            Tables.TRIALMATCH.CODE,
            Tables.TRIALMATCH.OPEN,
            Tables.TRIALMATCH.ACRONYM,
            Tables.TRIALMATCH.TITLE,
            Tables.TRIALMATCH.ISELIGIBLE
        )
            .values(
                treatmentMatchId,
                trialMatch.identification.nctId,
                trialMatch.identification.open,
                trialMatch.identification.acronym,
                trialMatch.identification.title,
                trialMatch.isPotentiallyEligible
            )
            .returning(Tables.TRIALMATCH.ID)
            .fetchOne()!!
            .getValue(Tables.TRIALMATCH.ID)
    }

    private fun writeCohortMatch(trialMatchId: Int, cohortMatch: CohortMatch): Int {
        return context.insertInto(
            Tables.COHORTMATCH,
            Tables.COHORTMATCH.TRIALMATCHID,
            Tables.COHORTMATCH.CODE,
            Tables.COHORTMATCH.EVALUABLE,
            Tables.COHORTMATCH.OPEN,
            Tables.COHORTMATCH.SLOTSAVAILABLE,
            Tables.COHORTMATCH.IGNORE,
            Tables.COHORTMATCH.DESCRIPTION,
            Tables.TRIALMATCH.ISELIGIBLE
        )
            .values(
                trialMatchId,
                cohortMatch.metadata.cohortId,
                cohortMatch.metadata.evaluable,
                cohortMatch.metadata.open,
                cohortMatch.metadata.slotsAvailable,
                cohortMatch.metadata.ignore,
                cohortMatch.metadata.description,
                cohortMatch.isPotentiallyEligible
            )
            .returning(Tables.COHORTMATCH.ID)
            .fetchOne()!!
            .getValue(Tables.COHORTMATCH.ID)
    }

    private fun writeEvaluations(trialMatchId: Int, cohortMatchId: Int?, evaluations: Map<Eligibility, Evaluation>) {
        for ((key, evaluation) in evaluations) {
            val eligibility = EligibilityFunctionDisplay.format(key.function)
            context.insertInto(
                Tables.EVALUATION,
                Tables.EVALUATION.TRIALMATCHID,
                Tables.EVALUATION.COHORTMATCHID,
                Tables.EVALUATION.ELIGIBILITY,
                Tables.EVALUATION.RESULT,
                Tables.EVALUATION.RECOVERABLE,
                Tables.EVALUATION.INCLUSIONMOLECULAREVENTS,
                Tables.EVALUATION.EXCLUSIONMOLECULAREVENTS,
                Tables.EVALUATION.PASSSPECIFICMESSAGES,
                Tables.EVALUATION.PASSGENERALMESSAGES,
                Tables.EVALUATION.WARNSPECIFICMESSAGES,
                Tables.EVALUATION.WARNGENERALMESSAGES,
                Tables.EVALUATION.UNDETERMINEDSPECIFICMESSAGES,
                Tables.EVALUATION.UNDETERMINEDGENERALMESSAGES,
                Tables.EVALUATION.FAILSPECIFICMESSAGES,
                Tables.EVALUATION.FAILGENERALMESSAGES
            )
                .values(
                    trialMatchId,
                    cohortMatchId,
                    eligibility,
                    evaluation.result.toString(),
                    evaluation.recoverable,
                    DataUtil.concat(evaluation.inclusionMolecularEvents),
                    DataUtil.concat(evaluation.exclusionMolecularEvents),
                    DataUtil.concat(evaluation.passSpecificMessages),
                    DataUtil.concat(evaluation.passGeneralMessages),
                    DataUtil.concat(evaluation.warnSpecificMessages),
                    DataUtil.concat(evaluation.warnGeneralMessages),
                    DataUtil.concat(evaluation.undeterminedSpecificMessages),
                    DataUtil.concat(evaluation.undeterminedGeneralMessages),
                    DataUtil.concat(evaluation.failSpecificMessages),
                    DataUtil.concat(evaluation.failGeneralMessages)
                )
                .execute()
        }
    }

    fun clearAllMatches() {
        context.transaction { configuration ->
            val transactionContext = DSL.using(configuration)
            sequenceOf(
                Tables.TREATMENTMATCH,
                Tables.TRIALMATCH,
                Tables.COHORTMATCH,
                Tables.EVALUATION
            ).forEach { transactionContext.truncate(it).execute() }
        }
    }

    fun insertAllMatches(matches: Iterable<TreatmentMatch>) {
        context.transaction { configuration ->
            val transactionContext = DSL.using(configuration)
            val indexedRecords = insertTreatmentMatches(transactionContext, matches)

            val indexedTrialMatches = writeRecordsAndReturnIndexedList(
                transactionContext, indexedRecords, Tables.TRIALMATCH, ::trialMatchesFromTreatmentMatch
            )
            val indexedCohortMatches = writeCohortMatchesAndReturnDoublyIndexedList(transactionContext, indexedTrialMatches)
            writeEvaluations(transactionContext, indexedCohortMatches)
        }
    }

    private fun insertTreatmentMatches(transactionContext: DSLContext, matches: Iterable<TreatmentMatch>): List<Pair<Int, TreatmentMatch>> {
        val (indexedRecords, rows) = matches.mapIndexed { index, record ->
            val treatmentMatchId = index + 1
            val dbRecord = transactionContext.newRecord(Tables.TREATMENTMATCH)
            dbRecord.from(record)
            dbRecord.set(Tables.TREATMENTMATCH.ID, treatmentMatchId)
            Pair(treatmentMatchId, record) to dbRecord
        }.unzip()

        transactionContext.batchInsert(rows).execute()
        return indexedRecords
    }

    private fun <T, U : TableRecord<*>, V> writeRecordsAndReturnIndexedList(
        transactionContext: DSLContext,
        indexedRecords: List<Pair<Int, T>>,
        table: Table<U>,
        recordMapper: (DSLContext, Int, T) -> List<Pair<V, U>>
    ): List<Pair<Int, V>> {
        val (outputEntries, rows) = indexedRecords.flatMap { (foreignKeyId, record) ->
            recordMapper(transactionContext, foreignKeyId, record)
        }
            .mapIndexed { index, (outputEntry: V, dbRecord: U) ->
                val id = index + 1
                dbRecord.set(table.field("id", Int::class.java), id)
                Pair(id, outputEntry) to dbRecord
            }
            .unzip()

        transactionContext.batchInsert(rows).execute()
        return outputEntries
    }

    private fun trialMatchesFromTreatmentMatch(transactionContext: DSLContext, treatmentMatchId: Int, record: TreatmentMatch) =
        record.trialMatches.map { trialMatch ->
            val dbRecord = transactionContext.newRecord(Tables.TRIALMATCH)
            dbRecord.from(trialMatch.identification)
            dbRecord.set(Tables.TRIALMATCH.TREATMENTMATCHID, treatmentMatchId)
            dbRecord.set(Tables.TRIALMATCH.CODE, trialMatch.identification.nctId)
            dbRecord.set(Tables.TRIALMATCH.ISELIGIBLE, trialMatch.isPotentiallyEligible)
            trialMatch to dbRecord
        }

    private fun cohortMatchesFromTrialMatch(transactionContext: DSLContext, trialMatchId: Int, trialMatch: TrialMatch) =
        trialMatch.cohorts.map { cohortMatch ->
            val dbRecord = transactionContext.newRecord(Tables.COHORTMATCH)
            dbRecord.from(cohortMatch.metadata)
            dbRecord.set(Tables.COHORTMATCH.TRIALMATCHID, trialMatchId)
            dbRecord.set(Tables.COHORTMATCH.CODE, cohortMatch.metadata.cohortId)
            dbRecord.set(Tables.COHORTMATCH.ISELIGIBLE, cohortMatch.isPotentiallyEligible)
            cohortMatch to dbRecord
        }

    private fun evaluationsFromCohortMatch(
        transactionContext: DSLContext,
        trialMatchId: Int,
        cohortMatchId: Int,
        cohortMatch: CohortMatch
    ) =
        cohortMatch.evaluations.map { (eligibility, evaluation) ->
            val dbRecord = transactionContext.newRecord(Tables.EVALUATION)
            dbRecord.set(Tables.EVALUATION.TRIALMATCHID, trialMatchId)
            dbRecord.set(Tables.EVALUATION.COHORTMATCHID, cohortMatchId)
            dbRecord.set(Tables.EVALUATION.ELIGIBILITY, EligibilityFunctionDisplay.format(eligibility.function))
            dbRecord.set(Tables.EVALUATION.RESULT, evaluation.result.toString())
            dbRecord.set(Tables.EVALUATION.RECOVERABLE, evaluation.recoverable)

            sequenceOf(
                Tables.EVALUATION.INCLUSIONMOLECULAREVENTS to evaluation.inclusionMolecularEvents,
                Tables.EVALUATION.EXCLUSIONMOLECULAREVENTS to evaluation.exclusionMolecularEvents,
                Tables.EVALUATION.PASSSPECIFICMESSAGES to evaluation.passSpecificMessages,
                Tables.EVALUATION.PASSGENERALMESSAGES to evaluation.passGeneralMessages,
                Tables.EVALUATION.WARNSPECIFICMESSAGES to evaluation.warnSpecificMessages,
                Tables.EVALUATION.WARNGENERALMESSAGES to evaluation.warnGeneralMessages,
                Tables.EVALUATION.UNDETERMINEDSPECIFICMESSAGES to evaluation.undeterminedSpecificMessages,
                Tables.EVALUATION.UNDETERMINEDGENERALMESSAGES to evaluation.undeterminedGeneralMessages,
                Tables.EVALUATION.FAILSPECIFICMESSAGES to evaluation.failSpecificMessages,
                Tables.EVALUATION.FAILGENERALMESSAGES to evaluation.failGeneralMessages
            ).forEach { (column, collection) -> dbRecord.set(column, DataUtil.concat(collection)) }

            dbRecord
        }

    private fun writeEvaluations(transactionContext: DSLContext, indexedCohortMatchesWithTrialIds: List<Triple<Int, Int, CohortMatch>>) {
        val rows = indexedCohortMatchesWithTrialIds.flatMap { (trialMatchId, cohortMatchId, cohortMatch) ->
            evaluationsFromCohortMatch(transactionContext, trialMatchId, cohortMatchId, cohortMatch)
        }
        transactionContext.batchInsert(rows).execute()
    }

    private fun writeCohortMatchesAndReturnDoublyIndexedList(
        transactionContext: DSLContext,
        trialMatches: List<Pair<Int, TrialMatch>>,
    ): List<Triple<Int, Int, CohortMatch>> {
        val (outputEntries, rows) = trialMatches.flatMap { (trialMatchId, trialMatch) ->
            cohortMatchesFromTrialMatch(transactionContext, trialMatchId, trialMatch)
        }
            .mapIndexed { index, (cohortMatch, dbRecord) ->
                val id = index + 1
                dbRecord.set(Tables.COHORTMATCH.ID, id)
                Triple(dbRecord.get(Tables.COHORTMATCH.TRIALMATCHID), id, cohortMatch) to dbRecord
            }
            .unzip()

        transactionContext.batchInsert(rows).execute()
        return outputEntries
    }
}