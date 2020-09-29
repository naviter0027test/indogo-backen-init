/**
 * BrifastservicePortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.indogo.bri.wsdl;

public interface BrifastservicePortType extends java.rmi.Remote {

    /**
     * Inquiry Account Number
     */
    public com.indogo.bri.wsdl.InquiryAccount_CT_Result inquiryAccount(com.indogo.bri.wsdl.InquiryAccount_CT parameters) throws java.rmi.RemoteException;

    /**
     * Inquiry Transaction
     */
    public com.indogo.bri.wsdl.InquiryTransaction_CT_Result inquiryTransaction(com.indogo.bri.wsdl.InquiryTransaction_CT parameters) throws java.rmi.RemoteException;

    /**
     * Request Token
     */
    public com.indogo.bri.wsdl.RequestTokenCTResult requestToken(com.indogo.bri.wsdl.RequestTokenCT parameters) throws java.rmi.RemoteException;

    /**
     * Inquiry Vostro Account
     */
    public com.indogo.bri.wsdl.InquiryVostro_CT_Result inquiryVostro(com.indogo.bri.wsdl.InquiryVostro_CT parameters) throws java.rmi.RemoteException;

    /**
     * Cancel Transaction
     */
    public com.indogo.bri.wsdl.CancelTransaction_CT_Result cancelTransaction(com.indogo.bri.wsdl.CancelTransaction_CT parameters) throws java.rmi.RemoteException;

    /**
     * Do Payment Using Account Number
     */
    public com.indogo.bri.wsdl.PaymentAccount_CT_Result paymentAccount(com.indogo.bri.wsdl.PaymentAccount_CT parameters) throws java.rmi.RemoteException;

    /**
     * Do Payment Using Account Number
     */
    public com.indogo.bri.wsdl.PaymentCash_CT_Result paymentCash(com.indogo.bri.wsdl.PaymentCash_CT parameters) throws java.rmi.RemoteException;

    /**
     * Do Payment Using Account Number
     */
    public com.indogo.bri.wsdl.PaymentAccount_CT_Result_BRC paymentAccount_BRC(com.indogo.bri.wsdl.PaymentAccount_CT_BRC parameters) throws java.rmi.RemoteException;

    /**
     * Do Payment Using Account Number
     */
    public com.indogo.bri.wsdl.PaymentCash_BRC_CT_Result_BRC paymentCash_BRC(com.indogo.bri.wsdl.PaymentCash_BRC_CT_BRC parameters) throws java.rmi.RemoteException;
}
