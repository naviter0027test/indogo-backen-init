/**
 * PaymentCash_BRC_CT_Result_BRC.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.indogo.bri.wsdl;

public class PaymentCash_BRC_CT_Result_BRC  implements java.io.Serializable {
    private java.lang.String message;

    private java.lang.String statusCode;

    private java.lang.String ticketNumber;

    private java.lang.String chargeAmount;

    public PaymentCash_BRC_CT_Result_BRC() {
    }

    public PaymentCash_BRC_CT_Result_BRC(
           java.lang.String message,
           java.lang.String statusCode,
           java.lang.String ticketNumber,
           java.lang.String chargeAmount) {
           this.message = message;
           this.statusCode = statusCode;
           this.ticketNumber = ticketNumber;
           this.chargeAmount = chargeAmount;
    }


    /**
     * Gets the message value for this PaymentCash_BRC_CT_Result_BRC.
     * 
     * @return message
     */
    public java.lang.String getMessage() {
        return message;
    }


    /**
     * Sets the message value for this PaymentCash_BRC_CT_Result_BRC.
     * 
     * @param message
     */
    public void setMessage(java.lang.String message) {
        this.message = message;
    }


    /**
     * Gets the statusCode value for this PaymentCash_BRC_CT_Result_BRC.
     * 
     * @return statusCode
     */
    public java.lang.String getStatusCode() {
        return statusCode;
    }


    /**
     * Sets the statusCode value for this PaymentCash_BRC_CT_Result_BRC.
     * 
     * @param statusCode
     */
    public void setStatusCode(java.lang.String statusCode) {
        this.statusCode = statusCode;
    }


    /**
     * Gets the ticketNumber value for this PaymentCash_BRC_CT_Result_BRC.
     * 
     * @return ticketNumber
     */
    public java.lang.String getTicketNumber() {
        return ticketNumber;
    }


    /**
     * Sets the ticketNumber value for this PaymentCash_BRC_CT_Result_BRC.
     * 
     * @param ticketNumber
     */
    public void setTicketNumber(java.lang.String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }


    /**
     * Gets the chargeAmount value for this PaymentCash_BRC_CT_Result_BRC.
     * 
     * @return chargeAmount
     */
    public java.lang.String getChargeAmount() {
        return chargeAmount;
    }


    /**
     * Sets the chargeAmount value for this PaymentCash_BRC_CT_Result_BRC.
     * 
     * @param chargeAmount
     */
    public void setChargeAmount(java.lang.String chargeAmount) {
        this.chargeAmount = chargeAmount;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof PaymentCash_BRC_CT_Result_BRC)) return false;
        PaymentCash_BRC_CT_Result_BRC other = (PaymentCash_BRC_CT_Result_BRC) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.message==null && other.getMessage()==null) || 
             (this.message!=null &&
              this.message.equals(other.getMessage()))) &&
            ((this.statusCode==null && other.getStatusCode()==null) || 
             (this.statusCode!=null &&
              this.statusCode.equals(other.getStatusCode()))) &&
            ((this.ticketNumber==null && other.getTicketNumber()==null) || 
             (this.ticketNumber!=null &&
              this.ticketNumber.equals(other.getTicketNumber()))) &&
            ((this.chargeAmount==null && other.getChargeAmount()==null) || 
             (this.chargeAmount!=null &&
              this.chargeAmount.equals(other.getChargeAmount())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getMessage() != null) {
            _hashCode += getMessage().hashCode();
        }
        if (getStatusCode() != null) {
            _hashCode += getStatusCode().hashCode();
        }
        if (getTicketNumber() != null) {
            _hashCode += getTicketNumber().hashCode();
        }
        if (getChargeAmount() != null) {
            _hashCode += getChargeAmount().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(PaymentCash_BRC_CT_Result_BRC.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:Brifastservice", "paymentCash_BRC_CT_Result_BRC"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("message");
        elemField.setXmlName(new javax.xml.namespace.QName("", "message"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("statusCode");
        elemField.setXmlName(new javax.xml.namespace.QName("", "statusCode"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("ticketNumber");
        elemField.setXmlName(new javax.xml.namespace.QName("", "ticketNumber"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("chargeAmount");
        elemField.setXmlName(new javax.xml.namespace.QName("", "chargeAmount"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
