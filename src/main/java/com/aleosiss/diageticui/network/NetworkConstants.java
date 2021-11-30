package com.aleosiss.diageticui.network;

import com.aleosiss.diageticui.DiageticUI;
import net.minecraft.util.Identifier;

public class NetworkConstants {
    public static final Identifier DIAGETIC_INVENTORY_REQUEST_PACKET = new Identifier(DiageticUI.MOD_ID, "inventory_request_packet");
    public static final Identifier DIAGETIC_INVENTORY_RESPONSE_PACKET = new Identifier(DiageticUI.MOD_ID, "inventory_response_packet");
}
