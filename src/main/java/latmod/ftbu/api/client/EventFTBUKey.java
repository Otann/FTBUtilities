package latmod.ftbu.api.client;

import latmod.ftbu.api.EventLM;

public class EventFTBUKey extends EventLM
{
	public final int key;
	public final boolean pressed;
	
	public EventFTBUKey(int k, boolean p)
	{
		key = k;
		pressed = p;
	}
}