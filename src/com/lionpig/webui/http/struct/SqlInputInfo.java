package com.lionpig.webui.http.struct;

public class SqlInputInfo {
	private String inputId;
	private String inputName;
	private String inputType;
	private int inputSeq;
	private String inputOp;
	private String inputLogic;
	private String inputOptional;
	private String inputListSource;
	private String inputDefaultValue;
	private int inputCustomFlag;
	
	public SqlInputInfo(String inputId, String inputName, String inputType, int inputSeq, String inputOp,
			String inputLogic, String inputOptional, String inputListSource, String inputDefaultValue,
			int inputCustomFlag) {
		this.inputId = inputId;
		this.inputName = inputName;
		this.inputType = inputType;
		this.inputSeq = inputSeq;
		this.inputOp = inputOp;
		this.inputLogic = inputLogic;
		this.inputOptional = inputOptional;
		this.inputListSource = inputListSource;
		this.inputDefaultValue = inputDefaultValue;
		this.inputCustomFlag = inputCustomFlag;
	}
	
	public String getInputId() {
		return inputId;
	}
	public String getInputName() {
		return inputName;
	}
	public String getInputType() {
		return inputType;
	}
	public int getInputSeq() {
		return inputSeq;
	}
	public String getInputOp() {
		return inputOp;
	}
	public String getInputLogic() {
		return inputLogic;
	}
	public String getInputOptional() {
		return inputOptional;
	}
	public boolean isOptional() {
		if (inputOptional.equals("N"))
			return false;
		else
			return true;
	}
	public String getInputListSource() {
		return inputListSource;
	}
	public String getInputDefaultValue() {
		return inputDefaultValue;
	}
	public int getInputCustomFlag() {
		return inputCustomFlag;
	}
}
