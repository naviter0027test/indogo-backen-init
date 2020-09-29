package com.lionpig.webui.http.struct;

public class SqlOutputInfo {
	private String outputId;
	private String outputName;
	private int outputSeq;
	private String outputType;
	
	public SqlOutputInfo(String outputId, String outputName, int outputSeq, String outputType) {
		this.outputId = outputId;
		this.outputName = outputName;
		this.outputSeq = outputSeq;
		this.outputType = outputType;
	}
	
	public String getOutputId() {
		return outputId;
	}
	public String getOutputName() {
		return outputName;
	}
	public int getOutputSeq() {
		return outputSeq;
	}
	public String getOutputType() {
		return outputType;
	}
	
	// variables used for caching
	private int parameterIndex;
	public void setParameterIndex(int i) {
		this.parameterIndex = i;
	}
	public int getParameterIndex() {
		return this.parameterIndex;
	}
	
	private String tempColumnName;
	public void setTempColumnName(String s) {
		this.tempColumnName = s;
	}
	public String getTempColumnName() {
		return this.tempColumnName;
	}
}
