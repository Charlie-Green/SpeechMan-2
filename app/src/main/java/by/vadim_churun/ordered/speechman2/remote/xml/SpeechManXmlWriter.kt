package by.vadim_churun.ordered.speechman2.remote.xml

import by.vadim_churun.ordered.speechman2.db.entities.*
import by.vadim_churun.ordered.speechman2.db.objs.HistoryStatus
import java.io.*


class SpeechManXmlWriter: AutoCloseable
{
    //////////////////////////////////////////////////////////////////////////////////////////////////
    // PROPERTIES:

    private val out: PrintWriter

    /** If true, the resulting XML file is more human readable, but is widely larger in its size. **/
    var doFormatting = false


    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // LIFECYCLE:

    constructor(outstream: OutputStream)
    { out = PrintWriter(outstream) }

    override fun close()
        = out.close()


    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // HELP METHODS:

    private var tabs: MutableList<String>? = null

    private fun getTabs(count: Int): String
    {
        if(!doFormatting) return " "

        val mTabs = tabs ?: mutableListOf(System.lineSeparator()).also { tabs = it }
        while(mTabs.size <= count)
        {
            mTabs.add(mTabs.last() + "\t")
        }
        // mTabs[j] contains the line separator + j tabs.
        return mTabs[count]
    }

