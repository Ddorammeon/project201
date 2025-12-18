package com.uilover.project2002.Activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.uilover.project2002.Models.Film
import com.uilover.project2002.R
import androidx.recyclerview.widget.RecyclerView

import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.uilover.project2002.Adapter.FilmListAdapter

class SearchResultFragment : Fragment() {

    private var searchResults: ArrayList<Film>? = null
    private var searchQuery: String? = null

    companion object {
        fun newInstance(results: ArrayList<Film>, query: String): SearchResultFragment {
            val fragment = SearchResultFragment()
            val args = Bundle()
            args.putParcelableArrayList("results", results)
            args.putString("query", query)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            searchResults = it.getParcelableArrayList("results")
            searchQuery = it.getString("query")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search_result, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewSearch)
        val tvTitle = view.findViewById<TextView>(R.id.tvSearchTitle)
        val tvNoResults = view.findViewById<TextView>(R.id.tvNoResults)

        tvTitle.text = "Kết quả tìm kiếm: $searchQuery"

        if (searchResults.isNullOrEmpty()) {
            tvNoResults.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            tvNoResults.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = FilmListAdapter(searchResults!!)
        }

        return view
    }
}