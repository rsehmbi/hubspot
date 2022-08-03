package com.example.hubspot.ratings

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.hubspot.R
import com.example.hubspot.ratings.ProfessorListViewModel.Professor
import com.example.hubspot.ratings.ProfessorListViewModel.ProfessorListViewModel
import com.squareup.picasso.Picasso
import java.util.*
import kotlin.collections.ArrayList


class ProfessorAdapter(private val dataSet: ArrayList<Professor>, private val fragmentActivity: FragmentActivity) :
    RecyclerView.Adapter<ProfessorAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profImage: ImageView
        val profName: TextView
        val profRating: TextView
        val profDepartment: TextView
        val ratingBar: RatingBar

        init {
            profImage = view.findViewById(R.id.profImageId)
            profName = view.findViewById(R.id.profNameId)
            profRating = view.findViewById(R.id.profRatingId)
            profDepartment = view.findViewById(R.id.profDepartmentId)
            ratingBar = view.findViewById(R.id.ratingBarDisplayId)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.professor, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        val selectedProf = dataSet[position]
        viewHolder.profName.text = changeDisplayName(selectedProf.profName)
        if(selectedProf.rating < 0.0F){
            viewHolder.profRating.text = "Rating: No reviews yet"
        }
        else{
            viewHolder.profRating.text = "Rating: ${selectedProf.rating}"
            viewHolder.ratingBar.rating = selectedProf.rating
        }

        viewHolder.profDepartment.text = "Department: ${selectedProf.department}"

        // setting the prof's image
            // fetching image from the url and setting it to profImage
        Picasso.with(viewHolder.itemView.context).load(selectedProf.imgUrl).into(viewHolder.profImage)


        viewHolder.itemView.setOnClickListener {

            val profListViewModel = ViewModelProvider(fragmentActivity)[ProfessorListViewModel::class.java]
            profListViewModel.isProfUpdated = true

            val intent = Intent(it.context, SingleProfessorActivity::class.java).apply {
                putExtra("PROF_NAME", selectedProf.profName) // Assuming that name of professors are unique
            }
            it.context.startActivity(intent)
        }
    }

    override fun getItemCount() = dataSet.size

    // to capitalize prof name
    private fun changeDisplayName(name: String): String{
        return name.split(" ").joinToString(" ") { it ->
            it.lowercase(Locale.getDefault())
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } }
    }
}