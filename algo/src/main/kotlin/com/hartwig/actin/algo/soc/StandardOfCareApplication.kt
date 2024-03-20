package com.hartwig.actin.algo.soc

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.PatientRecordFactory
import com.hartwig.actin.TreatmentDatabaseFactory
import com.hartwig.actin.algo.calendar.ReferenceDateProviderFactory
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.clinical.serialization.ClinicalRecordJson
import com.hartwig.actin.clinical.util.ClinicalPrinter
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.doid.DoidModelFactory
import com.hartwig.actin.doid.datamodel.DoidEntry
import com.hartwig.actin.doid.serialization.DoidJson
import com.hartwig.actin.medication.AtcTree
import com.hartwig.actin.medication.MedicationCategories
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.interpretation.MolecularInputChecker
import com.hartwig.actin.molecular.serialization.MolecularHistoryJson
import com.hartwig.actin.molecular.util.MolecularPrinter
import com.hartwig.actin.trial.input.FunctionInputResolver
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.IOException
import kotlin.system.exitProcess

class StandardOfCareApplication(private val config: StandardOfCareConfig) {

    fun run() {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION)

        LOGGER.info("Loading clinical record from {}", config.clinicalJson)
        val clinical: ClinicalRecord = ClinicalRecordJson.read(config.clinicalJson)
        ClinicalPrinter.printRecord(clinical)

        LOGGER.info("Loading molecular history from {}", config.molecularJson)
        val molecularHistory = MolecularHistoryJson.read(config.molecularJson)
        val molecular = requireNotNull(molecularHistory.mostRecentWGS()) {
            "No WGS record found in molecular history"
        }
        // TODO (kz) make a molecularHistoryPrinter
        MolecularPrinter.printRecord(molecular)

        val patient: PatientRecord = PatientRecordFactory.fromInputs(clinical, molecularHistory)

        LOGGER.info("Loading DOID tree from {}", config.doidJson)
        val doidEntry: DoidEntry = DoidJson.readDoidOwlEntry(config.doidJson)
        LOGGER.info(" Loaded {} nodes", doidEntry.nodes.size)
        val doidModel: DoidModel = DoidModelFactory.createFromDoidEntry(doidEntry)

        LOGGER.info("Creating ATC tree from file {}", config.atcTsv)
        val atcTree = AtcTree.createFromFile(config.atcTsv)

        LOGGER.info("Loading treatment data from {}", config.treatmentDirectory)
        val treatmentDatabase = TreatmentDatabaseFactory.createFromPath(config.treatmentDirectory)

        val referenceDateProvider = ReferenceDateProviderFactory.create(patient, config.runHistorically)
        val functionInputResolver = FunctionInputResolver(
            doidModel, MolecularInputChecker.createAnyGeneValid(), treatmentDatabase, MedicationCategories.create(atcTree)
        )
        val resources = RuleMappingResources(referenceDateProvider, doidModel, functionInputResolver, atcTree, treatmentDatabase)
        val recommendationEngine = RecommendationEngineFactory(resources).create()

        LOGGER.info(recommendationEngine.provideRecommendations(patient))
        val patientHasExhaustedStandardOfCare = recommendationEngine.patientHasExhaustedStandardOfCare(patient)
        LOGGER.info("Standard of care has${if (patientHasExhaustedStandardOfCare) "" else " not"} been exhausted")
    }

    companion object {
        val LOGGER: Logger = LogManager.getLogger(StandardOfCareApplication::class.java)
        const val APPLICATION = "ACTIN Standard of Care"
        private val VERSION = StandardOfCareApplication::class.java.getPackage().implementationVersion
    }
}

@Throws(IOException::class)
fun main(args: Array<String>) {
    val options: Options = StandardOfCareConfig.createOptions()

    try {
        val config = StandardOfCareConfig.createConfig(DefaultParser().parse(options, args))
        StandardOfCareApplication(config).run()
    } catch (exception: ParseException) {
        StandardOfCareApplication.LOGGER.warn(exception)
        HelpFormatter().printHelp(StandardOfCareApplication.APPLICATION, options)
        exitProcess(1)
    }
}
