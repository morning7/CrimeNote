package com.android.study.ui

import android.os.Bundle
import android.view.*
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.fragment.app.DialogFragment
import com.android.study.databinding.DialogPhotoBinding
import com.android.study.utils.PhotoUtils
import java.io.File

const val ARG_PATH = "path"

class PhotoDialogFragment: DialogFragment() {
    private lateinit var bind: DialogPhotoBinding

    companion object {
        fun newInstance(path: String): PhotoDialogFragment {
            return PhotoDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PATH, path)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = DialogPhotoBinding.inflate(inflater, container, false)
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val path = requireArguments().getString(ARG_PATH)
        activity?.let {
            updatePhotoView(path!!)
        }
    }

    private fun updatePhotoView(path: String) {
        bind.ivPhoto.apply {
            if (!File(path).exists()) {
                setImageBitmap(null)
                return@apply
            }
            viewTreeObserver.addOnGlobalLayoutListener(object:
                OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    setImageBitmap(PhotoUtils.getScaledBitmap(path, width, height))
                    if (viewTreeObserver.isAlive) {
                        viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }
                }
            })
        }
    }
}