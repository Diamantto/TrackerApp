package com.example.trackerapp.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trackerapp.R
import com.example.trackerapp.adapters.RunAdapter
import com.example.trackerapp.databinding.FragmentRunBinding
import com.example.trackerapp.ui.MainActivity
import com.example.trackerapp.ui.viewmodels.MainViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RunFragment : Fragment(R.layout.fragment_run) {

    private lateinit var binding: FragmentRunBinding
    private val viewModel: MainViewModel by viewModels()

    private lateinit var runAdapter: RunAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRunBinding.bind(view)

        (activity as MainActivity).requestLocationPermissions()
        setupRecyclerView()

        viewModel.runs.observe(viewLifecycleOwner) {
            runAdapter.submitList(it)
        }

        binding.spFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?, view: View?, pos: Int, id: Long
            ) {
                when (pos) {
                    0 -> viewModel.getSortedRuns("date")
                    1 -> viewModel.getSortedRuns("duration")
                    2 -> viewModel.getSortedRuns("distance")
                    3 -> viewModel.getSortedRuns("speed")
                    4 -> viewModel.getSortedRuns("calories")
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_runFragment_to_trackingFragment)
        }

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val run = runAdapter.differ.currentList[position]
                viewModel.deleteRun(run)
                Snackbar.make(view, "Successfully deleted article", Snackbar.LENGTH_LONG).apply {
                    setAction("Undo") {
                        viewModel.insertRun(run)
                    }
                    show()
                }
            }
        }

        ItemTouchHelper(itemTouchHelperCallback).apply {
            attachToRecyclerView(binding.rvRuns)
        }
    }

    private fun setupRecyclerView() = binding.rvRuns.apply {
        runAdapter = RunAdapter()
        adapter = runAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }
}