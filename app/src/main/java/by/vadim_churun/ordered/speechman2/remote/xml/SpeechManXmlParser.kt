package by.vadim_churun.ordered.speechman2.remote.xml

import android.util.Xml
import by.vadim_churun.ordered.speechman2.db.entities.*
import by.vadim_churun.ordered.speechman2.db.objs.*
import by.vadim_churun.ordered.speechman2.remote.lack.*
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import java.util.*
import kotlin.collections.*


object SpeechManXmlParser
{
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // HELP METHODS:

    private fun XmlPullParser.getBooleanValue(name: String): Boolean?
    {
        val value = getAttributeValue(null, name)
        when(value)
        {
            null    -> return null
            "true"  -> return true
            "yes"   -> return true
            "false" -> return false
            "no"    -> return false
            else    -> throw SpeechManXmlException(
                "$name attribute: expected a boolean value, got \"$value\""
            )
        }
    }

    private fun XmlPullParser.getCostingStrategyValue(name: String): Seminar.CostingStrategy?
    {
        val value = getAttributeValue(null, name) ?: return null
        when(value)
        {
            "fix" -> return Seminar.CostingStrategy.FIXED
            "f"   -> return Seminar.CostingStrategy.FIXED
            "p"   -> return Seminar.CostingStrategy.PARTICIPANTS
            "d"   -> return Seminar.CostingStrategy.DATE
            "pd"  -> return Seminar.CostingStrategy.PARTICIPANTS_DATE
            "dp"  -> return Seminar.CostingStrategy.DATE_PARTICIPANTS
            else  -> throw SpeechManXmlException("Unknown costing strategy \"$value\"")
        }
    }

    private fun XmlPullParser.getHistoryStatusValue(name: String): HistoryStatus?
    {
        val value = getAttributeValue(null, name) ?: return null
        when(value)
        {
            "nih"    -> return HistoryStatus.NOT_IN_HISTORY
            "usual"  -> return HistoryStatus.USUAL
            "flower" -> return HistoryStatus.FLOWER
            else     -> throw SpeechManXmlException("Unknown history status \"$value\"")
        }
    }

    private fun XmlPullParser.getIntValue(name: String): Int?
    {
        val value = getAttributeValue(null, name) ?: return null
        try {
            return value.toInt()
        } catch(exc: NumberFormatException) {
            throw SpeechManXmlException("$name attribute: expected an integer, got \"$value\"")
        }
    }

    private fun XmlPullParser.getLongValue(name: String): Long?
    {
        val value = getAttributeValue(null, name) ?: return null
        try {
            return value.toLong()
        } catch(exc: NumberFormatException) {
            throw SpeechManXmlException("$name attribute: expected a long integer, got \"$value\"")
        }
    }

    private fun XmlPullParser.getMoneyValue(name: String): Money?
    {
        val value = getAttributeValue(null, name) ?: return null
        try {
            return Money.parse(value)
        } catch(exc: Exception) {
            throw SpeechManXmlException("$name attribute: \"$value\" is not recognized as Money")
        }
    }

    private fun XmlPullParser.getStringValue(name: String): String?
        = getAttributeValue(null, name)

    private fun <T> HashSet<T>?.isNotNullAndContains(element: T): Boolean
        = this != null && this.contains(element)

    private val Any.ID: Int?
        get() {
            if(this is SeminarNameLack)
                return ID
            if(this is SeminarCityLack)
                return ID
            return (this as Seminar).ID
        }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // PRIVATE PARSING METHODS:

    private fun processRootTag(parser: XmlPullParser)
    {
        if(parser.depth != 1)
            throw SpeechManXmlException("<$XmlContract.TAG_ROOT> must be the root tag")
    }

