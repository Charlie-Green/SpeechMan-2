package by.vadim_churun.ordered.speechman2.views

import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import by.vadim_churun.ordered.speechman2.R
import by.vadim_churun.ordered.speechman2.db.objs.Money
import kotlinx.android.synthetic.main.money_picker_dialog.*


class MoneyPickerDialog(
    private val purpose: String,
    private val initialMoney: Money?,
    private val onMoneyPicked: (Money) -> Unit
): DialogFragment()
{
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    // FUNCTIONALITY:

    /** @return whether input was validated. **/
    private fun applyInput(): Boolean
    {
        /** @return a pair of the parsed [Money] and resource ID of the error message.
          * Exactly one of the pair's fields is null. **/
        fun validate(input: String): Pair<Money?, Int?>
        {
            val words = input.split(' ').filter { it.isNotEmpty() }
            if(words.size != 2)
                return Pair(null, R.string.msg_money_two_words)

            // TODO: Take care about the decimal point, which depends on locale.
            val amount = words[0].toFloatOrNull()
            if(amount == null || amount < 0f)
                return Pair(null, R.string.msg_money_amount_incorrect)

            if(!words[1].matches( Regex("[a-zA-Z]{3}") ))
                return Pair(null, R.string.msg_money_currency_incorrect)

            return Pair(Money(amount, words[1].toUpperCase()), null)
        }

        val result = validate(etMoney.text.toString())
        result.first?.also {
            tvError.visibility = View.INVISIBLE
            onMoneyPicked(it)
        }
        result.second?.also {
            tvError.visibility = View.VISIBLE
            tvError.setText(it)
        }
        return result.first != null
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    // LIFECYCLE:

    override fun onCreateView
    (inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
        = inflater.inflate(R.layout.money_picker_dialog, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        tvPurpose.text = purpose
        initialMoney?.also { etMoney.setText("$it") }
        buCancel.setOnClickListener { super.dismiss() }
        buApply.setOnClickListener {
            if(applyInput()) super.dismiss()
        }
    }
}