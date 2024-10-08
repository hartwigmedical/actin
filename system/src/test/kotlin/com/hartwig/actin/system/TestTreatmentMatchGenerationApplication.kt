package com.hartwig.actin.system

import com.hartwig.actin.PatientRecordJson
import com.hartwig.actin.TreatmentDatabaseFactory
import com.hartwig.actin.algo.TreatmentMatcher
import com.hartwig.actin.algo.TreatmentMatcherApplication
import com.hartwig.actin.algo.calendar.ReferenceDateProviderFactory
import com.hartwig.actin.algo.ckb.EfficacyEntryFactory
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.algo.serialization.TreatmentMatchJson
import com.hartwig.actin.algo.soc.ResistanceEvidenceMatcher
import com.hartwig.actin.algo.util.TreatmentMatchPrinter
import com.hartwig.actin.configuration.EnvironmentConfiguration
import com.hartwig.actin.doid.DoidModelFactory
import com.hartwig.actin.doid.serialization.DoidJson
import com.hartwig.actin.medication.AtcTree
import com.hartwig.actin.medication.MedicationCategories
import com.hartwig.actin.molecular.interpretation.MolecularInputChecker
import com.hartwig.actin.trial.input.FunctionInputResolver
import com.hartwig.actin.trial.serialization.TrialJson
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.time.Period
import kotlin.system.exitProcess

class TestTreatmentMatchGenerationApplication {

    private val testPatientRecordJson = resourceOnClasspath("test_patient_data/test_patient.patient_record.json")
    private val testTrialRecordDatabaseDir = resourceOnClasspath("test_trial_database")
    private val doidJson =
        listOf(System.getProperty("user.home"), "hmf", "repos", "actin-resources-private", "disease_ontology", "doid.json").joinToString(
            File.separator
        )
    private val extendedEvidenceJson =
        listOf(System.getProperty("user.home"), "hmf", "repos", "actin-resources-private", "ckb_extended_efficacy", "extended_efficacy_evidence_output.json").joinToString(
            File.separator
        )
    private val atcTreeTsv = listOf(
        System.getProperty("user.home"),
        "hmf",
        "repos",
        "actin-resources-private",
        "atc_config",
        "atc_tree.tsv"
    ).joinToString(File.separator)
    private val treatmentDatabaseDir =
        listOf(System.getProperty("user.home"), "hmf", "repos", "actin-resources-private", "treatment_db").joinToString(File.separator)
    private val outputDirectory = listOf(System.getProperty("user.home"), "hmf", "tmp").joinToString(File.separator)

    fun run() {
        LOGGER.info("Loading patient record from {}", testPatientRecordJson)
        val patient = PatientRecordJson.read(testPatientRecordJson)

        LOGGER.info("Loading trial data from {}", testTrialRecordDatabaseDir)
        val trials = TrialJson.readFromDir(testTrialRecordDatabaseDir)

        LOGGER.info("Loading DOID tree from {}", doidJson)
        val doidEntry = DoidJson.readDoidOwlEntry(doidJson)

        LOGGER.info(" Loaded {} nodes", doidEntry.nodes.size)
        val doidModel = DoidModelFactory.createFromDoidEntry(doidEntry)

        LOGGER.info("Creating ATC tree from file {}", atcTreeTsv)
        val atcTree = AtcTree.createFromFile(atcTreeTsv)

        val referenceDateProvider = ReferenceDateProviderFactory.create(patient, false)
        LOGGER.info("Matching patient to available trials")

        // We assume we never check validity of a gene inside algo.
        val molecularInputChecker = MolecularInputChecker.createAnyGeneValid()
        val treatmentDatabase = TreatmentDatabaseFactory.createFromPath(treatmentDatabaseDir)
        val functionInputResolver =
            FunctionInputResolver(doidModel, molecularInputChecker, treatmentDatabase, MedicationCategories.create(atcTree))
        val environmentConfiguration = EnvironmentConfiguration.create(null)
        val resources = RuleMappingResources(
            referenceDateProvider,
            doidModel,
            functionInputResolver,
            atcTree,
            treatmentDatabase,
            null,
            environmentConfiguration.algo,
            environmentConfiguration.algo.maxMolecularTestAgeInDays?.let { referenceDateProvider.date().minus(Period.ofDays(it)) }

        )
        val evidenceEntries = EfficacyEntryFactory(treatmentDatabase).extractEfficacyEvidenceFromCkbFile(extendedEvidenceJson)

        LOGGER.info("Loading evidence database for resistance evidence")
        val tumorDoids = patient.tumor.doids.orEmpty().toSet()
        val resistanceEvidenceMatcher =
            ResistanceEvidenceMatcher.create(doidModel, tumorDoids, treatmentDatabase, patient.molecularHistory)
        val match = TreatmentMatcher.create(resources, trials, evidenceEntries, resistanceEvidenceMatcher)
            .evaluateAndAnnotateMatchesForPatient(patient)

        TreatmentMatchPrinter.printMatch(match)
        TreatmentMatchJson.write(match, outputDirectory)
        LOGGER.info("Done!")
    }

    companion object {
        val LOGGER: Logger = LogManager.getLogger(TreatmentMatcherApplication::class.java)
        const val APPLICATION = "ACTIN Treatment Matcher"
    }

    private fun resourceOnClasspath(relativePath: String): String {
        return Companion::class.java.getResource("/" + relativePath.removePrefix("/"))!!.path
    }
}

fun main() {
    TestTreatmentMatchGenerationApplication.LOGGER.info("Running {}", TestTreatmentMatchGenerationApplication.APPLICATION)
    try {
        TestTreatmentMatchGenerationApplication().run()
    } catch (exception: ParseException) {
        TestTreatmentMatchGenerationApplication.LOGGER.warn(exception)
        exitProcess(1)
    }
}
