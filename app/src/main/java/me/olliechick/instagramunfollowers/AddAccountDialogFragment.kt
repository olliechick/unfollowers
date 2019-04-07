package me.olliechick.instagramunfollowers

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.add_account_dialog.*
import kotlinx.android.synthetic.main.add_account_dialog.view.*
import me.olliechick.instagramunfollowers.Util.Companion.TAG

class AddAccountDialogFragment : DialogFragment() {
    internal lateinit var listener: AddAccountDialogListener

    interface AddAccountDialogListener {
        fun onAccountAdded(username: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = context as AddAccountDialogListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException(
                (context.toString() +
                        " must implement AddAccountDialogListener")
            )
        }
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {

            val builder = AlertDialog.Builder(it)
            val view = requireActivity().layoutInflater.inflate(R.layout.add_account_dialog, null)
            builder.setView(view)
            builder.setTitle(getString(R.string.add_account))
            // Set up the buttons
            builder.setPositiveButton(
                getString(R.string.add)
            ) { dialog, _ ->  run {
                listener.onAccountAdded(view.input?.text.toString().toLowerCase()) } }

            builder.setNegativeButton(
                getString(R.string.cancel)
            ) { dialog, _ -> dialog.cancel() }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}