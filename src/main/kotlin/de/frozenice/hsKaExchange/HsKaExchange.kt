package de.frozenice.hsKaExchange

import microsoft.exchange.webservices.data.autodiscover.exception.AutodiscoverLocalException
import microsoft.exchange.webservices.data.property.complex.EmailAddress
import java.io.File
import java.util.logging.Logger


fun main(args: Array<String>) {

    val configFile = File(Settings.configFile)
    val logger = Logger.getAnonymousLogger()

    if (!configFile.exists()) {
        val templateFile = File(Settings.configFile + "." + Settings.templateEnding)
        ConfigParser.generateDefaultConfig(templateFile)
        logger.info("Please modify ${templateFile.name} and save as ${configFile.name}")
        return
    }

    val config = ConfigParser.parseConfigFile(configFile)

    if (config == null) {
        logger.severe("Error while loading config")
        return
    }

    logger.info("Config loaded with HS e-mail: ${config?.email}")


    var client: ExchangeClient

    try {
        client = ExchangeClient(config.email, config.password)
    } catch (ex: AutodiscoverLocalException) {
        logger.severe("Could't establish an exchange connection. Check email / password and try again.")
        return
    }



    try {
        val mails = client.findUnreadMails()
        client.forwardMails(mails, EmailAddress.getEmailAddressFromString(config.receiverMail))

        mails.forEach {
            logger.info("relayed: ${it.subject}")
        }
    } catch (ex: Exception) {
        logger.severe("Message could't be relayed\n\n$ex")
    }

    logger.info("Shutting down")
}