    private fun getHistoryStatusValue(hs: HistoryStatus): String
    {
        when(hs)
        {
            HistoryStatus.NOT_IN_HISTORY -> return "nih"
            HistoryStatus.USUAL          -> return "usual"
            else                         -> return "flower"
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // HEAD AND ENDING:

    private class AttributePair(
        var name: String,
        var value: String
    )
    private val attrCache = AttributePair("", "")

    fun writeHead()
    {
        out.print("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
        out.print("${getTabs(0)}<${XmlContract.TAG_ROOT}>")
    }

    fun writeEnding()
    { out.println("${getTabs(0)}</${XmlContract.TAG_ROOT}>") }


    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // ENTITIES:

    private fun <T> writeEntities(
        entities: Collection<T>,
        tagName: String,
        attrCount: Int,
        attrFactory: (T, Int) -> Unit,
        contentFactory: (T) -> String? = { null } )
    {
        for(entity in entities)
        {
            out.print("${getTabs(1)}<$tagName ")

            for(attrIndex in 0.until(attrCount))
            {
                attrFactory(entity, attrIndex)    // Initializes attrCache.
                if(attrCache.value.isNotEmpty())
                    out.print("${getTabs(3)}${attrCache.name}=\"${attrCache.value}\"")
            }

            val content = contentFactory(entity)
            content?.also {
                out.print(">${getTabs(0)}${getTabs(2)}")
                out.print(it)
                out.print("${getTabs(0)}${getTabs(1)}</$tagName>")
            } ?: out.print(" />")

            out.print(getTabs(0))    // Terminate line, if formatting is enabled.
        }
    }

    fun writePersonTypes(types: Collection<PersonType>)
    { /* PersonType's are not yet supported. */ }

    fun writePeople(people: Collection<Person>)
        = writeEntities(people,
            XmlContract.TAG_PERSON,
            3, { person, index ->
                when(index)
                {
                    0 -> {
                        attrCache.name = XmlContract.ATTR_PSEUDOID
                        attrCache.value = person.ID?.let { "$it" } ?: ""
                    }
                    1 -> {
                        attrCache.name = XmlContract.ATTR_PERSON_NAME
                        attrCache.value = person.name
                    }
                    2 -> {
                        attrCache.name = XmlContract.ATTR_PERSON_TYPE_ID
                        attrCache.value = person.personTypeID?.let { "$it" } ?: ""
                    }
                }
            }
        )

    fun writeSeminars(seminars: Collection<Seminar>)
        = writeEntities(seminars,
            XmlContract.TAG_SEMINAR,
            6, { sem, index ->
                when(index)
                {
                    0 -> {
                        attrCache.name = XmlContract.ATTR_PSEUDOID
                        attrCache.value = sem.ID?.let { "$it" } ?: ""
                    }
                    1 -> {
                        attrCache.name = XmlContract.ATTR_SEMINAR_NAME
                        attrCache.value = sem.name
                    }
                    2 -> {
                        attrCache.name = XmlContract.ATTR_SEMINAR_CITY
                        attrCache.value = sem.city
                    }
                    3 -> {
                        attrCache.name = XmlContract.ATTR_SEMINAR_ADDRESS
                        attrCache.value = sem.address
                    }
                    4 -> {
                        attrCache.name = XmlContract.ATTR_SEMINAR_COSTING
                        when(sem.costing)
                        {
                            Seminar.CostingStrategy.FIXED             -> attrCache.value = "f"
                            Seminar.CostingStrategy.PARTICIPANTS      -> attrCache.value = "p"
                            Seminar.CostingStrategy.DATE              -> attrCache.value = "d"
                            Seminar.CostingStrategy.PARTICIPANTS_DATE -> attrCache.value = "pd"
                            Seminar.CostingStrategy.DATE_PARTICIPANTS -> attrCache.value = "dp"
                        }
                    }
                    5 -> {
                        attrCache.name = XmlContract.ATTR_SEMINAR_DELETED
                        attrCache.value = if(sem.isLogicallyDeleted) "yes" else "no"
                    }
                }
            },
            { sem -> sem.content }
        )

    fun writeProducts(products: Collection<Product>)
        = writeEntities(products,
            XmlContract.TAG_PRODUCT,
            5, { product, index ->
                when(index)
                {
                    0 -> {
                        attrCache.name = XmlContract.ATTR_PSEUDOID
                        attrCache.value = product.ID?.let { "$it" } ?: ""
                    }
                    1 -> {
                        attrCache.name = XmlContract.ATTR_PRODUCT_NAME
                        attrCache.value = product.name
                    }
                    2 -> {
                        attrCache.name = XmlContract.ATTR_PRODUCT_COST
                        attrCache.value = "${product.cost}"
                    }
                    3 -> {
                        attrCache.name = XmlContract.ATTR_PRODUCT_COUNT_BOXED
                        attrCache.value = "${product.countBoxes}"
                    }
                    4 -> {
                        attrCache.name = XmlContract.ATTR_PRODUCT_COUNT_CASED
                        attrCache.value = "${product.countCase}"
                    }
                }
            }
        )

    fun writeAppointments(appointments: Collection<Appointment>)
        = writeEntities(appointments,
            XmlContract.TAG_APPOINT_LONG,
            6, { appoint, index ->
                when(index)
                {
                    0 -> {
                        attrCache.name = XmlContract.ATTR_APPOINT_PERSON_ID
                        attrCache.value = "${appoint.personID}"
                    }
                    1 -> {
                        attrCache.name = XmlContract.ATTR_APPOINT_SEMINAR_ID
                        attrCache.value = "${appoint.seminarID}"
                    }
                    2 -> {
                        attrCache.name = XmlContract.ATTR_APPOINT_PURCHASE
                        attrCache.value = "${appoint.purchase}"
                    }
                    3 -> {
                        attrCache.name = XmlContract.ATTR_APPOINT_COST
                        attrCache.value = "${appoint.cost}"
                    }
                    4 -> {
                        attrCache.name = XmlContract.ATTR_APPOINT_HISTORY_STATUS
                        attrCache.value = getHistoryStatusValue(appoint.historyStatus)
                    }
                    5 -> {
                        attrCache.name = XmlContract.ATTR_APPOINT_DELETED
                        attrCache.value = if(appoint.isLogicallyDeleted) "yes" else "no"
                    }
                }
            }
        )

    fun writeOrders(orders: Collection<Order>)
        = writeEntities(orders,
            XmlContract.TAG_ORDER,
            5, { order, index ->
                when(index)
                {
                    0 -> {
                        attrCache.name = XmlContract.ATTR_ORDER_PERSON_ID
                        attrCache.value = "${order.personID}"
                    }
                    1 -> {
                        attrCache.name = XmlContract.ATTR_ORDER_PRODUCT_ID
                        attrCache.value = "${order.productID}"
                    }
                    2 -> {
                        attrCache.name = XmlContract.ATTR_ORDER_PURCHASE
                        attrCache.value = "${order.purchase}"
                    }
                    3 -> {
                        attrCache.name = XmlContract.ATTR_ORDER_HISTORY_STATUS
                        attrCache.value = getHistoryStatusValue(order.historyStatus)
                    }
                    4 -> {
                        attrCache.name = XmlContract.ATTR_ORDER_DELETED
                        attrCache.value = if(order.isLogicallyDeleted) "yes" else "no"
                    }
                }
            }
        )

    fun writeSemDays(days: Collection<SemDay>)
        = writeEntities(days,
            XmlContract.TAG_SEMDAY_LONG,
            4, { day, index ->
                when(index)
                {
                    0 -> {
                        attrCache.name = XmlContract.ATTR_PSEUDOID
                        attrCache.value = "${day.ID}"
                    }
                    1 -> {
                        attrCache.name = XmlContract.ATTR_SEMDAY_SEMINAR_ID
                        attrCache.value = "${day.seminarID}"
                    }
                    2 -> {
                        attrCache.name = XmlContract.ATTR_SEMDAY_START
                        attrCache.value = "${day.start.timeInMillis}"
                    }
                    3 -> {
                        attrCache.name = XmlContract.ATTR_SEMDAY_DURATION
                        attrCache.value = "${day.duration}"
                    }
                }
            })

    fun writeSemCosts(costs: Collection<SemCost>)
        = writeEntities(costs,
            XmlContract.TAG_SEMCOST_LONG,
            4, { cost, index ->
                when(index)
                {
                    0 -> {
                        attrCache.name = XmlContract.ATTR_SEMCOST_SEMINAR_ID
                        attrCache.value = "${cost.seminarID}"
                    }
                    1 -> {
                        attrCache.name = XmlContract.ATTR_SEMCOST_PARTICIPANTS
                        attrCache.value = "${cost.minParticipants}"
                    }
                    2 -> {
                        attrCache.name = XmlContract.ATTR_SEMCOST_DATE
                        attrCache.value = "${cost.minDate.timeInMillis}"
                    }
                    3 -> {
                        attrCache.name = XmlContract.ATTR_SEMCOST_MONEY
                        attrCache.value = "${cost.cost}"
                    }
                }
            })
}