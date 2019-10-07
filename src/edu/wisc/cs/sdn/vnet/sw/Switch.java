package edu.wisc.cs.sdn.vnet.sw;

import net.floodlightcontroller.packet.Ethernet;
import edu.wisc.cs.sdn.vnet.Device;
import edu.wisc.cs.sdn.vnet.DumpFile;
import edu.wisc.cs.sdn.vnet.Iface;

import java.util.concurrent.*; 

/**
 * @author Aaron Gember-Jacobson
 */
public class Switch extends Device implements Runnable 
{	
	ConcurrentHashMap<MACAddress,swEntry> swTable;
	protected long timeout = 15000;

	class swEntry{
		long startTime;
		Iface inIface;
		swEntry(long l, Iface inface){
			this.startTime = l;
			this.inIface = inface;
		}
	}

	@Override
	public void run(){
		while(true){
			for(Map.Entry<MACAddress,swEntry> entry: this.wTable.entrySet()){
				long curTime = System.currentTimeMillis();
				long startTime = entry.getValue().starTime;
				if((curTime - startTime)>this.timeout){
					System.out.println("Timeout entry MAC: "+entry.getKey());
					this.swTable.remove(entry.getKey());
				}
			}
		}
	}
	/**
	 * Creates a router for a specific host.
	 * @param host hostname for the router
	 */
	public Switch(String host, DumpFile logfile)
	{
		super(host,logfile);
		swTable = new ConcurrentHashMap<MACAddress,swEntry>();
		Thread thread = new Thread(this);
		thread.start();
	}

	/**
	 * Handle an Ethernet packet received on a specific interface.
	 * @param etherPacket the Ethernet packet that was received
	 * @param inIface the interface on which the packet was received
	 */
	public void handlePacket(Ethernet etherPacket, Iface inIface)
	{
		System.out.println("*** -> Received packet: " +
                etherPacket.toString().replace("\n", "\n\t"));
		

		/********************************************************************/
		/* TODO: Handle packets                                             */
		MACAddress srcMac = etherPacket.getSourceMac();
		MACAddress dstMac = etherPacket.getDestinationMAC();
		if(swTable.contains(dstMac)){
			swEntry entry = swTable.get(dstMac);
			entry.startTime = System.currentTimeMillis();
			sendPacket(etherPacket, entry.inIface);
		}else{
			swEntry entry = new swEntry(System.currentTimeMillis(),inIface);
			swTable.add(srcMac,entry);
			//broadcast
			System.out.println("broad casting!");
			for(Map.Entry<String,Iface> interfaceEntry: this.interfaces.entrySet()){
				Iface curIface = interfaceEntry.getValue();
				if(!curIface.toString().equals(inIface.toString())){
					sendPacket(etherPacket,curIface);
				}
			}
		}
		/********************************************************************/
	}
}
