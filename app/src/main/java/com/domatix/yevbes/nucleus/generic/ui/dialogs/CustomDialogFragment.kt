package com.domatix.yevbes.nucleus.generic.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.domatix.yevbes.nucleus.R
import com.domatix.yevbes.nucleus.generic.callbacs.dialogs.OnDialogButtonsClickListener
import com.domatix.yevbes.nucleus.generic.callbacs.dialogs.OnDialogDetachListener
import com.domatix.yevbes.nucleus.generic.callbacs.dialogs.OnDialogStartListener


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val ARG_PARAM3 = "param3"
private const val ARG_PARAM4 = "param4"
private const val ARG_PARAM5 = "param5"
private const val ARG_PARAM6 = "param6"
private const val ARG_PARAM7 = "param7"
private const val ARG_PARAM8 = "param8"
private const val ARG_PARAM9 = "param9"
private const val ARG_PARAM10 = "param10"

class CustomDialogFragment : DialogFragment() {
    private var onDialogDetachListener: OnDialogDetachListener? = null
    private var onDialogViewCreatedListener: OnDialogStartListener? = null
    private var onDialogButtonsClickListener: OnDialogButtonsClickListener? = null
    private lateinit var ctx: Context
    private lateinit var manager: FragmentManager
    private var dialogTag: String = "TAG"
    private var showInstantly: Boolean = false
    private var playAnimation = false

    private lateinit var tvTitle: TextView
    private lateinit var tvMessage: TextView
    private lateinit var lottieAnimationView: LottieAnimationView
    private lateinit var linearLayoutButtons: LinearLayout
    private lateinit var positiveButton: Button
    private lateinit var negativeButton: Button

    companion object {
        fun newInstance(context: Context, manager: FragmentManager, tag: String = "TAG",
                        title: String = "TITLE", message: String = "MESSAGE", animation: String = "loading.json", showInstantly: Boolean = true,
                        playAnimation: Boolean = true, minFrame: Int = 0, maxFrame: Int = -1, loopAnimation: Boolean = true, repeatCount: Int = LottieDrawable.INFINITE, cancelable: Boolean = true, visibleButtons: Boolean = false) =
                CustomDialogFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, title)
                        putString(ARG_PARAM2, message)
                        putString(ARG_PARAM3, animation)
                        putBoolean(ARG_PARAM4, playAnimation)
                        putBoolean(ARG_PARAM5, loopAnimation)
                        putInt(ARG_PARAM6, repeatCount)
                        putBoolean(ARG_PARAM7, cancelable)
                        putBoolean(ARG_PARAM8, visibleButtons)
                        putInt(ARG_PARAM9, minFrame)
                        putInt(ARG_PARAM10, maxFrame)
                    }
                    dialogTag = tag
                    this.manager = manager
                    this.ctx = context
                    this.showInstantly = showInstantly

                    if (showInstantly) {
                        apply {
                            showDialog()
                        }
                    }
                }

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(ctx)
        val v = activity!!.layoutInflater.inflate(R.layout.custom_dialog_fragment, null)
        builder.setView(v)

        tvTitle = v.findViewById(R.id.custom_dialog_title)
        tvMessage = v.findViewById(R.id.custom_dialog_message)
        lottieAnimationView = v.findViewById(R.id.custom_dialog_animation)
        linearLayoutButtons = v.findViewById(R.id.layout_buttons_cancel_confirm)
        positiveButton = v.findViewById<Button>(R.id.positive_button)
        negativeButton = v.findViewById<Button>(R.id.negative_button)

        val dialog = builder.create()

        arguments?.let {
            tvTitle.text = it.getString(ARG_PARAM1)
            tvMessage.text = it.getString(ARG_PARAM2)
            setAnimation(it.getString(ARG_PARAM3), it.getBoolean(ARG_PARAM4), it.getInt(ARG_PARAM9), it.getInt(ARG_PARAM10), it.getBoolean(ARG_PARAM5), it.getInt(ARG_PARAM6))
            this.isCancelable = it.getBoolean(ARG_PARAM7)

            if (it.getBoolean(ARG_PARAM8)) {
                linearLayoutButtons.visibility = View.VISIBLE
            }

            positiveButton.setOnClickListener {
                if (onDialogButtonsClickListener != null)
                    onDialogButtonsClickListener?.onPositiveButtonPressed()
            }

            negativeButton.setOnClickListener {
                if (onDialogButtonsClickListener != null)
                    onDialogButtonsClickListener?.onNegativeButtonPressed()
            }
        }
        return dialog!!
    }

    override fun onStart() {
        super.onStart()
        if (onDialogViewCreatedListener != null)
            onDialogViewCreatedListener?.onDialogStarted()
    }

    fun isVisibleDialogButtons(isEnabled: Boolean) {
        if (::linearLayoutButtons.isInitialized) {
            if (isEnabled)
                linearLayoutButtons.visibility = View.VISIBLE
            else
                linearLayoutButtons.visibility = View.GONE
        }
    }

    fun setOnDialogButtonsClickListener(listener: OnDialogButtonsClickListener) {
        this.onDialogButtonsClickListener = listener
    }

    fun setOnDialogDetachLIstener(listener: OnDialogDetachListener) {
        this.onDialogDetachListener = listener
    }

    fun setOnDialogStartListener(listener: OnDialogStartListener) {
        this.onDialogViewCreatedListener = listener
    }

    fun setTilte(title: String) {
        tvTitle.text = title
    }

    fun setMessage(message: String) {
        tvMessage.text = message
    }

    fun setButtonsText(negative: String = negativeButton.text.toString(), positive: String= positiveButton.text.toString()) {
        negativeButton.text = negative
        positiveButton.text = positive
    }

    fun setAnimation(animation: String, playAnimation: Boolean, minFrame: Int = 0, maxFrame: Int = -1, loop: Boolean, repeatCount: Int = LottieDrawable.INFINITE) {
        lottieAnimationView.setAnimation(animation)
        if (loop) {
            lottieAnimationView.repeatCount = repeatCount
        } else {
            lottieAnimationView.repeatCount = 0
        }

        lottieAnimationView.setMinFrame(minFrame)
        if (maxFrame > -1) {
            lottieAnimationView.setMaxFrame(maxFrame)
        }

        if (playAnimation)
            startPlayAnimation()

    }

    fun startPlayAnimation() {
        lottieAnimationView.playAnimation()
    }

    fun pausePlayAnimation() {
        lottieAnimationView.pauseAnimation()
    }

    fun resumePlayAnimation() {
        lottieAnimationView.resumeAnimation()
    }

    fun stopPlayAnimation() {
        lottieAnimationView.cancelAnimation()
    }

    fun setRepeatCountAnimation(repeatCount: Int) {
        lottieAnimationView.repeatCount = repeatCount
    }

    fun setCancelableDialog(cancelable: Boolean) {
        isCancelable = cancelable
    }

    fun showDialog() {
        show(manager, dialogTag)
        if (playAnimation) {
            startPlayAnimation()
        }
    }

    fun dismissDialog() {
        dismiss()
    }

    override fun onDetach() {
        super.onDetach()
        if (onDialogDetachListener != null)
            onDialogDetachListener?.onDialogDetached()
    }

}