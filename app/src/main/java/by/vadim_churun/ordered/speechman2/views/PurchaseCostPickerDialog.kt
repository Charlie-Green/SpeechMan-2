package by.vadim_churun.ordered.speechman2.views

import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import by.vadim_churun.ordered.speechman2.R
import by.vadim_churun.ordered.speechman2.db.objs.Money
import kotlinx.android.synthetic.main.purchase_cost_picker_dialog.*


/** Allows to pick information like "paid {some much} out of {some much}". **/
class PurchaseCostPickerDialog(
    private val purpose: String,
    private val initialPurchase: Money?,
    private val initialCost: Money?,
    private val onPicked: (purchase: Money, cost: Money) -> Unit
): DialogFragment()
{
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // FUNCTIONALITY:

    private fun applyInput()
    {
        prbar.visibility = View.VISIBLE

        fun tryParseMoney(src: EditText, errorTextView: TextView): Money?
        {
            try {
                return Money
                    .parse(src.text.toString())
                    .also { errorTextView.visibility = View.GONE }
            } catch(exc: Exception) {
                errorTextView.visibility = View.VISIBLE
                return null
            }
        }

        val purchase = tryParseMoney(etPurchase, tvPurchaseError)
        val cost = tryParseMoney(etCost, tvCostError)
        if(purchase != null && cost != null) {
            onPicked(purchase, cost)
            super.dismiss()
        } else {
            prbar.visibility = View.GONE
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // LIFECYCLE:

    override fun onCreateView
    (inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
        = inflater.inflate(R.layout.purchase_cost_picker_dialog, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        tvPurpose.text = purpose
        initialPurchase?.also { etPurchase.setText("$it") }
        initialCost?.also { etCost.setText("$it") }
        buApply.setOnClickListener { applyInput() }
        buCancel.setOnClickListener { super.dismiss() }
    }
}