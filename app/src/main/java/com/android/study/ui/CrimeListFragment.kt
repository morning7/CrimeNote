package com.android.study.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.*
import com.android.study.R
import com.android.study.control.CrimeListViewModel
import com.android.study.databinding.FragmentCrimeListBinding
import com.android.study.databinding.ListItemCrimeBinding
import com.android.study.model.Crime
import java.text.SimpleDateFormat
import java.util.*

class CrimeListFragment: Fragment(){

    interface Callbacks {
        fun onCrimeSelected(crimeId: UUID)
    }

    private var callbacks: Callbacks?= null

    private lateinit var bind: FragmentCrimeListBinding
    private var crimeAdapter = CrimeAdapter(emptyList())

    private val crimeListViewModel: CrimeListViewModel by lazy {
        ViewModelProvider(this)[CrimeListViewModel::class.java]
    }

    companion object {
        fun newInstance(): CrimeListFragment{
            return CrimeListFragment()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            callbacks = context as Callbacks?
        } catch (e: ClassCastException){

        }
    }

    override fun onStart() {
        super.onStart()
        val activity = activity as AppCompatActivity
        val toolbar = activity.supportActionBar
        toolbar?.title = "Crime List"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentCrimeListBinding.inflate(inflater, container, false)
        bind.rvCrime.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = crimeAdapter
        }
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeListViewModel.crimesLiveData.observe(viewLifecycleOwner, { crimes ->
            crimes?.let {
                crimeAdapter = CrimeAdapter(crimes)
                bind.rvCrime.adapter = crimeAdapter
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_crime_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        callbacks?.onCrimeSelected(UUID.randomUUID())
        return true
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    private inner class CrimeAdapter(private var crimes: List<Crime>): RecyclerView.Adapter<CrimeViewHolder>(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeViewHolder {
            return CrimeViewHolder(ListItemCrimeBinding.inflate(LayoutInflater.from(parent.context)))
        }

        override fun onBindViewHolder(holder: CrimeViewHolder, position: Int) {
            holder.bindData(crimes[position])
        }

        override fun getItemCount(): Int {
            return crimes.size
        }
    }

    private inner class CrimeViewHolder(private val bind: ListItemCrimeBinding): RecyclerView.ViewHolder(bind.root), View.OnClickListener {
        private lateinit var crime: Crime
        init {
            bind.root.setOnClickListener(this)
        }

        fun bindData(crime: Crime) {
            this.crime = crime
            bind.apply {
                tvTitle.text = crime.title
                val format = SimpleDateFormat("EEE MMM dd yyyy", Locale.getDefault())
                tvDate.text = format.format(crime.date)
            }
        }

        override fun onClick(v: View?) {
            callbacks?.onCrimeSelected(crime.id)
        }
    }
}