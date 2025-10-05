package net.p3pp3rf1y.sophisticatedstorage.init;

import com.github.salandora.sophisticatedlibrary.network.api.v1.PayloadRegistrar;
import net.p3pp3rf1y.sophisticatedstorage.network.*;

public class ModPayloads {
	private ModPayloads() {
	}

	public static void registerPayloads() {
		final PayloadRegistrar registrar = PayloadRegistrar.registrar();
		registrar.playToServer(OpenStorageInventoryPayload.TYPE, OpenStorageInventoryPayload.STREAM_CODEC, OpenStorageInventoryPayload::handlePayload);
		registrar.playToServer(RequestStorageContentsPayload.TYPE, RequestStorageContentsPayload.STREAM_CODEC, RequestStorageContentsPayload::handlePayload);
		registrar.playToClient(StorageContentsPayload.TYPE, StorageContentsPayload.STREAM_CODEC, StorageContentsPayload::handlePayload);
		registrar.playToServer(ScrolledToolPayload.TYPE, ScrolledToolPayload.STREAM_CODEC, ScrolledToolPayload::handlePayload);
		registrar.playToServer(RequestPlayerSettingsPayload.TYPE, RequestPlayerSettingsPayload.STREAM_CODEC, (payload, context) -> RequestPlayerSettingsPayload.handlePayload(context));
	}
}
