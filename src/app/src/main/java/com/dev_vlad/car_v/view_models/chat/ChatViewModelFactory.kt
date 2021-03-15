package com.dev_vlad.car_v.view_models.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dev_vlad.car_v.models.persistence.auth.UserRepo
import com.dev_vlad.car_v.models.persistence.chat.ChatRepo

class ChatViewModelFactory(
    private val userRepo: UserRepo,
    private val chatRepo: ChatRepo
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(
                userRepo = userRepo,
                chatRepo = chatRepo,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}