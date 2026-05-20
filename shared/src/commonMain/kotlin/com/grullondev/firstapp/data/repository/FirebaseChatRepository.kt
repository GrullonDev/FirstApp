package com.grullondev.firstapp.data.repository

import com.grullondev.firstapp.domain.model.*
import com.grullondev.firstapp.domain.repository.ChatRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FirebaseChatRepository : ChatRepository {
    private val firestore = Firebase.firestore

    override fun getChats(): Flow<List<Chat>> =
        firestore.collection("chats")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.map { it.data<Chat>() }
            }

    override fun getMessages(chatId: String): Flow<List<ChatMessage>> =
        firestore.collection("chats").document(chatId).collection("messages")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.map { it.data<ChatMessage>() }
            }

    override suspend fun sendMessage(chatId: String, text: String, type: MessageType, fileName: String?) {
        val message = ChatMessage(
            id = "", // Se genera automáticamente en Firestore o se puede asignar uno
            text = text,
            isMine = true,
            time = "10:00 AM", // En una app real usaríamos un timestamp
            type = type,
            fileName = fileName
        )
        firestore.collection("chats").document(chatId).collection("messages").add(data = message)
    }

    override suspend fun markAllAsRead() {
        // Lógica para marcar como leído en Firebase
    }

    override suspend fun togglePin(chatId: String) {
        // Lógica para anclar chat en Firebase
    }
}
