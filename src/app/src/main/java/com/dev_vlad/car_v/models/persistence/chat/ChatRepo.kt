package com.dev_vlad.car_v.models.persistence.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dev_vlad.car_v.util.*
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class ChatRepo(private val chatDao: ChatDao) {

    companion object {
        private val TAG = ChatRepo::class.java.simpleName
    }

    suspend fun sendMessageToFirestore(chatEntity: ChatEntity): String? {
        val chatCollection = Firebase.firestore.collection(CHATS_COLLECTION_NAME)
        val chatDoc = if (chatEntity.chatId == "") {
            val tmpDoc = chatCollection.document()
            chatEntity.chatId = tmpDoc.id
            tmpDoc
        } else {
            chatCollection.document(chatEntity.chatId)
        }

        return try {
            chatDoc.set(chatEntity).await()
            chatEntity.chatId
        } catch (exception: Exception) {
            MyLogger.logThis(TAG, "", "exception ${exception.message}", exception)
            null
        }

    }

    private val registeredListeners = ArrayList<ListenerRegistration>()
    private val chatUpdates = MutableLiveData<ChatEntityStateWrapper>()
    fun getChatUpdates(): LiveData<ChatEntityStateWrapper> = chatUpdates
    fun listenForMyChatUpdates(ownerOrDealerField: String, userId: String) {
        MyLogger.logThis(
            TAG,
            "listenForChatUpdates",
            "for user $userId of type $ownerOrDealerField"
        )
        val messages = Firebase.firestore.collection(CHATS_COLLECTION_NAME)
            .whereEqualTo(ownerOrDealerField, userId)

        val messagesListener = messages.addSnapshotListener { snapshots, e ->
            if (e != null) {
                MyLogger.logThis(TAG, "listenForChatUpdates", "exc ${e.message}", e)
                return@addSnapshotListener
            }

            for (dc in snapshots!!.documentChanges) {
                when (dc.type) {
                    DocumentChange.Type.ADDED -> {
                        MyLogger.logThis(
                            TAG,
                            "DocumentChange.Type.ADDED ",
                            "New chat: ${dc.document.data}"
                        )
                        try {
                            val newChat = dc.document.toObject<ChatEntity>()

                            chatUpdates.value = ChatEntityStateWrapper(
                                chat = newChat,
                                state = DataState.ADD
                            )
                        } catch (exception: Exception) {
                            MyLogger.logThis(
                                TAG,
                                "listenForChatUpdates",
                                "exc thrown ${exception.message}",
                                exception
                            )
                        }
                    }
                    DocumentChange.Type.MODIFIED -> {
                        MyLogger.logThis(
                            TAG,
                            "DocumentChange.Type.MODIFIED",
                            "Modified chat: ${dc.document.data}"
                        )
                        try {
                            val modifiedChat = dc.document.toObject<ChatEntity>()
                            chatUpdates.value = ChatEntityStateWrapper(
                                chat = modifiedChat,
                                state = DataState.UPDATE
                            )
                        } catch (exception: Exception) {
                            MyLogger.logThis(
                                TAG,
                                "listenForChatUpdates",
                                "exc thrown ${exception.message}",
                                exception
                            )
                        }
                    }
                    DocumentChange.Type.REMOVED -> {
                        MyLogger.logThis(
                            TAG,
                            "DocumentChange.Type.REMOVED",
                            "Removed chat: ${dc.document.data}"
                        )
                        try {
                            val deletedChat = dc.document.toObject<ChatEntity>()
                            chatUpdates.value = ChatEntityStateWrapper(
                                chat = deletedChat,
                                state = DataState.UPDATE
                            )
                        } catch (exception: Exception) {
                            MyLogger.logThis(
                                TAG,
                                "listenForChatUpdates",
                                "exc thrown ${exception.message}",
                                exception
                            )
                        }
                    }
                    else -> {
                    }
                }
            }
        }
        registeredListeners.add(messagesListener)

    }

    fun removeListeners() {
        for (listener in registeredListeners) {
            listener.remove()
        }
    }


    /***************** STD ROOM CRUD OPERATIONS ***/
    suspend fun saveChatLocally(chat: ChatEntity) {
        MyLogger.logThis(TAG, "saveChatLocally()", chat.message)
        chatDao.insert(chat)
    }

    suspend fun updateChatLocally(chat: ChatEntity) {
        MyLogger.logThis(TAG, "updateChatLocally()", chat.message)
        chatDao.update(chat)
    }

    suspend fun deleteChatLocally(chat: ChatEntity) {
        MyLogger.logThis(TAG, "deleteChatLocally()", chat.message)
        chatDao.delete(chat)
    }

    fun fetchMessages(
        pageNo: Int,
        carId: String,
        ownerId: String,
        dealerId: String
    ): Flow<List<ChatEntity>> {
        val page = if (pageNo < 1) 1 else pageNo //safety
        val offset = if (page == 1) {
            0
        } else {
            CHATS_RECYCLER_PAGE_SIZE * (page - 1)
        }
        return chatDao.getMessages(
            carId = carId,
            ownerId = ownerId,
            dealerId = dealerId,
            limit = CHATS_RECYCLER_PAGE_SIZE,
            offset = offset
        )
    }


}


enum class DataState {
    DELETE,
    UPDATE,
    ADD
}

data class ChatEntityStateWrapper(val chat: ChatEntity, val state: DataState)