package com.example.a5todolist
import android.content.Intent
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import com.example.a5todolist.database.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class AddTaskActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_task)

        val highRadioButton = findViewById<RadioButton>(R.id.highRadioButton)
        highRadioButton.isChecked = true

        val addButton: Button = findViewById(R.id.add_button)
        addButton.setOnClickListener {
            val description = findViewById<EditText>(R.id.describe).text.toString()

            // Проверка, что поле описания не пустое
            if (description.isBlank()) {
                // Если описание пустое, просто вернемся на предыдущую активность
                finish()
            } else {
                val priorityRadioGroup = findViewById<RadioGroup>(R.id.priorityRadioGroup)
                val selectedPriority =
                    findViewById<RadioButton>(priorityRadioGroup.checkedRadioButtonId).text.toString()

                val task = Task(description = description, priority = selectedPriority)

                // Вставка задачи в базу данных
                CoroutineScope(Dispatchers.IO).launch {
                    MyApplication.database.taskDao().insert(task)
                }

                // Возвращение к предыдущей активности (MainActivity)
                finish()
            }
        }
    }
}