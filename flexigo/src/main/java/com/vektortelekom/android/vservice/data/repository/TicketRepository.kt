package com.vektortelekom.android.vservice.data.repository

import com.vektortelekom.android.vservice.data.model.CreateTicketRequest
import com.vektortelekom.android.vservice.data.model.GetDestinationRoutesRequest
import com.vektortelekom.android.vservice.data.model.GetDestinationRoutesResponse
import com.vektortelekom.android.vservice.data.remote.service.TicketService
import com.vektortelekom.android.vservice.data.remote.service.UserService
import io.reactivex.Observable
import javax.inject.Inject

class TicketRepository
@Inject
constructor(
        private val ticketService: TicketService,
        private val userService: UserService
) {

    fun getTickets(langCode: String?) = ticketService.getTickets(langCode?:"tr")

    fun getTicketTypes(langCode: String?) = ticketService.getTicketTypes(langCode ?: "tr")

    fun getDestinations() = ticketService.getDestinations()

    fun getDestinationRoutes(id: Long): Observable<GetDestinationRoutesResponse> {
        return ticketService.getDestinationRoutes(GetDestinationRoutesRequest((id)))
    }

    fun createTicket(request: CreateTicketRequest) = ticketService.createTicket(request)

}