package org.freeims.ims.rtpproxy;

public class RtpProxyRequest_UL extends RtpProxyRequest {

	// U[opts] call_id remote_ip remote_port from_tag [to_tag] [notify_socket
	// notify_tag]

	private String callId;
	private String remoteIp;
	private int remotePort;
	private String fromTag;
	private String toTag;
	private String notifySocket;
	private String notifyTag;

	private char[] opts;

	public RtpProxyRequest_UL() {
		command = Command.U;
	}

	@Override
	public byte[] encode() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(createToken());
		buffer.append(" ");
		if (toTag != null && toTag.length() > 0) {
			command = Command.L;
		}
		buffer.append(command);

		if (opts != null) {
			for (char c : opts) {
				buffer.append(c);
			}
		}
		buffer.append(" ");
		buffer.append(callId);

		buffer.append(" ");
		buffer.append(remoteIp);

		buffer.append(" ");
		buffer.append(remotePort);

		buffer.append(" ");
		buffer.append(fromTag);

		if (toTag != null && toTag.length() > 0) {
			buffer.append(" ");
			buffer.append(toTag);
		}

		// no implement for a while

		// buffer.append(" ");
		// buffer.append(notifySocket);
		//
		// buffer.append(" ");
		// buffer.append(notifyTag);

		return buffer.toString().getBytes();
	}

	public String getCallId() {
		return callId;
	}

	public void setCallId(String callId) {
		this.callId = callId;
	}

	public String getRemoteIp() {
		return remoteIp;
	}

	public void setRemoteIp(String remoteIp) {
		this.remoteIp = remoteIp;
	}

	public int getRemotePort() {
		return remotePort;
	}

	public void setRemotePort(int remotePort) {
		this.remotePort = remotePort;
	}

	public String getFromTag() {
		return fromTag;
	}

	public void setFromTag(String fromTag) {
		this.fromTag = fromTag;
	}

	public String getToTag() {
		return toTag;
	}

	public void setToTag(String toTag) {
		this.toTag = toTag;
	}

	public String getNotifySocket() {
		return notifySocket;
	}

	public void setNotifySocket(String notifySocket) {
		this.notifySocket = notifySocket;
	}

	public String getNotifyTag() {
		return notifyTag;
	}

	public void setNotifyTag(String notifyTag) {
		this.notifyTag = notifyTag;
	}

	public char[] getOpts() {
		return opts;
	}

	public void setOpts(char[] opts) {
		this.opts = opts;
	}
}
