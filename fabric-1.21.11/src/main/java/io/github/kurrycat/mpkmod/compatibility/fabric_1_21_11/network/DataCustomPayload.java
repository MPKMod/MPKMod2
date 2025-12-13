package io.github.kurrycat.mpkmod.compatibility.fabric_1_21_11.network;

import io.github.kurrycat.mpknetapi.common.MPKNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record DataCustomPayload(byte[] data) implements CustomPacketPayload {
    public static final Type<DataCustomPayload> MPK_ID = new Type<>(Identifier.fromNamespaceAndPath(MPKNetworking.CHANNEL_NAMESPACE, MPKNetworking.CHANNEL_PATH));

    public static final StreamCodec<FriendlyByteBuf, DataCustomPayload> CODEC = StreamCodec.ofMember(
        (payload, buf) -> buf.writeBytes(payload.data()),
        buf -> {
            byte[] data = new byte[buf.readableBytes()];
            buf.readBytes(data);
            return new DataCustomPayload(data);
        }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return MPK_ID;
    }

    public static Type<DataCustomPayload> registerClientboundPayload() {
        PayloadTypeRegistry.playS2C().register(MPK_ID, DataCustomPayload.CODEC);
        return MPK_ID;
    }

    public static Type<DataCustomPayload> registerServerboundPayload() {
        PayloadTypeRegistry.playC2S().register(MPK_ID, DataCustomPayload.CODEC);
        return MPK_ID;
    }
}