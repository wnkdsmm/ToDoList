package com.example.a5todolist

import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.example.a5todolist.database.AppDatabase
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.ActionBar
import androidx.lifecycle.lifecycleScope
import com.example.a5todolist.database.Task

class MainActivity : AppCompatActivity() {

    private lateinit var taskAdapter: TaskAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val actionBar: ActionBar? = supportActionBar
        actionBar?.title = "To-Do List"

        recyclerView = findViewById(R.id.recyclerView)
        taskAdapter = TaskAdapter(TaskDiffCallback(), lifecycleScope)

        // Attach swipe-to-delete functionality to the RecyclerView
        taskAdapter.attachSwipeToDelete(recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = taskAdapter

        if (MyApplication.isDatabaseInitialized()) {
            // Load tasks from the database
            loadTasks()
        } else {
            // Обработка случая, когда база данных не инициализирована
        }


        val addButton: FloatingActionButton = findViewById(R.id.AddTask)
        addButton.setOnClickListener {
            // Creating an intent to navigate to the AddTaskActivity
            val intent = Intent(this@MainActivity, AddTaskActivity::class.java)

            // Starting the activity
            startActivity(intent)
        }

    }

    fun onTaskClick(task: Task) {
        // Handle the click on the task for editing
        updateTask(task)
    }

    private fun updateTask(task: Task) {
        val updatedDescription = "Новое описание"
        task.description = updatedDescription

        lifecycleScope.launch(Dispatchers.IO) {
            MyApplication.database.taskDao().updateTask(task)
        }

        // Update the list of tasks in the adapter
        lifecycleScope.launch(Dispatchers.Main) {
            taskAdapter.updateTask(task)
        }
    }

    override fun onResume() {
        super.onResume()
        loadTasks()
    }
    private fun loadTasks() {
        if (MyApplication.isDatabaseInitialized()) {
            GlobalScope.launch(Dispatchers.IO) {
                val tasks = MyApplication.database.taskDao().getAllTasks()

                withContext(Dispatchers.Main) {
                    // Используйте setTasks для установки и сортировки задач
                    taskAdapter.setTasks(tasks)
                }
            }
        } else {
            // Обработка случая, когда база данных не инициализирована
        }
    }

}

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "task-database"
        ).build()
    }

    companion object {
        lateinit var database: AppDatabase
            private set

        fun isDatabaseInitialized(): Boolean {
            return this::database.isInitialized
        }
    }


}
