package com.example.jgi.ui.showItems

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.navigation.fragment.findNavController
import com.example.jgi.BaseFragment
import com.example.jgi.R

class ShowItemsFragment : BaseFragment() {

    companion object {
        fun newInstance() = ShowItemsFragment()
    }

    private lateinit var viewModel: ShowItemsViewModel
    private lateinit var cardView: CardView

    override fun onBackPressed(): Boolean {
        findNavController().popBackStack()
        return true
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.show_items_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cardView=view.findViewById(R.id.card_view)

        cardView.setOnClickListener {
            findNavController().navigate(R.id.destination_maps)
        }
    }
}