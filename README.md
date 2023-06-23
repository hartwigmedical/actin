# ACTIN

ACTIN is a system that determines available treatment options for cancer patients based on the following inputs:
 - Comprehensive clinical record of the patient
 - Comprehensive molecular analysis of the tumor
 - Set of all treatment options available (standard-of-care (SOC) and experimental).

![ACTIN System](system/src/main/resources/actin_system.png)
 
More details on the following modules are available from the links below:

| Module                 | Description                                                                                |
|------------------------|--------------------------------------------------------------------------------------------|
| [Clinical](clinical)   | Ingestion and curation of an external electronic health record (EHR) clinical data stream. |
| [Molecular](molecular) | Interpretation of molecular tests and ingestion into ACTIN.                                |
| [Treatment](treatment) | Generation of available treatment options (standard-of-care and experimental).             |
| [Algo](algo)           | Matching all data from a patient to available treatment options.                           |
| [Database](database)   | Capture of all ACTIN data in a database.                                                   |
| [Report](report)       | Create a patient-centric PDF report with available treatment options.                      |

### Developing and Releasing ACTIN

To make a chance in ACTIN and release the process is as follows:
 - Create a ticket in JIRA and make a branch from that ticket with `git checkout -b ACTIN-#`
 - When changes are complete create a PR
  