## CKB-Database

[CKB](https://ckbhome.jax.org) - **C**linical **K**nowledge**b**ase - is a cancer knowledgebase provided by Jackson Lab. 
 
This module provides functionality to read a CKB FLEX database in json format and map it to an internal java datamodel.

A test application exists to read the CKB FLEX database and to print some statistics about the database read.

This application requires Java 11+ and can be run as follows:

```
java -cp actin.jar com.hartwig.actin.ckb.CkbDatabaseReaderApplication \
   -ckb_flex_directory /path/to/ckb_flex
```
