package ru.littleligr;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.impl.command.client.ClientCommandInternals;

public class CreatopicoUtilsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			ClientCommandInternals.executeCommand("bobby upgrade");
		});
	}
}