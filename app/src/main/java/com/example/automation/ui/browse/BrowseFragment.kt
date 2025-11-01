package com.example.automation.ui.browse

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.automation.R
import com.example.automation.databinding.FragmentBrowseBinding
import com.example.automation.model.BrowseSuggestion
import com.example.automation.ui.AppViewModelFactory
import com.example.automation.ui.BrowseEvent
import com.example.automation.ui.BrowseViewModel
import com.example.automation.ui.ThemeViewModel
import com.example.automation.ui.theme.updateThemeMenuItem
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class BrowseFragment : Fragment() {
    private var _binding: FragmentBrowseBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModelFactory: AppViewModelFactory
    private val viewModel: BrowseViewModel by viewModels { viewModelFactory }
    private lateinit var themeViewModel: ThemeViewModel
    private lateinit var adapter: BrowseAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModelFactory = AppViewModelFactory(requireActivity().application)
        themeViewModel = ViewModelProvider(requireActivity(), viewModelFactory)[ThemeViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBrowseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolbar.title = getString(R.string.browse_title)
        binding.toolbar.inflateMenu(R.menu.menu_theme_only)
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_toggle_theme -> {
                    themeViewModel.toggleNightMode()
                    true
                }
                else -> false
            }
        }

        val themeItem = binding.toolbar.menu.findItem(R.id.action_toggle_theme)
        themeViewModel.themeMode.observe(viewLifecycleOwner) { mode ->
            updateThemeMenuItem(requireContext(), themeItem, mode)
        }

        adapter = BrowseAdapter(
            onPreview = { openSuggestion(it) },
            onAdd = { viewModel.addToLibrary(it) }
        )
        binding.browseList.layoutManager = LinearLayoutManager(requireContext())
        binding.browseList.adapter = adapter

        viewModel.browseItems.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is BrowseEvent.AddedToLibrary -> showMessage(getString(R.string.browse_added_message, event.title))
                        is BrowseEvent.AlreadyInLibrary -> showMessage(getString(R.string.browse_exists_message, event.title))
                    }
                }
            }
        }
    }

    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun openSuggestion(suggestion: BrowseSuggestion) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(suggestion.url))
        val chooser = Intent.createChooser(intent, suggestion.title)
        try {
            startActivity(chooser)
        } catch (ex: Exception) {
            showMessage(getString(R.string.browse_open_error))
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
