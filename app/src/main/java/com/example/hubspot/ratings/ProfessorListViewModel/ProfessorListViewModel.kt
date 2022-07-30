package com.example.hubspot.ratings.ProfessorListViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*
import kotlin.collections.ArrayList

class ProfessorListViewModel: ViewModel()  {
    private var professorListSuggestions: MutableLiveData<ArrayList<String>>? = null
    var selectedProfessorList = ArrayList<Professor>()
    val professorReference =
        FirebaseDatabase.getInstance("https://hubspot-629d4-default-rtdb.firebaseio.com/").reference.child(
            "Professors"
        )

    fun getProfessorListSuggestions(): LiveData<ArrayList<String>>? {
        if (professorListSuggestions == null) {
            professorListSuggestions = MutableLiveData<ArrayList<String>>()
            loadSuggestions()
        }
        return professorListSuggestions
    }


    private fun loadSuggestions(){
        professorReference.addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var suggestions = ArrayList<String>()
                    for (dsp in dataSnapshot.children) {
                        val professorKey = dsp.key
                        suggestions.add(changeDisplayName(professorKey!!))
                    }
                    professorListSuggestions?.value = suggestions
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    //handle databaseError
                }
            })
    }

    // to capitalize prof name
    private fun changeDisplayName(name: String): String{
        return name.split(" ").joinToString(" ") { it ->
            it.lowercase(Locale.getDefault())
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } }
    }
}