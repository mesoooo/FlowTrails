package xyz.mesdev.flowtrails;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public interface ClientBridge {
	Screen currentScreen(Minecraft mc);

	void openScreen(Minecraft mc, Screen screen);

	void showStatus(Minecraft mc, Component message);
}