    private fun processPersonTag(parser: XmlPullParser,
        objs: MutableCollection<Any>,
        presentPersonIDs: HashSet<Int> )
    {
        if(parser.depth != 2)
            throw SpeechManXmlException(
                "<${XmlContract.TAG_PERSON}> must be a direct child of <$XmlContract.TAG_ROOT>" )

        val id = parser.getIntValue(XmlContract.ATTR_PSEUDOID)
        val name = parser.getStringValue(XmlContract.ATTR_PERSON_NAME)
            ?: throw SpeechManXmlException(
                "${XmlContract.ATTR_PERSON_NAME} is a mandatory attribute for <${XmlContract.TAG_PERSON}>" )
        val type = parser.getIntValue(XmlContract.ATTR_PERSON_TYPE_ID)
        id?.also { presentPersonIDs.add(it) }

        objs.add( Person(id, name, type) )
    }

    private fun processPersonTypeTag(parser: XmlPullParser)
    { /* Person types are not yer supported. */ }

    /** @return seminarObject **/
    private fun processSeminarTag
    (parser: XmlPullParser, seminarMap: HashMap<Int, Any>): Any
    {
        if(parser.depth != 2)
            throw SpeechManXmlException(
                "<${XmlContract.TAG_SEMINAR}> must be a direct child of <${XmlContract.TAG_ROOT}>" )

        val id = parser.getIntValue(XmlContract.ATTR_PSEUDOID)
        val name = parser.getStringValue(XmlContract.ATTR_SEMINAR_NAME)
        val city = parser.getStringValue(XmlContract.ATTR_SEMINAR_CITY)
        if(name == null && city == null) {
            throw SpeechManXmlException("<$XmlContract.TAG_SEMINAR> is missing both " +
                "${XmlContract.ATTR_SEMINAR_NAME} and ${XmlContract.ATTR_SEMINAR_CITY} attributes" )
        }
        val address = parser.getStringValue(XmlContract.ATTR_SEMINAR_ADDRESS) ?: ""
        val isDeleted = parser.getBooleanValue(XmlContract.ATTR_SEMINAR_DELETED) ?: false
        val costing = parser.getCostingStrategyValue(XmlContract.ATTR_SEMINAR_COSTING) ?: Seminar.CostingStrategy.FIXED

        val seminarObject: Any
        if(name == null)
            seminarObject = SeminarNameLack(id, city!!, address, "", costing, isDeleted)
        else if(city == null)
            seminarObject = SeminarCityLack(id, name!!, address, "", costing, isDeleted)
        else
            seminarObject = Seminar(id, name, city, address, "", null, costing, isDeleted)
        id?.also { seminarMap.put(id, seminarObject) }
        return seminarObject
    }

    private fun processProductTag(parser: XmlPullParser,
        objs: MutableCollection<Any>,
        lacks: MutableCollection<DataLack<*,*>>,
        productMap: HashMap<Int, Any> )
    {
        if(parser.depth != 2)
            throw SpeechManXmlException("<$XmlContract.TAG_PRODUCT> must be a direct child of <$XmlContract.TAG_ROOT>")

        val id = parser.getIntValue(XmlContract.ATTR_PSEUDOID)
        val name = parser.getStringValue(XmlContract.ATTR_PRODUCT_NAME)
            ?: throw SpeechManXmlException(
                "${XmlContract.ATTR_PRODUCT_NAME} is a mandatory attribute for <${XmlContract.TAG_PRODUCT}>"
            )
        val cost = parser.getMoneyValue(XmlContract.ATTR_PRODUCT_COST)
        val boxed = parser.getIntValue(XmlContract.ATTR_PRODUCT_COUNT_BOXED) ?: 0
        val cased = parser.getIntValue(XmlContract.ATTR_PRODUCT_COUNT_CASED) ?: 0
        val deleted = parser.getBooleanValue(XmlContract.ATTR_PRODUCT_DELETED) ?: false

        val costObject: Any
        if(cost == null) {
            ProductCostLack(id, name, boxed, cased, deleted).also {
                costObject = it
                lacks.add(it)
            }
        } else {
            Product(id, name, cost, boxed, cased, deleted).also {
                costObject = it
                objs.add(it)
            }
        }
        id?.also { productMap[it] = costObject }
    }


