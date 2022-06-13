package com.example.doitsoon.ui.list

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.doitsoon.R
import com.example.doitsoon.data.SortOrder
import com.example.doitsoon.databinding.ListFragmentBinding
import com.example.doitsoon.ui.addedittask.AddEditTaskDialogFragment
import com.example.doitsoon.ui.list.adapter.TaskAdapter
import com.example.doitsoon.ui.list.adapter.listitem.TaskItem
import com.example.doitsoon.util.onQueryTextChanged
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ListFragment : Fragment(R.layout.list_fragment),TaskAdapter.onTaskClickedListener {

    companion object {
        fun newInstance() = ListFragment()
    }

    private lateinit var binding: ListFragmentBinding

    private val viewModel: ListViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ListFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeList()
        setListeners()
        setHasOptionsMenu(true)
        setObservers()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
       inflater.inflate(R.menu.menu_fragment_tasks,menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView


        searchView.onQueryTextChanged {
            viewModel.searchQuery.value = it
        }

        viewLifecycleOwner.lifecycleScope.launch{
            menu.findItem(R.id.action_hide_completed).isChecked =
            viewModel.preferencesFlow.first().hideCompleted
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
       return when(item.itemId){
            R.id.action_sort_by_name -> {
                context?.let { viewModel.onSortOrderSelected(SortOrder.BY_NAME_DESC, it) }
                true
            }
            R.id.action_sort_by_date -> {
                context?.let { viewModel.onSortOrderSelected(SortOrder.BY_DATE, it) }
                true
            }
            R.id.action_hide_completed -> {
                item.isChecked = !item.isChecked
                context?.let { viewModel.onHideCompletedClicked(item.isChecked, it) }
                true
            }
            R.id.action_delete_completed ->{

                true
            }
           else -> super.onOptionsItemSelected(item)
       }
    }

    private fun initializeList() {

        val taskAdapter = TaskAdapter(this)
        with(binding.taskRecyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = taskAdapter
            setHasFixedSize(true)
        }

        viewModel.tasks.observe(viewLifecycleOwner){
            taskAdapter.submitList(it)
        }

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val task = taskAdapter.currentList[viewHolder.adapterPosition]
                viewModel.onTaskSwipe(task)
            }

        }).attachToRecyclerView(binding.taskRecyclerView)

    }

    private fun setListeners(){
        with(binding){
            addButton.setOnClickListener {
                AddEditTaskDialogFragment(
                    onSaveClickListener = { task ->
                        viewModel.addTask(task)
                    }
                ).show(parentFragmentManager,"Dialog")
            }
        }
    }


    private fun setObservers(){
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.taskEvents.collect { event ->
                when(event){
                    is ListViewModel.TaskEvents.ShowUndoDeleteTaskEvent -> {
                        Snackbar.make(requireView(),"Task Deleted",Snackbar.LENGTH_LONG)
                            .setAction("UNDO"){
                                viewModel.undoDeletedTask(event.task)
                            }
                            .show()
                    }
                }
            }
        }
    }

    override fun onItemClick(taskItem: TaskItem) {}

    override fun onItemCheckBoxClicked(taskItem: TaskItem, isChecked: Boolean) {
        viewModel.onTaskCheckedChanged(taskItem,isChecked)
    }

}