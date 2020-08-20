package logging

import generics.IdTable

object LoggingTable : IdTable() {
    val log = text("log")
    val cause = text("cause").nullable()
}
