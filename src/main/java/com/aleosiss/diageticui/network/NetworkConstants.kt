package com.aleosiss.diageticui.network

import com.aleosiss.diageticui.DiageticUI
import net.minecraft.util.Identifier

object NetworkConstants {
    @JvmField
    val DIAGETIC_INVENTORY_REQUEST_PACKET = Identifier(DiageticUI.MOD_ID, "inventory_request_packet")
    @JvmField
    val DIAGETIC_INVENTORY_RESPONSE_PACKET = Identifier(DiageticUI.MOD_ID, "inventory_response_packet")
}
