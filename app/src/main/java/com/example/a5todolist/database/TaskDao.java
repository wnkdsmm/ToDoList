package com.example.a5todolist.database;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import org.jetbrains.annotations.NotNull;

import java.util.List;

@Dao
public interface TaskDao {
    @Insert
    void insert(Task task);

    @Query("SELECT * FROM tasks")
    List<Task> getAllTasks();

    @Delete
    void deleteTask(Task task);

    @Update
    void updateTask(Task task);
}
