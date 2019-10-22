package by.vadim_churun.ordered.speechman2.model.lack_info

import by.vadim_churun.ordered.speechman2.model.warning.DataWarning
import by.vadim_churun.ordered.speechman2.remote.lack.DataLack


class DataLackInfosRequest(
    val requestID: Int,
    val entities: List<Any>,
    val lacks: List<DataLack<*, *>>,
    val warnings: List<DataWarning<*>>
)