    private fun getSeminarIdForDayOrCost(parser: XmlPullParser,
        shortTagName: String,
        longTagName: String,
        seminarIdAttrName: String,
        seminarObject: Any?
    ): Int
    {
        if(parser.name == shortTagName) {
            if(seminarObject == null || parser.depth != 3) {
                throw SpeechManXmlException(
                    "<$shortTagName> must be a direct child of <${XmlContract.TAG_SEMINAR}>"
                )
            }

            // This SemDay or SemCost inherits Seminar ID from its parent Seminar tag.
            return seminarObject.ID ?: throw SpeechManXmlException(
                "<${XmlContract.TAG_SEMINAR}> without ${XmlContract.ATTR_PSEUDOID} cannot contain <$shortTagName>" )
        }

        if( (parser.depth != 3 && parser.depth != 2) ||
            (parser.depth == 3 && seminarObject == null) ) {
            throw SpeechManXmlException(
                "<$longTagName> must be a direct child of either <${XmlContract.TAG_SEMINAR}> or <${XmlContract.TAG_ROOT}>"
            )
        }

        if(seminarObject == null) {
            return parser.getIntValue(seminarIdAttrName)
                ?: throw SpeechManXmlException(
                    "<$longTagName> must either " +
                            "be a direct child of <${XmlContract.TAG_SEMINAR}> or specify $seminarIdAttrName attribute"
                )
        }

        return seminarObject.ID ?: throw SpeechManXmlException(
            "<${XmlContract.TAG_SEMINAR}> without ${XmlContract.ATTR_PSEUDOID} cannot contain <$longTagName>"
        )
    }

    private fun processSemDayTag(
        parser: XmlPullParser,
        objs: MutableCollection<Any>,
        seminarObject: Any? )
    {
        val semID =
            getSeminarIdForDayOrCost(
                parser,
                XmlContract.TAG_SEMDAY_SHORT,
                XmlContract.TAG_SEMDAY_LONG,
                XmlContract.ATTR_SEMDAY_SEMINAR_ID,
                seminarObject
            )

        val start = parser.getLongValue(XmlContract.ATTR_SEMDAY_START)?.let {
            Calendar.getInstance().apply { timeInMillis = it }
        } ?: throw SpeechManXmlException("<${parser.name}> requires ${XmlContract.ATTR_SEMDAY_START} attribute")
        val dur = parser.getAttributeValue(null,
            XmlContract.ATTR_SEMDAY_DURATION
        )?.toShort() ?: 480

        objs.add( SemDay(null, semID, start, dur) )
    }

    private fun processSemCostTag(parser: XmlPullParser,
        objs: MutableCollection<Any>,
        lacks: MutableCollection<DataLack<*,*>>,
        seminarObject: Any?,
        costCountMap: HashMap<Int, Int> )
    {
        val semID =
            getSeminarIdForDayOrCost(
                parser,
                XmlContract.TAG_SEMCOST_SHORT,
                XmlContract.TAG_SEMCOST_LONG,
                XmlContract.ATTR_SEMCOST_SEMINAR_ID,
                seminarObject
            )
        val oldCount = costCountMap[semID] ?: 0
        costCountMap[semID] = oldCount + 1

        val particips = parser.getIntValue(XmlContract.ATTR_SEMCOST_PARTICIPANTS) ?: 0
        val dateMillis = parser.getLongValue(XmlContract.ATTR_SEMCOST_DATE) ?: Long.MIN_VALUE
        val date = Calendar.getInstance().apply { timeInMillis = dateMillis }
        val cost = parser.getMoneyValue(XmlContract.ATTR_SEMCOST_MONEY)

        cost?.also {
            objs.add( SemCost(null, semID, particips, date, it) )
        } ?: lacks.add( SemCostMoneyLack(semID, particips, date) )
    }

