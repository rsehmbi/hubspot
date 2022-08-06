package com.example.hubspot.ratings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hubspot.R
import com.example.hubspot.ratings.ProfessorListViewModel.Professor
import com.example.hubspot.ratings.ProfessorListViewModel.ProfessorListViewModel
import com.google.firebase.database.*
import java.util.*
import kotlin.collections.ArrayList

class RatingsFragment : Fragment() {
    lateinit var autocompleteTextSearch: AutoCompleteTextView;
    lateinit var autoPopulateProfList: RecyclerView;
    private var suggestionProfList = ArrayList<String>()
    lateinit var profListViewModel: ProfessorListViewModel



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val profView: View = inflater.inflate(R.layout.fragment_ratings, container, false)
        autocompleteTextSearch = profView.findViewById(R.id.profAutoCompleteId)
        autoPopulateProfList = profView.findViewById(R.id.profListViewId)
        onClickButtonHandler(profView)
        val recycleLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(requireContext())
        autoPopulateProfList.layoutManager = recycleLayoutManager

        profListViewModel = ViewModelProvider(requireActivity())[ProfessorListViewModel::class.java]
        profListViewModel.getProfessorListSuggestions()?.observe(requireActivity(), Observer {
            suggestionProfList = it
            var courseListAdapter = ArrayAdapter(
                requireActivity(),
                android.R.layout.simple_list_item_1,
                suggestionProfList
            )
            autocompleteTextSearch.setAdapter(courseListAdapter)
            autocompleteTextSearch.onItemClickListener = AdapterView.OnItemClickListener{ parent, view, position, id ->
                val professorSelected = parent.getItemAtPosition(position).toString().uppercase()
                if(!profListViewModel.professorsSelectedList.contains(professorSelected)){
                    profListViewModel.professorsSelectedList.add(professorSelected)
                }

                //

                if(!isProfSelected(professorSelected)){
                    loadProfInfo(profListViewModel.professorReference, professorSelected)
                    Toast.makeText(requireContext(), "Professor $professorSelected added to the list", Toast.LENGTH_SHORT).show()
                }

                // clears the search box
                autocompleteTextSearch.getText().clear()
            }
            val adapter = ProfessorAdapter(profListViewModel.selectedProfessorList, requireActivity())
            autoPopulateProfList.adapter = adapter
        })
        return profView
    }

    private fun isProfSelected(profSelected: String): Boolean{
        for (prof in profListViewModel.selectedProfessorList ){
            if (profSelected.lowercase() == prof.profName.lowercase()){
                Toast.makeText(requireActivity(), "${changeDisplayName(profSelected)} is already added!", Toast.LENGTH_SHORT).show()
                return true
            }
        }
        return false
    }

    private fun loadProfInfo(databaseReference: DatabaseReference, profSelected: String) {
        val query: Query = databaseReference.orderByChild("ProfName").equalTo(profSelected)
        val valueListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    for (dataSnapshot in snapshot.children){
                        var rating: Float = -1.0F
                        if(dataSnapshot.child("Rating").child("Count").value.toString() != "0"){
                            var sum = dataSnapshot.child("Rating").child("Sum").value.toString().toFloat()
                            var count = dataSnapshot.child("Rating").child("Count").value.toString().toInt()
                            rating = sum/count
                        }
                        val selectedProf = Professor (
                            dataSnapshot.child("Area").value.toString(),
                            dataSnapshot.child("Department").value.toString(),
                            dataSnapshot.child("Email").value.toString(),
                            dataSnapshot.child("ImgUrl").value.toString(),
                            dataSnapshot.child("Occupation").value.toString(),
                            rating,
                            dataSnapshot.child("ProfName").value.toString()
                        )
                        profListViewModel.selectedProfessorList.add(selectedProf)
                    }
                    val adapter = ProfessorAdapter(profListViewModel.selectedProfessorList, requireActivity())
                    autoPopulateProfList.adapter = adapter
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
            }
        }
        query.addListenerForSingleValueEvent(valueListener)
    }

    override fun onResume() {
        // to update professor list after review update
        if(profListViewModel.isProfUpdated){
            profListViewModel.selectedProfessorList.clear()

            for (prof in profListViewModel.professorsSelectedList){
                loadProfInfo(profListViewModel.professorReference, prof)
            }


            val adapter = ProfessorAdapter(profListViewModel.selectedProfessorList, requireActivity())
            autoPopulateProfList.adapter = adapter
            profListViewModel.isProfUpdated = false
        }

        super.onResume()
    }

    private fun onClickButtonHandler(view: View){
        view.findViewById<Button>(R.id.clear_profs_btn_id).setOnClickListener {
            Toast.makeText(requireContext(), "Cleared the list of professors", Toast.LENGTH_SHORT).show()
            profListViewModel.professorsSelectedList.clear()
            profListViewModel.selectedProfessorList.clear()
            val adapter = ProfessorAdapter(profListViewModel.selectedProfessorList, requireActivity())
            autoPopulateProfList.adapter = adapter
        }
    }

    // to capitalize prof name
    private fun changeDisplayName(name: String): String{
        return name.split(" ").joinToString(" ") { it ->
            it.lowercase(Locale.getDefault())
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } }
    }
}