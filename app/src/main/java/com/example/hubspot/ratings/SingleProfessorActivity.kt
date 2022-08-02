/*
Displays the rating and additional information about the professors

 */


package com.example.hubspot.ratings

import android.app.Activity
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.hubspot.NetworkUtil
import com.example.hubspot.R
import com.example.hubspot.ratings.ProfessorListViewModel.ProfessorListViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.util.*


class SingleProfessorActivity : AppCompatActivity() {

    private lateinit var profImage: ImageView
    private lateinit var profName: TextView
    private lateinit var profDepartment: TextView
    private lateinit var profOccupation: TextView
    private lateinit var profEmail: TextView
    private lateinit var profArea: TextView
    private lateinit var rateNowBtn: Button
    private lateinit var profListViewModel: ProfessorListViewModel
    private lateinit var curActivity: Activity


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(NetworkUtil.isOnline(this)){
            setContentView(R.layout.activity_single_professor)
            curActivity = this

            // getting prof_name (which is unique)
            val selectedProfName = intent.getStringExtra("PROF_NAME")

            // getting references of the views in the activity
            getViewReferences()

            // calling listener on rate now btn
            onClickButtonHandler()

            // displaying prof's info
            loadProfInfo(selectedProfName!!)

            // case 1: view a list of reviews for this professor
            val reviewListFragments = ReviewDisplayFragment()
            val arg = Bundle()
            arg.putString("PROF_NAME", selectedProfName)
            reviewListFragments.arguments = arg
            replaceFragment(reviewListFragments)
        }
        else{
            setContentView(R.layout.activity_offline)
            val closeBtn: Button = findViewById(R.id.close_btn_id)
            closeBtn.setOnClickListener {
                finish()
            }
        }
    }

    private fun getViewReferences(){
        profImage = findViewById(R.id.profImageId)
        profName = findViewById(R.id.profNameId)
        profDepartment = findViewById(R.id.profDepartmentId)
        profOccupation = findViewById(R.id.profOccupationId)
        profEmail = findViewById(R.id.profEmailId)
        profArea = findViewById(R.id.profAreaId)
        rateNowBtn = findViewById(R.id.rate_now_btn_id)
    }

    private fun onClickButtonHandler(){
        rateNowBtn.setOnClickListener {
            if(rateNowBtn.text == "Rate Now!"){
                rateNowBtn.text = "View Reviews"
                val reviewAddFragments = ReviewAddFragment()
                val arg = Bundle()
                val selectedProfName = intent.getStringExtra("PROF_NAME")
                arg.putString("PROF_NAME", selectedProfName)
                reviewAddFragments.arguments = arg
                replaceFragment(reviewAddFragments)
            }
            else{
                rateNowBtn.text = "Rate Now!"
                val reviewListFragments = ReviewDisplayFragment()
                val arg = Bundle()
                val selectedProfName = intent.getStringExtra("PROF_NAME")
                arg.putString("PROF_NAME", selectedProfName)
                reviewListFragments.arguments = arg
                replaceFragment(reviewListFragments)
            }
        }
    }

    // to capitalize first letter of prof's first and last name
    private fun changeDisplayName(name: String): String{
        return name.split(" ").joinToString(" ") { it ->
            it.lowercase(Locale.getDefault())
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } }
    }

    // loads prof's information to be displayed
    private fun loadProfInfo(selectedProfName: String){
        // loading the values from the database
        profListViewModel = ViewModelProvider(this)[ProfessorListViewModel::class.java]

        val query: Query = profListViewModel.professorReference.orderByChild("ProfName").equalTo(selectedProfName)
        val valueListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    for (dataSnapshot in snapshot.children){

                        // setting the prof's image
                        // fetching image from the url and setting it to profImage
                        Picasso.with(curActivity).load(dataSnapshot.child("ImgUrl").value.toString()).into(profImage)

                        profName.text = "Name: ${changeDisplayName(dataSnapshot.child("ProfName").value.toString())}"
                        profDepartment.text = "Department: ${dataSnapshot.child("Department").value.toString()}"
                        profOccupation.text =  "Occupation: ${changeDisplayName(dataSnapshot.child("Occupation").value.toString())}"
                        profEmail.text = "Email: ${dataSnapshot.child("Email").value.toString()}"
                        profArea.text = "Area: ${dataSnapshot.child("Area").value.toString()}"

                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
            }
        }
        query.addListenerForSingleValueEvent(valueListener)
    }

    // changes the fragment that frame layout is currently is holding
    private fun replaceFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction().replace(R.id.frame_layout_3_displays,fragment).commit()
    }

}