    private fun processAppointTag(parser: XmlPullParser,
        objs: MutableCollection<Any>,
        lacks: MutableCollection<DataLack<*,*>>,
        mandatoryAliveSemIDs: HashSet<Int> )
    {
        val personID = parser.getIntValue(XmlContract.ATTR_APPOINT_PERSON_ID)
            ?: throw SpeechManXmlException(
                "${XmlContract.ATTR_APPOINT_PERSON_ID} attribute is mandatory for <${parser.name}>"
            )

        val semID = parser.getIntValue(XmlContract.ATTR_APPOINT_SEMINAR_ID)
            ?: throw SpeechManXmlException(
                "${XmlContract.ATTR_APPOINT_SEMINAR_ID} attribute is mandatory for <${parser.name}>"
            )

        val purchase = parser.getMoneyValue(XmlContract.ATTR_APPOINT_PURCHASE)
        val cost = parser.getMoneyValue(XmlContract.ATTR_APPOINT_COST)
        val history = parser.getHistoryStatusValue(XmlContract.ATTR_APPOINT_HISTORY_STATUS) ?: HistoryStatus.USUAL
        val deleted: Boolean
        when(parser.getBooleanValue(XmlContract.ATTR_APPOINT_DELETED))
        {
            true -> { deleted = true }
            false -> {
                deleted = false
                // This Appointment is forced to be alive, so its associated Seminar must be alive, too.
                mandatoryAliveSemIDs.add(semID)
            }
            null -> {
                // By default, false.
                // Later may be changed if the associated seminar turns out to be deleted.
                deleted = false
            }
        }

        if(purchase == null && cost != null)
            lacks.add( AppointmentPurchaseLack(personID, semID, cost, history, deleted) )
        else if(purchase != null && cost == null)
            lacks.add( AppointmentCostLack(personID, semID, purchase, history, deleted) )
        else if(purchase == null && cost == null)
            lacks.add( AppointmentMoneyLack(personID, semID, history, deleted) )
        else /* if(purchase != null && cost != null) */
            objs.add( Appointment(personID, semID, purchase!!, cost!!, history, deleted) )
    }

    private fun processOrderTag(parser: XmlPullParser,
        objs: MutableCollection<Any>,
        lacks: MutableCollection< DataLack<*,*> >,
        mandatoryAliveProductIDs: HashSet<Int> )
    {
        val personID = parser.getIntValue(XmlContract.ATTR_ORDER_PERSON_ID)
            ?: throw SpeechManXmlException(
                "${XmlContract.ATTR_ORDER_PERSON_ID} attribute is mandatory for <${XmlContract.TAG_ORDER}>"
            )

        val productID = parser.getIntValue(XmlContract.ATTR_ORDER_PRODUCT_ID)
            ?: throw SpeechManXmlException(
                "$XmlContract.ATTR_ORDER_PRODUCT_ID attribute is mandatory for <$XmlContract.TAG_ORDER>"
            )

        val purchase = parser.getMoneyValue(XmlContract.ATTR_ORDER_PURCHASE)
        val history = parser.getHistoryStatusValue(XmlContract.ATTR_ORDER_HISTORY_STATUS) ?: HistoryStatus.USUAL
        val deleted: Boolean
        when(parser.getBooleanValue(XmlContract.ATTR_ORDER_DELETED))
        {
            true -> {
                deleted = true
            }
            false -> {
                deleted = false
                mandatoryAliveProductIDs.add(productID)
            }
            null -> {
                deleted = false
            }
        }

        purchase?.also {
            objs.add( Order(personID, productID, it, history, deleted) )
        } ?: lacks.add( OrderPurchaseLack(personID, productID, history, deleted) )
    }

