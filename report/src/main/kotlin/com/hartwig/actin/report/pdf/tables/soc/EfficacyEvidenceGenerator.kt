package com.hartwig.actin.report.pdf.tables.soc

import com.hartwig.actin.datamodel.algo.AnnotatedTreatmentMatch
import com.hartwig.actin.datamodel.efficacy.EfficacyEntry
import com.hartwig.actin.datamodel.efficacy.PatientPopulation
import com.hartwig.actin.datamodel.efficacy.TrialReference
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.tables.soc.SOCGeneratorFunctions.addEndPointsToTable
import com.hartwig.actin.report.pdf.tables.soc.SOCGeneratorFunctions.analysisGroupForPopulation
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.kernel.pdf.action.PdfAction
import com.itextpdf.layout.element.Table

class EfficacyEvidenceGenerator(private val treatments: List<AnnotatedTreatmentMatch>?) : TableGenerator {

    private val patientCharacteristicHeadersAndFunctions = listOf<Pair<String, (PatientPopulation) -> String?>>(
        "WHO/ECOG" to SOCGeneratorFunctions::createWhoString,
        "Primary tumor location" to { it.formatTumorLocation(", ") },
        "Mutations" to PatientPopulation::mutations,
        "Metastatic sites" to PatientPopulation::formatMetastaticSites,
        "Previous systemic therapy" to { "${it.priorSystemicTherapy ?: NA}/${it.numberOfPatients}" },
        "Prior therapies" to PatientPopulation::priorTherapies
    )

    override fun title(): String {
        return "Standard of care options considered potentially eligible"
    }

    override fun forceKeepTogether(): Boolean {
        return false
    }

    override fun contents(): Table {
        if (treatments.isNullOrEmpty()) {
            return Tables.createSingleCol()
                .addCell(Cells.createContentNoBorder("There are no standard of care treatment options for this patient"))
        } else {
            val table = Tables.createRelativeWidthCols(1f, 3f)
            table.addHeaderCell(Cells.createHeader("Treatment"))
            table.addHeaderCell(Cells.createHeader("Literature efficacy evidence"))
            treatments.sortedBy { it.annotations.size }.reversed().forEach { treatment: AnnotatedTreatmentMatch ->
                table.addCell(Cells.createContentBold(SOCGeneratorFunctions.abbreviate(treatment.treatmentCandidate.treatment.name)))
                if (treatment.annotations.isNotEmpty()) {
                    val subTable = Tables.createSingleCol()
                    for (annotation in treatment.annotations) {
                        for (trialReference in annotation.trialReferences) {
                            subTable.addCell(
                                Cells.create(createOneLiteraturePart(annotation, trialReference, treatment))
                            )
                        }
                    }
                    table.addCell(Cells.createContent(subTable))
                } else table.addCell(Cells.createContent("No literature efficacy evidence available yet"))
            }
            return table
        }
    }

    private fun createOneLiteraturePart(
        annotation: EfficacyEntry,
        trialReference: TrialReference,
        treatment: AnnotatedTreatmentMatch
    ): Table {
        val subTable = Tables.createSingleCol()
        val nestedTables = listOf(
            createTrialHeader(annotation),
            createPatientCharacteristics(trialReference, treatment),
            createEndpoints(trialReference, treatment)
        )
        nestedTables.forEachIndexed { i, nestedTable ->
            subTable.addCell(Cells.create(nestedTable))
            if (i < nestedTables.size - 1) {
                subTable.addCell(Cells.createEmpty())
            }
        }
        return subTable
    }

    private fun createTrialHeader(annotation: EfficacyEntry): Table {
        val table = Tables.createFixedWidthCols(100f, 150f).setWidth(250f)
        table.addCell(
            Cells.createSubTitle(annotation.acronym).setAction(PdfAction.createURI(annotation.trialReferences.first().url))
                .addStyle(Styles.urlStyle())
        )
        table.addCell(Cells.createValue(""))
        table.addCell(Cells.createValue("Patient characteristics: "))
        table.addCell(Cells.createKey(""))
        return table
    }

    private fun createPatientCharacteristics(trialReference: TrialReference, treatment: AnnotatedTreatmentMatch): Table {
        val table = Tables.createRelativeWidthCols(1f, 3f)
        trialReference.patientPopulations.asSequence()
            .filter { it.treatment?.name.equals(treatment.treatmentCandidate.treatment.name, true) }
            .forEach { addPatientCharacteristicsToTable(it, table) }
        return table
    }

    private fun addPatientCharacteristicsToTable(patientPopulation: PatientPopulation, table: Table) {
        patientCharacteristicHeadersAndFunctions.flatMap { (characteristic, extractAsString) ->
            sequenceOf(characteristic, extractAsString(patientPopulation) ?: NA)
        }
            .forEach { table.addCell(Cells.createContent(it)) }
    }

    private fun createEndpoints(trialReference: TrialReference, treatment: AnnotatedTreatmentMatch): Table {
        val table = Tables.createFixedWidthCols(10f, 25f).setWidth(350f)
        trialReference.patientPopulations
            .filter { it.treatment?.name.equals(treatment.treatmentCandidate.treatment.name, true) }
            .forEach { patientPopulation ->
                val analysisGroup = analysisGroupForPopulation(patientPopulation)
                table.addCell(Cells.createValue("Median PFS: "))
                addEndPointsToTable(analysisGroup, "Median Progression-Free Survival", table)

                table.addCell(Cells.createValue("Median OS: "))
                addEndPointsToTable(analysisGroup, "Median Overall Survival", table)
            }
        table.addCell(Cells.createEmpty())
        table.addCell(Cells.createEmpty())
        return table
    }
}