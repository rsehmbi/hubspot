package com.example.hubspot.schedule.CourseListViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.hubspot.schedule.Repository.repository

// View Model View Factory
class CourseOutlineViewModelFactory(private val repository: repository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CourseOutlineViewModel(repository) as T
    }
}