package com.dev_vlad.car_v.view_models.chat

import androidx.lifecycle.*
import com.dev_vlad.car_v.models.persistence.auth.UserEntity
import com.dev_vlad.car_v.models.persistence.auth.UserRepo
import com.dev_vlad.car_v.models.persistence.chat.ChatEntity
import com.dev_vlad.car_v.models.persistence.chat.ChatInitiateData
import com.dev_vlad.car_v.models.persistence.chat.ChatRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatViewModel(private val chatRepo: ChatRepo, private val userRepo: UserRepo) : ViewModel() {

    lateinit var chatInitiateData: ChatInitiateData
    fun initializedData(chatInitializationData: ChatInitiateData) {
        chatInitiateData = chatInitializationData
    }

    init {
        initUser()
    }

    private val currentUser = MutableLiveData<UserEntity>()
    fun observeCurrentUser(): LiveData<UserEntity> = currentUser
    private fun initUser() {
        viewModelScope.launch(Dispatchers.IO) {
            val user = userRepo.getNonObservableUser()[0]
            withContext(Dispatchers.Main) {
                currentUser.value = user
            }
        }
    }

    private var currentPage = 1
    fun observeMessages(): LiveData<List<ChatEntity>> {
        return chatRepo.fetchMessages(
            currentPage,
            carId = chatInitiateData.carId,
            ownerId = chatInitiateData.ownerId,
            dealerId = chatInitiateData.dealerId
        )
            .asLiveData()
    }


    fun sendNewMessage(newMsg: String) {
        val chatEntity = ChatEntity(
            chatId = "",
            ownerId = chatInitiateData.ownerId,
            dealerId = chatInitiateData.dealerId,
            carId = chatInitiateData.carId,
            message = newMsg,
            sentByOwner = currentUser.value?.isSeller ?: false,
            sentByDealer = currentUser.value?.isDealer ?: false,
            sentOn = System.currentTimeMillis()
        )
        viewModelScope.launch(Dispatchers.IO) {
            val serverChatId = chatRepo.sendMessageToFirestore(chatEntity)
             //serverChatId will be null if something went wrong
        }
    }

}