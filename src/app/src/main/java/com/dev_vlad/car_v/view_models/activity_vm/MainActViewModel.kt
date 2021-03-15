package com.dev_vlad.car_v.view_models.activity_vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.dev_vlad.car_v.models.persistence.auth.UserEntity
import com.dev_vlad.car_v.models.persistence.auth.UserRepo
import com.dev_vlad.car_v.models.persistence.cars.CarRepo
import com.dev_vlad.car_v.models.persistence.chat.ChatEntity
import com.dev_vlad.car_v.models.persistence.chat.ChatRepo
import com.dev_vlad.car_v.util.MyLogger
import com.dev_vlad.car_v.view_models.auth.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActViewModel(
    private val userRepo: UserRepo,
    private val carRepo: CarRepo,
    private val chatRepo: ChatRepo
) : ViewModel() {

    companion object {
        private val TAG = AuthViewModel::class.java.simpleName
    }

    val userState: LiveData<List<UserEntity>> = userRepo.user.asLiveData()


    /********* CHATS ****/
    fun listenForMyChatUpdates(ownerOrDealerField: String, userId: String) =
        chatRepo.listenForMyChatUpdates(ownerOrDealerField = ownerOrDealerField, userId = userId)

    fun getChatUpdates() = chatRepo.getChatUpdates()


    fun addChat(chat: ChatEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            MyLogger.logThis(TAG, "addChat() -to repo", chat.message)
            chatRepo.saveChatLocally(chat)
        }
    }

    fun updateChat(chat: ChatEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            MyLogger.logThis(TAG, "updatechat() -to repo", chat.message)
            chatRepo.updateChatLocally(chat)
        }
    }

    fun deleteChat(chat: ChatEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            MyLogger.logThis(TAG, "delete() -to repo", chat.message)
            chatRepo.deleteChatLocally(chat)
        }
    }


    override fun onCleared() {
        super.onCleared()
        chatRepo.removeListeners()
    }


}