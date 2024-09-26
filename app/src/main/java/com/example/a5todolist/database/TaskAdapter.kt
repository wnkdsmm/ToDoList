// TaskAdapter.kt
package com.example.a5todolist

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.a5todolist.database.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TaskAdapter(
    diffCallback: DiffUtil.ItemCallback<Task>, private val coroutineScope: CoroutineScope
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(diffCallback) {
    private var sortedTasks: List<Task> = emptyList()
    private var onTaskClickListener: OnTaskClickListener? = null

    fun setOnTaskClickListener(listener: OnTaskClickListener) {
        onTaskClickListener = listener
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_item, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = sortedTasks[position]
        holder.bind(task)
    }

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        private val priorityTextView: TextView = itemView.findViewById(R.id.priorityTextView)
        private val priorityCircleImageView: ImageView = itemView.findViewById(R.id.priorityCircleImageView)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onTaskClickListener?.onTaskClick(sortedTasks[position])
                }
            }
        }

        fun bind(task: Task) {
            descriptionTextView.text = task.description
            priorityTextView.text = task.priority
            val diameter = 50 // Размер диаметра круга
            val priorityValue = when (task.priority?.toLowerCase()) {
                "high" -> {
                    priorityTextView.text = "1"
                    Color.parseColor("#FF5555")
                }
                "medium" -> {
                    priorityTextView.text = "2"
                    Color.parseColor("#FFC355")
                }
                "low" -> {
                    priorityTextView.text = "3"
                    Color.parseColor("#FFFF9C")
                }
                else -> {
                    priorityTextView.text = ""
                    Color.TRANSPARENT
                }
            }

            val circleBitmap = createCircleWithText(diameter, priorityValue)

            // Преобразовать Bitmap в Drawable и установить в ImageView
            val circleDrawable = BitmapDrawable(itemView.resources, circleBitmap)
            priorityCircleImageView.setImageDrawable(circleDrawable)
        }
        private fun createCircleWithText(diameter: Int, color: Int): Bitmap {
            val bitmap = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            paint.color = color

            // Рисование круга
            canvas.drawCircle((diameter / 2).toFloat(), (diameter / 2).toFloat(), (diameter / 2).toFloat(), paint)

            // Настройка параметров для рисования текста
            paint.color = Color.BLACK
            paint.textSize = 20f
            paint.textAlign = Paint.Align.CENTER

            // Рисование цифры в центре круга
            val text = priorityTextView.text.toString()
            val x = diameter / 2
            val y = diameter / 2 - (paint.descent() + paint.ascent()) / 2
            canvas.drawText(text, x.toFloat(), y, paint)

            return bitmap
        }

    }
    interface OnTaskClickListener {
        fun onTaskClick(task: Task)
    }

    fun setTasks(tasks: List<Task>) {
        sortedTasks = tasks.sortedBy { getPriorityValue(it.priority) }
        submitList(sortedTasks)
    }

    private fun getPriorityValue(priority: String?): Int {
        return when (priority?.toLowerCase()) {
            "high" -> 1
            "medium" -> 2
            "low" -> 3
            else -> 0 // Default value for unknown priorities
        }
    }

    class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }
    }

    fun updateTask(task: Task) {
        coroutineScope.launch(Dispatchers.Main) {
            val position = currentList.indexOfFirst { it.id == task.id }
            if (position != -1) {
                notifyItemChanged(position)
            }
        }
    }

    fun attachSwipeToDelete(recyclerView: RecyclerView) {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                // Not needed for swipe-to-delete
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Remove the swiped item from the adapter
                val position = viewHolder.adapterPosition
                val task = getItem(position)

                // Delete the task from the database
                GlobalScope.launch(Dispatchers.IO) {
                    MyApplication.database.taskDao().deleteTask(task)
                }

                // Update the list of tasks in the adapter
                setTasks(currentList.filter { it.id != task.id })
            }

        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }
}
fun Int.dpToPx(): Int {
    val scale = Resources.getSystem().displayMetrics.density
    return (this * scale).toInt()
}
class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
    override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
        return oldItem == newItem
    }
}