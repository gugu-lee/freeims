package org.freeims.ims.rtpproxy;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Vector;

import javax.sdp.Connection;
import javax.sdp.Media;
import javax.sdp.MediaDescription;
import javax.sdp.SdpFactory;
import javax.sip.header.FromHeader;
import javax.sip.header.ToHeader;

import org.apache.log4j.Logger;
import org.freeims.sipproxy.servlet.SipProxyMessage;
import org.freeims.sipproxy.servlet.SipProxyRequest;
import org.freeims.sipproxy.servlet.SipProxyResponse;
import org.freeims.sipproxy.servlet.impl.SipProxyMessageImpl;
import org.freeims.javax.sdp.SessionDescriptionImpl;
import org.freeims.javax.sdp.fields.AttributeField;
import org.freeims.javax.sip.message.SIPMessage;

public class RtpProxyControlClient {

	private Logger logger = Logger.getLogger(RtpProxyControlClient.class);
	private static final String CONTENT_TYPE_SDP = "application/sdp";
	private String ctrlHost;
	private String proxyHost;
	private int ctrlPort;

	public RtpProxyControlClient(String crtlHost, int crtlPort,String proxyHost) {
		this.ctrlHost = crtlHost;
		this.ctrlPort = crtlPort;
		this.proxyHost = proxyHost;
	}

	public boolean testRtpProxy() {
		return true;
	}

	public void applyRtpProxy(SipProxyMessage message) {

		if ( message.getContentLength() == 0 ||  !message.getContentType().equals("application/sdp") )
		{
			return ;
		}
		SdpFactory sdpFactory = SdpFactory.getInstance();
		try {
			SessionDescriptionImpl sd = (SessionDescriptionImpl)sdpFactory.createSessionDescription(new String(message.getRawContent()));
			String noRtpProxy = sd.getAttribute("nortpproxy");
			if (noRtpProxy !=null && noRtpProxy.equals("yes"))
			{
				return;
			}
			Connection cn = sd.getConnection();
			logger.info("rtpproxy host:"+proxyHost+" ctrlHost:"+ctrlHost);
			cn.setAddress(proxyHost);

			// System.out.println("nettype:" + cn.getNetworkType());
			SipProxyMessageImpl requestImpl = (SipProxyMessageImpl)message;
			SIPMessage sipRequest = (SIPMessage)requestImpl.getMessage();
			FromHeader fromHeader = sipRequest.getFrom();
			String fromTag = fromHeader.getTag();
			ToHeader toHeader = sipRequest.getToHeader();
			String toTag = toHeader.getTag();
			String callId = sipRequest.getCallId().getCallId();
			Vector<MediaDescription> vc = sd.getMediaDescriptions(false);
			
			for (int i = 0; i < vc.size(); i++) {
				MediaDescription ms = vc.elementAt(i);
				Media m = ms.getMedia();
				
				RtpProxyRequest_UL rtpProxyRequest;
				rtpProxyRequest = new RtpProxyRequest_UL();
				rtpProxyRequest.setTokenPrefix(String.valueOf(Thread.currentThread().getId()));
				rtpProxyRequest.setCallId(callId);
				rtpProxyRequest.setRemoteIp(cn.getAddress());
				rtpProxyRequest.setRemotePort(m.getMediaPort());
				rtpProxyRequest.setFromTag(fromTag + ";" + (i+1));
				if (message instanceof SipProxyResponse)
				{
					rtpProxyRequest.setToTag(toTag);
				}
				
				if (toTag != null && toTag.length() > 0){
					
					rtpProxyRequest.setToTag(toTag+";" + i+1);
					
				}
				RtpProxyResponse resp = requestCommand(rtpProxyRequest);
				m.setMediaPort(Integer.parseInt(resp.getValue()));
				
				
			}
			if (message instanceof SipProxyRequest){
				AttributeField attr = new AttributeField();
				attr.setName("nortpproxy");
				attr.setValue("yes");
				sd.addField(attr);
			}
			message.setContent(sd.toString(), CONTENT_TYPE_SDP);
		} catch (Exception e) {
			logger.info(e.getMessage(),e);
		}
	}

	public RtpProxyResponse requestCommand(RtpProxyRequest request) {
		RtpProxyResponse response = null;
		try {

			DatagramSocket client = new DatagramSocket();
			InetAddress addr = InetAddress.getByName(ctrlHost);
			byte[] sendbuff = request.encode();
			DatagramPacket sendPacket = new DatagramPacket(sendbuff, sendbuff.length, addr, ctrlPort);
			client.send(sendPacket);
			byte[] recvBuf = new byte[100];
			DatagramPacket recvPacket = new DatagramPacket(recvBuf, recvBuf.length);
			client.receive(recvPacket);
			String recvStr = new String(recvPacket.getData(), 0, recvPacket.getLength());
			response = new RtpProxyResponse(request.getCommand(), recvStr);
			client.close();

		} catch (Exception e) {
			logger.info(e.getMessage(),e);
		}finally{
			
		}
		return response;
	}

	public static RtpProxyResponse requestCommand(String host, int port,String proxyHost, RtpProxyRequest request) {
		return new RtpProxyControlClient(host, port,proxyHost).requestCommand(request);
	}


}
