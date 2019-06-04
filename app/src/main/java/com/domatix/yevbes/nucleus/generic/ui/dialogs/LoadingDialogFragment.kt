package com.domatix.yevbes.nucleus.generic.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.domatix.yevbes.nucleus.R
import com.domatix.yevbes.nucleus.generic.callbacs.OnDialogDetachListener
import com.domatix.yevbes.nucleus.generic.callbacs.OnDialogViewCreatedListener


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val ARG_PARAM3 = "param3"
private const val ARG_PARAM4 = "param4"
private const val ARG_PARAM5 = "param5"
private const val ARG_PARAM6 = "param6"
private const val ARG_PARAM7 = "param7"

class LoadingDialogFragment : DialogFragment() {
    private var onDialogDetachListener: OnDialogDetachListener? = null
    private var onDialogViewCreatedListener: OnDialogViewCreatedListener? = null
    private lateinit var ctx: Context
    private lateinit var manager: FragmentManager
    private var dialogTag: String = "TAG"
    private var showInstantly: Boolean = false
    private var playAnimation = false

    private lateinit var tvTitle: TextView
    private lateinit var tvMessage: TextView
    private lateinit var lottieAnimationView: LottieAnimationView

    companion object {
        fun newInstance(context: Context, manager: FragmentManager, tag: String = "TAG",
                        title: String = "TITLE", message: String = "MESSAGE", animation: String = "loading.json", showInstantly: Boolean = true,
                        playAnimation: Boolean = true, loopAnimation: Boolean = true, repeatCount: Int = LottieDrawable.INFINITE, cancelable: Boolean = true) =
                LoadingDialogFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, title)
                        putString(ARG_PARAM2, message)
                        putString(ARG_PARAM3, animation)
                        putBoolean(ARG_PARAM4, playAnimation)
                        putBoolean(ARG_PARAM5, loopAnimation)
                        putInt(ARG_PARAM6, repeatCount)
                        putBoolean(ARG_PARAM7, cancelable)
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
        val v = activity!!.layoutInflater.inflate(R.layout.dialog_loading, null)
        builder.setView(v)

        tvTitle = v.findViewById(R.id.dialog_loading_title)
        tvMessage = v.findViewById(R.id.dialog_loading_message)
        lottieAnimationView = v.findViewById(R.id.dialog_loading_animation)

        val dialog = builder.create()

        arguments?.let {
            tvTitle.text = it.getString(ARG_PARAM1)
            tvMessage.text = it.getString(ARG_PARAM2)
            setAnimation(it.getString(ARG_PARAM3), it.getBoolean(ARG_PARAM4), it.getBoolean(ARG_PARAM5), it.getInt(ARG_PARAM6))
            this.isCancelable = it.getBoolean(ARG_PARAM7)
        }
        return dialog!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (onDialogViewCreatedListener != null)
            onDialogViewCreatedListener?.onDialogViewCreated()
    }

    override fun onStart() {
        super.onStart()
    }

    fun setOnDialogDetachLIstener(listener: OnDialogDetachListener) {
        this.onDialogDetachListener = listener
    }

    fun setOnDialogViewCreatedListener(listener: OnDialogViewCreatedListener) {
        this.onDialogViewCreatedListener = listener
    }

    fun setTilte(title: String) {
        tvTitle.text = title
    }

    fun setMessage(message: String) {
        tvMessage.text = message
    }

    fun setAnimation(animation: String, playAnimation: Boolean, loop: Boolean, repeatCount: Int = LottieDrawable.INFINITE) {
        lottieAnimationView.setAnimation(animation)
        if (loop) {
            lottieAnimationView.repeatCount = repeatCount
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