package de.frozenice.hsKaExchange

import com.beust.klaxon.Klaxon
import de.frozenice.hsKaExchange.dao.ConfigDao
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

class ConfigParser {

    companion object {
        fun parseConfigFile(file: File): ConfigDao? {
            return Klaxon().parse<ConfigDao>(file)
        }

        fun generateDefaultConfig(file: File) {
            FileWriter(file).use {
                BufferedWriter(it).use {
                    it.write(Klaxon().toJsonString(ConfigDao("mail@hs-karlsruhe.de","123456","yourMail@provider.de")))
                }
            }




        }
    }

}