package com.magic.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.magic.data.repositories.MuseMagicRepositoryImpl
import com.magic.ui.databinding.FragmentTestBinding
import kotlinx.coroutines.launch

class Test : Fragment() {

    private var _binding: FragmentTestBinding? = null
    private val repository = MuseMagicRepositoryImpl()

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment using view binding
        _binding = FragmentTestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // You can now access the views directly
        binding.textView.text = "Hello, View Binding!"
        binding.scanButton.setOnClickListener {
            lifecycleScope.launch {
                val data = repository.getAnchorList()
                binding.textView.text = data[1].anchor
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Null out the binding object
        _binding = null
    }
}