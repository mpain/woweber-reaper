package org.mpain.reaper.http;

public class HttpExchangeData {
	private String request;
	private String response;
	private Integer code;

	public String getRequest() {
		return request;
	}

	public void setRequest(String request) {
		this.request = request;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("HttpExchangeData [\n\trequest=");
		builder.append(request);
		builder.append(", \n\tresponse=");
		builder.append(response);
		builder.append(", \n\tcode=");
		builder.append(code);
		builder.append("\n]");
		return builder.toString();
	}
}