    /** @return seminarObject **/
    private fun processText(parser: XmlPullParser, seminarObject: Any?): Any?
    {
        seminarObject ?: return null

        var text = parser.text
        if(text.isEmpty()) return seminarObject
        val isNotWhiteSpace = { char: Char -> !" \t\r\n".contains(char) }
        val leftIndex = text.indexOfFirst(isNotWhiteSpace)
            .let { Math.min(Math.max(it, 0), text.length - 1) }
        val rightIndex = text.indexOfLast(isNotWhiteSpace)
            .let { Math.min(Math.max(it, 0), text.length - 1) }
        text = text.substring(leftIndex, rightIndex + 1)

        when(seminarObject)
        {
            is SeminarNameLack -> return seminarObject.apply { content = text }
            is SeminarCityLack -> return seminarObject.apply { content = text }
            is Seminar -> return Seminar(
                seminarObject.ID,
                seminarObject.name,
                seminarObject.city,
                seminarObject.address,
                text,
                seminarObject.imageUri,
                seminarObject.costing,
                seminarObject.isLogicallyDeleted
            )
            else -> throw Exception(
                "seminarObject has unexpected type ${seminarObject.javaClass.name}" )
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // VALIDATTION:

    private fun validateAppointment(
        appoint: Any,
        presentPersonIDs: HashSet<Int>?,
        seminarMap: HashMap<Int, Any>?,
        mandatoryAliveSemIDs: HashSet<Int>?
    ): Any
    {
        val personID: Int; val semID: Int
        when(appoint)
        {
            is AppointmentPurchaseLack -> {
                personID = appoint.personID
                semID = appoint.seminarID
            }
            is AppointmentCostLack -> {
                personID = appoint.personID
                semID = appoint.seminarID
            }
            is AppointmentMoneyLack -> {
                personID = appoint.personID
                semID = appoint.seminarID
            }
            else -> {
                personID = (appoint as Appointment).personID
                semID = (appoint as Appointment).seminarID
            }
        }

        if(!presentPersonIDs.isNotNullAndContains(personID))
            throw SpeechManXmlException("No person pseudo-ID $personID found for an Appointment")
        if(seminarMap?.containsKey(semID) != true)
            throw SpeechManXmlException("No seminar pseudo-ID $semID found for an Appointment")

        val sem = seminarMap[semID]!!
        val isSemDeleted: Boolean
        when(sem)
        {
            is SeminarNameLack -> isSemDeleted = sem.isDeleted
            is SeminarCityLack -> isSemDeleted = sem.isDeleted
            else -> isSemDeleted = (sem as Seminar).isLogicallyDeleted
        }

        if(mandatoryAliveSemIDs?.contains(semID) == true && isSemDeleted) {
            // The seminar is deleted and the Appointment is forced not to be.
            throw SpeechManXmlException(
                "" +
                        "Seminar ID $semID must not be deleted, since it has an undeleted Appointment"
            )
        }

        if(!isSemDeleted)
            return appoint    // The appointment can be either deleted or not.

        // The appointment must be deleted, because it's seminar is.
        when(appoint)
        {
            is AppointmentPurchaseLack -> return appoint.apply { isDeleted = true }
            is AppointmentCostLack -> return appoint.apply { isDeleted = true }
            is AppointmentMoneyLack -> return appoint.apply { isDeleted = true }
            is Appointment -> return Appointment(
                appoint.personID,
                semID,
                appoint.purchase,
                appoint.cost,
                appoint.historyStatus,
                true
            )
            else -> throw TypeCastException(
                "Cannot treat ${appoint.javaClass.name} as an appointment instance" )
        }
    }

    /** @return the given Order, the given Order modified or a new Order. **/
    private fun validateOrder(order: Any,
        presentPersonIDs: HashSet<Int>?,
        productMap: HashMap<Int, Any>?,
        mandatoryAliveProductIDs: HashSet<Int>?
    ): Any
    {
        val personID: Int; val productID: Int; val isOrderDeleted: Boolean
        when(order)
        {
            is OrderPurchaseLack -> {
                personID = order.personID; productID = order.productID
                isOrderDeleted = order.isDeleted
            }

            else -> {
                personID = (order as Order).personID
                productID = (order as Order).productID
                isOrderDeleted = (order as Order).isLogicallyDeleted
            }
        }

        if(!presentPersonIDs.isNotNullAndContains(personID))
            throw SpeechManXmlException("No person pseudo-ID $personID found for an Order")
        if(productMap?.containsKey(productID) != true)
            throw SpeechManXmlException("No product pseudo-ID $productID found for an Order")
        if(isOrderDeleted)
            return order

        val product = productMap[productID]!!
        val isProductDeleted: Boolean
        when(product)
        {
            is ProductCostLack -> isProductDeleted = product.isDeleted
            else -> isProductDeleted = (product as Product).isLogicallyDeleted
        }

        if(isProductDeleted && mandatoryAliveProductIDs.isNotNullAndContains(productID)) {
            throw SpeechManXmlException(
                "Product pseudo-ID $productID cannot be deleted since it has an undeleted Order"
            )
        }

        if(!isProductDeleted)
            return order
        when(order)
        {
            is OrderPurchaseLack -> return order.apply { isDeleted = true }
            else -> return Order(
                (order as Order).personID,
                (order as Order).productID,
                (order as Order).purchase,
                (order as Order).historyStatus,
                true
            )
        }
    }

    private fun validateSemDay(seminarID: Int, seminarMap: HashMap<Int, Any>?)
    {
        if(seminarMap?.containsKey(seminarID) != true)
            throw SpeechManXmlException("No seminar pseudo-ID $seminarID found for a SemDay")
    }

    private fun validateSemCost(
        seminarID: Int,
        seminarMap: HashMap<Int, Any>?,
        costCountMap: HashMap<Int, Int>? )
    {
        costCountMap ?: throw NullPointerException("Cannot have a SemCost with null costCountMap")

        val sem = seminarMap?.get(seminarID)
            ?: throw SpeechManXmlException("No seminar ID $seminarID found for a SemCost")

        if(costCountMap[seminarID]!! == 1)
            return
        // If the seminar's CostingStrategy is FIXED, having more than 1 SemCost is forbidden.
        val costing: Seminar.CostingStrategy
        when(sem)
        {
            is SeminarNameLack -> costing = sem.costing
            is SeminarCityLack -> costing = sem.costing
            else -> costing = (sem as Seminar).costing
        }
        if(costing == Seminar.CostingStrategy.FIXED) {
            throw SpeechManXmlException(
                "Cannot have more than one SemCost " +
                        "for a Seminar with fixed costing (found ${costCountMap[seminarID]})"
            )
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // PUBLIC PARSE:

    /** Parses input from the given string considering it to be SpeechMan 2 data.
      * IMPORTANT: Any ID in created objects/lacks is a pseudo ID identifying the object/lack
      * only in this stream, rather than an ID which identifies it in the whole database.
      * @param instream (in parameter): the source of data.
      * @param objs (out parameter): successfully parsed SpeechMan 2 entities.
      * @param lacks (out parameter): [DataLack] objects extracted from the stream. **/
    fun parse(instream: InputStream,
        objs: MutableList<Any>,
        lacks: MutableList< DataLack<*,*> > )
    {
        var presentPersonIDs: HashSet<Int>? = null
        var presentPersonTypeIDs: HashSet<Int>? = null     // PersonType's are not yet supported.
        var seminarMap: HashMap<Int, Any>? = null
        var productMap: HashMap<Int, Any>? = null
        var costCountMap: HashMap<Int, Int>? = null
        var mandatoryAliveSemIDs: HashSet<Int>? = null
        var mandatoryAliveProductIDs: HashSet<Int>? = null

        // Contains information about a seminar retrieved on START_TAG, and then filled on TEXT:
        var seminarObject: Any? = null

        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(instream, null)

        // Parse:
        while(parser.next() != XmlPullParser.END_DOCUMENT)
        {
            if(parser.eventType == XmlPullParser.TEXT) {
                seminarObject =
                    processText(
                        parser,
                        seminarObject
                    )
                continue
            }
            if(parser.eventType == XmlPullParser.END_TAG) {
                if(parser.name == XmlContract.TAG_SEMINAR) {
                    if(seminarObject is DataLack<*,*>)
                        lacks.add(seminarObject)
                    else
                        objs.add(seminarObject!!)
                    seminarObject = null
                }
                continue
            }
            if(parser.eventType != XmlPullParser.START_TAG) {
                continue
            }

            var tagName = parser.name
            if(tagName == XmlContract.TAG_SEMDAY_SHORT)
                tagName =
                    XmlContract.TAG_SEMDAY_LONG    // To execute the same code for both.
            else if(tagName == XmlContract.TAG_SEMCOST_SHORT)
                tagName =
                    XmlContract.TAG_SEMCOST_LONG
            else if(tagName == XmlContract.TAG_APPOINT_SHORT)
                tagName =
                    XmlContract.TAG_APPOINT_LONG
            when(tagName)
            {
                XmlContract.TAG_ROOT -> {
                    processRootTag(
                        parser
                    )
                }

                XmlContract.TAG_PERSON -> {
                    presentPersonIDs = presentPersonIDs ?: HashSet()
                    processPersonTag(
                        parser,
                        objs,
                        presentPersonIDs
                    )
                }

                XmlContract.TAG_PERSON_TYPE -> {
                    processPersonTypeTag(
                        parser
                    )
                }

                XmlContract.TAG_SEMINAR -> {
                    seminarMap = seminarMap ?: HashMap()
                    seminarObject =
                        processSeminarTag(
                            parser,
                            seminarMap
                        )
                }

                XmlContract.TAG_PRODUCT -> {
                    productMap = productMap ?: HashMap()
                    processProductTag(
                        parser,
                        objs,
                        lacks,
                        productMap
                    )
                }

                XmlContract.TAG_SEMDAY_LONG -> {
                    processSemDayTag(
                        parser,
                        objs,
                        seminarObject
                    )
                }

                XmlContract.TAG_SEMCOST_LONG -> {
                    costCountMap = costCountMap ?: HashMap()
                    processSemCostTag(
                        parser,
                        objs,
                        lacks,
                        seminarObject,
                        costCountMap
                    )
                }

                XmlContract.TAG_APPOINT_LONG -> {
                    mandatoryAliveSemIDs = mandatoryAliveSemIDs ?: HashSet()
                    processAppointTag(
                        parser,
                        objs,
                        lacks,
                        mandatoryAliveSemIDs
                    )
                }

                XmlContract.TAG_ORDER -> {
                    mandatoryAliveProductIDs = mandatoryAliveProductIDs ?: HashSet()
                    processOrderTag(
                        parser,
                        objs,
                        lacks,
                        mandatoryAliveProductIDs
                    )
                }

                else -> {
                    throw SpeechManXmlException("Unknown tag <$tagName>")
                }
            }
        }

        // Assert that:
        // - all ID constrains are fulfilled:
        // - no Appointments associated with a deleted Seminar are forced not to be deleted.
        for(j in 0.until(objs.size))
        {
            val obj = objs[j]
            when(obj)
            {
                is Appointment -> {
                    objs[j] =
                        validateAppointment(
                            obj, presentPersonIDs, seminarMap, mandatoryAliveSemIDs
                        )
                }

                is Order -> {
                    objs[j] =
                        validateOrder(
                            obj,
                            presentPersonIDs,
                            productMap,
                            mandatoryAliveProductIDs
                        )
                }

                is SemDay -> {
                    validateSemDay(
                        obj.seminarID,
                        seminarMap
                    )
                }

                is SemCost -> {
                    validateSemCost(
                        obj.seminarID,
                        seminarMap,
                        costCountMap
                    )
                }
            }
        }
        for(j in 0.until(lacks.size))
        {
            val lack = lacks[j]
            when(lack)
            {
                is AppointmentPurchaseLack -> {
                    lacks[j] = validateAppointment(
                        lack, presentPersonIDs, seminarMap, mandatoryAliveSemIDs
                    ) as DataLack<*,*>
                }

                is AppointmentCostLack -> {
                    lacks[j] = validateAppointment(
                        lack, presentPersonIDs, seminarMap, mandatoryAliveSemIDs
                    ) as DataLack<*,*>
                }

                is AppointmentMoneyLack -> {
                    lacks[j] = validateAppointment(
                        lack, presentPersonIDs, seminarMap, mandatoryAliveSemIDs
                    ) as DataLack<*,*>
                }

                is OrderPurchaseLack -> {
                    lacks[j] = validateOrder(
                        lack, presentPersonIDs, productMap, mandatoryAliveProductIDs
                    ) as DataLack<*,*>
                }

                is SemCostMoneyLack -> {
                    validateSemCost(
                        lack.seminarID,
                        seminarMap,
                        costCountMap
                    )
                }
            }
        }
    }
}