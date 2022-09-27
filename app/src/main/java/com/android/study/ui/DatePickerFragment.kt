package com.android.study.ui

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import java.util.*

const val ARG_DATE = "date"
const val REQUEST_DATE = "request_date"

class DatePickerFragment: DialogFragment() {

    companion object {
        fun newInstance(date: Date): DatePickerFragment{
            return DatePickerFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_DATE, date)
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val date = requireArguments().getSerializable(ARG_DATE) as Date
        val calendar = Calendar.getInstance()
        calendar.time = date
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return DatePickerDialog(requireContext(),
            { _, _year, _month, _dayOfMonth ->
                parentFragmentManager.setFragmentResult(REQUEST_DATE, Bundle().apply {
                    putSerializable(ARG_DATE, GregorianCalendar(_year, _month, _dayOfMonth).time)
                })
            }, year, month, day)
    }
}