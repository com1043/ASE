package ase.appConnect.channel;

public interface ChannelReceiveCallback
{
	public void receiveData(Channel ch, byte[][] data);
}