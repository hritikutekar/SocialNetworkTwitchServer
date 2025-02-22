package com.plcoding.routes

import com.plcoding.service.ChatService
import com.plcoding.util.Constants
import com.plcoding.util.QueryParams
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach

fun Route.getMessagesForChat(chatService: ChatService) {
    authenticate {
        get("/api/chat/messages") {
            val chatId = call.parameters[QueryParams.PARAM_CHAT_ID] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val page = call.parameters[QueryParams.PARAM_PAGE]?.toIntOrNull() ?: 0
            val pageSize = call.parameters[QueryParams.PARAM_PAGE_SIZE]?.toIntOrNull() ?: Constants.DEFAULT_PAGE_SIZE

            if (!chatService.doesChatBelongToUser(chatId, call.userId)) {
                call.respond(HttpStatusCode.Forbidden)
                return@get
            }

            val messages = chatService.getMessagesForChat(chatId, page, pageSize)
            call.respond(HttpStatusCode.OK, messages)
        }
    }
}

fun Route.getChatsForUser(chatService: ChatService) {
    authenticate {
        get("/api/chats") {
            val chats = chatService.getChatsForUser(call.userId)
            call.respond(HttpStatusCode.OK, chats)
        }
    }
}

fun Route.chatWebSocket(chatService: ChatService) {
    webSocket("/api/chat/websocket") {
        incoming.consumeEach { frame ->
            when (frame) {
                is Frame.Text -> {
                    if (frame.readText() == "Hello World") {
                        send(Frame.Text("Yo, what's up"))
                    }
                }
            }
        }
    }
}