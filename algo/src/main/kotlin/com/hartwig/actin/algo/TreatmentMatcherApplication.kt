package com.hartwig.actin.algo

import com.hartwig.actin.PatientRecordFactory
import com.hartwig.actin.TreatmentDatabaseFactory
import com.hartwig.actin.algo.calendar.ReferenceDateProviderFactory
import com.hartwig.actin.algo.ckb.EfficacyEntryFactory
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.algo.serialization.TreatmentMatchJson
import com.hartwig.actin.algo.util.TreatmentMatchPrinter
import com.hartwig.actin.clinical.serialization.ClinicalRecordJson
import com.hartwig.actin.clinical.util.ClinicalPrinter
import com.hartwig.actin.configuration.EMC_TRIAL_SOURCE
import com.hartwig.actin.configuration.EnvironmentConfiguration
import com.hartwig.actin.doid.DoidModelFactory
import com.hartwig.actin.doid.serialization.DoidJson
import com.hartwig.actin.medication.AtcTree
import com.hartwig.actin.medication.MedicationCategories
import com.hartwig.actin.molecular.interpretation.MolecularInputChecker
import com.hartwig.actin.molecular.serialization.MolecularRecordJson
import com.hartwig.actin.molecular.util.MolecularPrinter
import com.hartwig.actin.trial.input.FunctionInputResolver
import com.hartwig.actin.trial.serialization.TrialJson
import java.io.IOException
import kotlin.system.exitProcess
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class TreatmentMatcherApplication(private val config: TreatmentMatcherConfig) {
    fun run() {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION)

        LOGGER.info("Loading clinical record from {}", config.clinicalJson)
        val clinical = ClinicalRecordJson.read(config.clinicalJson)
        ClinicalPrinter.printRecord(clinical)

        val molecular = config.molecularJson?.let { molecularJson ->
            LOGGER.info("Loading molecular record from {}", molecularJson)
            MolecularRecordJson.read(molecularJson)
        }
        molecular?.let(MolecularPrinter::printRecord) ?: LOGGER.info("No molecular record provided")

        val patient = PatientRecordFactory.fromInputs(clinical, molecular)

        LOGGER.info("Loading trials from {}", config.trialDatabaseDirectory)
        val trials = TrialJson.readFromDir(config.trialDatabaseDirectory)
        LOGGER.info(" Loaded {} trials", trials.size)

        LOGGER.info("Loading DOID tree from {}", config.doidJson)
        val doidEntry = DoidJson.readDoidOwlEntry(config.doidJson)
        LOGGER.info(" Loaded {} nodes", doidEntry.nodes.size)
        val doidModel = DoidModelFactory.createFromDoidEntry(doidEntry)

        LOGGER.info("Creating ATC tree from file {}", config.atcTsv)
        val atcTree = AtcTree.createFromFile(config.atcTsv)

        val referenceDateProvider = ReferenceDateProviderFactory.create(patient, config.runHistorically)
        LOGGER.info("Matching patient to available trials")

        // We assume we never check validity of a gene inside algo.
        val molecularInputChecker = MolecularInputChecker.createAnyGeneValid()
        val treatmentDatabase = TreatmentDatabaseFactory.createFromPath(config.treatmentDirectory)
        val functionInputResolver =
            FunctionInputResolver(doidModel, molecularInputChecker, treatmentDatabase, MedicationCategories.create(atcTree))
        val resources = RuleMappingResources(referenceDateProvider, doidModel, functionInputResolver, atcTree, treatmentDatabase)
        val evidenceEntries = EfficacyEntryFactory(treatmentDatabase).extractEfficacyEvidenceFromCkbFile(config.extendedEfficacyJson)

        val overrides = config.overridesYaml?.let { EnvironmentConfiguration.createFromFile(config.overridesYaml) }

        val match = TreatmentMatcher.create(resources, trials, evidenceEntries, overrides?.algo?.trialSource ?: EMC_TRIAL_SOURCE)
            .evaluateAndAnnotateMatchesForPatient(patient)

        TreatmentMatchPrinter.printMatch(match)
        TreatmentMatchJson.write(match, config.outputDirectory)
        LOGGER.info("Done!")
    }

    companion object {
        val LOGGER: Logger = LogManager.getLogger(TreatmentMatcherApplication::class.java)
        const val APPLICATION = "ACTIN Treatment Matcher"
        val VERSION: String = TreatmentMatcherApplication::class.java.getPackage().implementationVersion ?: "UNKNOWN VERSION"
    }
}

@Throws(IOException::class)
fun main(args: Array<String>) {
    val options: Options = TreatmentMatcherConfig.createOptions()
    try {
        val config = TreatmentMatcherConfig.createConfig(DefaultParser().parse(options, args))
        TreatmentMatcherApplication(config).run()
    } catch (exception: ParseException) {
        TreatmentMatcherApplication.LOGGER.error(exception)
        HelpFormatter().printHelp(TreatmentMatcherApplication.APPLICATION, options)
        exitProcess(1)
    }
}