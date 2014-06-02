package pl.asie.computronics;

import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;

import paulscode.sound.SoundBuffer;
import paulscode.sound.SoundSystem;
import pl.asie.computronics.gui.GuiTapePlayer;
import pl.asie.computronics.tile.TileTapeDrive;
import pl.asie.computronics.tile.TileTapeDrive.State;
import pl.asie.lib.AsieLibMod;
import pl.asie.lib.audio.DFPWM;
import pl.asie.lib.audio.StreamingAudioPlayer;
import pl.asie.lib.network.NetworkHandlerBase;
import pl.asie.lib.network.PacketOutput;
import pl.asie.lib.util.GuiUtils;
import pl.asie.lib.util.WorldUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.event.sound.PlayStreamingSourceEvent;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class NetworkHandler extends NetworkHandlerBase implements IPacketHandler {
	private static final AudioFormat DFPWM_DECODED_FORMAT = new AudioFormat(32768, 8, 1, false, false);
	
	@Override
	public void handlePacket(INetworkManager manager, PacketOutput packet, int command, Player player,
			boolean isClient) throws IOException {
		switch(command) {
			case Packets.PACKET_TAPE_GUI_STATE: {
				TileEntity entity = isClient ? packet.readTileEntity() : packet.readTileEntityServer();
				State state = State.values()[packet.readUnsignedByte()];
				if(isClient) {
					int volume = packet.readByte() & 127;
				}
				if(entity instanceof TileTapeDrive) {
					TileTapeDrive tile = (TileTapeDrive)entity;
					tile.switchState(state);
				}
			} break;
			case Packets.PACKET_AUDIO_DATA: {
				if(!isClient) return;
				int dimId = packet.readInt();
				int x = packet.readInt();
				int y = packet.readInt();
				int z = packet.readInt();
				int packetId = packet.readInt();
				int codecId = packet.readInt();
				short packetSize = packet.readShort();
				short volume = packet.readByte();
				byte[] data = packet.readByteArrayData(packetSize);
				byte[] audio = new byte[packetSize * 8];
				String sourceName = "dfpwm_"+codecId;
				StreamingAudioPlayer codec = Computronics.instance.audio.getPlayer(codecId);

				if(dimId != WorldUtils.getCurrentClientDimension()) return;
				
				codec.decompress(audio, data, 0, 0, packetSize);
				for(int i = 0; i < (packetSize * 8); i++) {
					// Convert signed to unsigned data
					audio[i] = (byte)(((int)audio[i] & 0xFF) ^ 0x80);
				}
				
				if((codec.lastPacketId + 1) != packetId) {
					codec.reset();
				}
				codec.setSampleRate(packetSize * 32);
				codec.setDistance((float)Computronics.TAPEDRIVE_DISTANCE);
				codec.setVolume(volume/127.0F);
				codec.playPacket(audio, x, y, z);
				codec.lastPacketId = packetId;
			} break;
			case Packets.PACKET_AUDIO_STOP: {
				if(!isClient) return;
				int codecId = packet.readInt();
				Computronics.instance.audio.removePlayer(codecId);
			} break;
		}
	}
}