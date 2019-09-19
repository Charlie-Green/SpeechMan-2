package by.vadim_churun.ordered.speechman2.db.objs


class Money(
    val amount: Float,
    val currency: String
): Cloneable
{
    companion object
    {
        fun parse(str: String, currencyLen: Int = 3)
            = Money(str.substring(0..str.length-currencyLen-2).toFloat(),
                str.substring(str.length - currencyLen) )
    }

    override fun toString()
        = "${"%.2f".format(amount)} $currency"

    public override fun clone(): Any
        = Money(amount, currency)
}