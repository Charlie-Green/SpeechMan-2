package by.vadim_churun.ordered.speechman2.model.objects

import by.vadim_churun.ordered.speechman2.db.entities.Person


class PersonHeader(
    val person: Person,
    val countAppoints: Int?,
    val countOrders: Int?
)