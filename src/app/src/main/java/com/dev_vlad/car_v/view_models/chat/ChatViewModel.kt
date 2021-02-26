package com.dev_vlad.car_v.view_models.chat

import androidx.lifecycle.*
import com.dev_vlad.car_v.models.persistence.auth.UserEntity
import com.dev_vlad.car_v.models.persistence.auth.UserRepo
import com.dev_vlad.car_v.models.persistence.chat.ChatEntity
import com.dev_vlad.car_v.models.persistence.chat.ChatInitiateData
import com.dev_vlad.car_v.models.persistence.chat.ChatRepo
import com.dev_vlad.car_v.util.RECYCLER_PAGE_SIZE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatViewModel(private val chatRepo: ChatRepo, private val userRepo: UserRepo) : ViewModel() {

    lateinit var chatInitiateData: ChatInitiateData
    fun setInitializationData(chatInitializationData: ChatInitiateData) {
        chatInitiateData = chatInitializationData
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

    private var loadedPage = MutableLiveData<Int>(1)
    fun observeMessages(): LiveData<List<ChatEntity>> = loadedPage.switchMap {
        chatRepo.fetchMessages(
            pageNo = loadedPage.value?:1,
            carId = chatInitiateData.carId,
            ownerId = chatInitiateData.ownerId,
            dealerId = chatInitiateData.dealerId
        )
            .asLiveData()
    }

    var isLoading = false
    fun fetchMoreCars(totalItemsInListNow: Int) {
        if (!isLoading){
            isLoading = true
            val itemsLoaded = if (totalItemsInListNow > 0) totalItemsInListNow else 1
            val currentPage = (itemsLoaded / RECYCLER_PAGE_SIZE).toInt()
            val nextPage = currentPage + 1
            loadedPage.value = nextPage // trigger reload

        }
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
            sentOn = System.currentTimeMillis(),
            messageIsImage = false
        )
        viewModelScope.launch(Dispatchers.IO) {
            val serverChatId = chatRepo.sendMessageToFireStore(chatEntity)
             //serverChatId will be null if something went wrong
        }
    }

    fun sendImageMessage(uriStr: String) {
        val chatEntity = ChatEntity(
                chatId = "",
                ownerId = chatInitiateData.ownerId,
                dealerId = chatInitiateData.dealerId,
                carId = chatInitiateData.carId,
                message = uriStr,
                sentByOwner = currentUser.value?.isSeller ?: false,
                sentByDealer = currentUser.value?.isDealer ?: false,
                sentOn = System.currentTimeMillis(),
                messageIsImage = true
        )
        viewModelScope.launch(Dispatchers.IO) {
            val serverChatId = chatRepo.sendImageMessageToFireStore(chatEntity, currentUser.value!!.userId)
            //serverChatId will be null if something went wrong
        }
    }



}