package com.riteshknayak.cleo.Repo;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.riteshknayak.cleo.Models.Message;

import java.util.List;

@Dao
public interface MessagesDao {

    @Query("select * from messages")
    List<Message> getMessages();

    @Insert
    void addMessage(Message message);

    @Update
    void updateMessage(Message message);

    @Delete
    void deleteMessage(Message message);

}
