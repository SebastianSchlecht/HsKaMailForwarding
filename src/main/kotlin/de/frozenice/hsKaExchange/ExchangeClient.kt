package de.frozenice.hsKaExchange

import de.frozenice.hsKaExchange.dao.MailDao
import microsoft.exchange.webservices.data.core.ExchangeService
import microsoft.exchange.webservices.data.core.PropertySet
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName
import microsoft.exchange.webservices.data.core.enumeration.search.LogicalOperator
import microsoft.exchange.webservices.data.core.enumeration.service.ConflictResolutionMode
import microsoft.exchange.webservices.data.core.service.item.EmailMessage
import microsoft.exchange.webservices.data.core.service.item.Item
import microsoft.exchange.webservices.data.core.service.schema.EmailMessageSchema
import microsoft.exchange.webservices.data.credential.WebCredentials
import microsoft.exchange.webservices.data.property.complex.EmailAddress
import microsoft.exchange.webservices.data.property.complex.MessageBody
import microsoft.exchange.webservices.data.search.FindItemsResults
import microsoft.exchange.webservices.data.search.ItemView
import microsoft.exchange.webservices.data.search.filter.SearchFilter


class ExchangeClient(email: String, password: String ) {
    private val username: String = email.substringBefore('@')
    private val service = ExchangeService(ExchangeVersion.Exchange2010)


    init {
        if (!email.contains('@')) {
            throw IllegalArgumentException("$email is not a valid e-mail.")
        }

        service.credentials = WebCredentials(username, password)
        service.autodiscoverUrl(email)
    }



    fun forwardMails(mails: List<EmailMessage>, receiver: EmailAddress, textBody: String = "") {
        mails.forEach {
            it.forward(MessageBody.getMessageBodyFromText(textBody), receiver)
        }
    }

    fun markAsRead(mails: List<EmailMessage>) {
        mails.forEach {
            it.isRead = true
            it.update(ConflictResolutionMode.AutoResolve)
        }
    }


    fun findUnreadMails(maxAmount: Int = 20): List<EmailMessage> {
        val sf = SearchFilter.SearchFilterCollection(LogicalOperator.And, SearchFilter.IsEqualTo(EmailMessageSchema.IsRead, false))
        val findResults = service.findItems(WellKnownFolderName.Inbox, sf, ItemView(maxAmount))

        //MOOOOOOST IMPORTANT: load messages' properties before
        if (findResults.totalCount != 0) {
            service.loadPropertiesForItems(findResults, PropertySet.FirstClassProperties)
        }

         return findResults.map {
            it as EmailMessage
         }
    }


    private fun convertMail(findItemsResults: FindItemsResults<Item>): List<MailDao> =
            findItemsResults.map { MailDao(it.id.uniqueId, it.subject) }
}