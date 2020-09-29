/**
 * BrifastserviceBindingStub.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.indogo.bri.wsdl;

public class BrifastserviceBindingStub extends org.apache.axis.client.Stub implements com.indogo.bri.wsdl.BrifastservicePortType {
    private java.util.Vector cachedSerClasses = new java.util.Vector();
    private java.util.Vector cachedSerQNames = new java.util.Vector();
    private java.util.Vector cachedSerFactories = new java.util.Vector();
    private java.util.Vector cachedDeserFactories = new java.util.Vector();

    static org.apache.axis.description.OperationDesc [] _operations;

    static {
        _operations = new org.apache.axis.description.OperationDesc[9];
        _initOperationDesc1();
    }

    private static void _initOperationDesc1(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("inquiryAccount");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "parameters"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:Brifastservice", "inquiryAccount_CT"), com.indogo.bri.wsdl.InquiryAccount_CT.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:Brifastservice", "inquiryAccount_CT_Result"));
        oper.setReturnClass(com.indogo.bri.wsdl.InquiryAccount_CT_Result.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[0] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("inquiryTransaction");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "parameters"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:Brifastservice", "inquiryTransaction_CT"), com.indogo.bri.wsdl.InquiryTransaction_CT.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:Brifastservice", "inquiryTransaction_CT_Result"));
        oper.setReturnClass(com.indogo.bri.wsdl.InquiryTransaction_CT_Result.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[1] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("requestToken");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "parameters"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:Brifastservice", "requestTokenCT"), com.indogo.bri.wsdl.RequestTokenCT.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:Brifastservice", "requestTokenCTResult"));
        oper.setReturnClass(com.indogo.bri.wsdl.RequestTokenCTResult.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[2] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("inquiryVostro");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "parameters"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:Brifastservice", "inquiryVostro_CT"), com.indogo.bri.wsdl.InquiryVostro_CT.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:Brifastservice", "inquiryVostro_CT_Result"));
        oper.setReturnClass(com.indogo.bri.wsdl.InquiryVostro_CT_Result.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "parameters"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[3] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("cancelTransaction");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "parameters"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:Brifastservice", "cancelTransaction_CT"), com.indogo.bri.wsdl.CancelTransaction_CT.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:Brifastservice", "cancelTransaction_CT_Result"));
        oper.setReturnClass(com.indogo.bri.wsdl.CancelTransaction_CT_Result.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[4] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("paymentAccount");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "parameters"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:Brifastservice", "paymentAccount_CT"), com.indogo.bri.wsdl.PaymentAccount_CT.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:Brifastservice", "paymentAccount_CT_Result"));
        oper.setReturnClass(com.indogo.bri.wsdl.PaymentAccount_CT_Result.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[5] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("paymentCash");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "parameters"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:Brifastservice", "paymentCash_CT"), com.indogo.bri.wsdl.PaymentCash_CT.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:Brifastservice", "paymentCash_CT_Result"));
        oper.setReturnClass(com.indogo.bri.wsdl.PaymentCash_CT_Result.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[6] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("paymentAccount_BRC");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "parameters"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:Brifastservice", "paymentAccount_CT_BRC"), com.indogo.bri.wsdl.PaymentAccount_CT_BRC.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:Brifastservice", "paymentAccount_CT_Result_BRC"));
        oper.setReturnClass(com.indogo.bri.wsdl.PaymentAccount_CT_Result_BRC.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[7] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("paymentCash_BRC");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "parameters"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:Brifastservice", "paymentCash_BRC_CT_BRC"), com.indogo.bri.wsdl.PaymentCash_BRC_CT_BRC.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:Brifastservice", "paymentCash_BRC_CT_Result_BRC"));
        oper.setReturnClass(com.indogo.bri.wsdl.PaymentCash_BRC_CT_Result_BRC.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[8] = oper;

    }

    public BrifastserviceBindingStub() throws org.apache.axis.AxisFault {
         this(null);
    }

    public BrifastserviceBindingStub(java.net.URL endpointURL, javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
         this(service);
         super.cachedEndpoint = endpointURL;
    }

    public BrifastserviceBindingStub(javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
        if (service == null) {
            super.service = new org.apache.axis.client.Service();
        } else {
            super.service = service;
        }
        ((org.apache.axis.client.Service)super.service).setTypeMappingVersion("1.2");
            java.lang.Class cls;
            javax.xml.namespace.QName qName;
            javax.xml.namespace.QName qName2;
            java.lang.Class beansf = org.apache.axis.encoding.ser.BeanSerializerFactory.class;
            java.lang.Class beandf = org.apache.axis.encoding.ser.BeanDeserializerFactory.class;
            java.lang.Class enumsf = org.apache.axis.encoding.ser.EnumSerializerFactory.class;
            java.lang.Class enumdf = org.apache.axis.encoding.ser.EnumDeserializerFactory.class;
            java.lang.Class arraysf = org.apache.axis.encoding.ser.ArraySerializerFactory.class;
            java.lang.Class arraydf = org.apache.axis.encoding.ser.ArrayDeserializerFactory.class;
            java.lang.Class simplesf = org.apache.axis.encoding.ser.SimpleSerializerFactory.class;
            java.lang.Class simpledf = org.apache.axis.encoding.ser.SimpleDeserializerFactory.class;
            java.lang.Class simplelistsf = org.apache.axis.encoding.ser.SimpleListSerializerFactory.class;
            java.lang.Class simplelistdf = org.apache.axis.encoding.ser.SimpleListDeserializerFactory.class;
            qName = new javax.xml.namespace.QName("urn:Brifastservice", "cancelTransaction_CT");
            cachedSerQNames.add(qName);
            cls = com.indogo.bri.wsdl.CancelTransaction_CT.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:Brifastservice", "cancelTransaction_CT_Result");
            cachedSerQNames.add(qName);
            cls = com.indogo.bri.wsdl.CancelTransaction_CT_Result.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:Brifastservice", "inquiryAccount_CT");
            cachedSerQNames.add(qName);
            cls = com.indogo.bri.wsdl.InquiryAccount_CT.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:Brifastservice", "inquiryAccount_CT_Result");
            cachedSerQNames.add(qName);
            cls = com.indogo.bri.wsdl.InquiryAccount_CT_Result.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:Brifastservice", "inquiryTransaction_CT");
            cachedSerQNames.add(qName);
            cls = com.indogo.bri.wsdl.InquiryTransaction_CT.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:Brifastservice", "inquiryTransaction_CT_Result");
            cachedSerQNames.add(qName);
            cls = com.indogo.bri.wsdl.InquiryTransaction_CT_Result.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:Brifastservice", "inquiryVostro_CT");
            cachedSerQNames.add(qName);
            cls = com.indogo.bri.wsdl.InquiryVostro_CT.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:Brifastservice", "inquiryVostro_CT_Result");
            cachedSerQNames.add(qName);
            cls = com.indogo.bri.wsdl.InquiryVostro_CT_Result.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:Brifastservice", "paymentAccount_CT");
            cachedSerQNames.add(qName);
            cls = com.indogo.bri.wsdl.PaymentAccount_CT.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:Brifastservice", "paymentAccount_CT_BRC");
            cachedSerQNames.add(qName);
            cls = com.indogo.bri.wsdl.PaymentAccount_CT_BRC.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:Brifastservice", "paymentAccount_CT_Result");
            cachedSerQNames.add(qName);
            cls = com.indogo.bri.wsdl.PaymentAccount_CT_Result.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:Brifastservice", "paymentAccount_CT_Result_BRC");
            cachedSerQNames.add(qName);
            cls = com.indogo.bri.wsdl.PaymentAccount_CT_Result_BRC.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:Brifastservice", "paymentCash_BRC_CT_BRC");
            cachedSerQNames.add(qName);
            cls = com.indogo.bri.wsdl.PaymentCash_BRC_CT_BRC.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:Brifastservice", "paymentCash_BRC_CT_Result_BRC");
            cachedSerQNames.add(qName);
            cls = com.indogo.bri.wsdl.PaymentCash_BRC_CT_Result_BRC.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:Brifastservice", "paymentCash_CT");
            cachedSerQNames.add(qName);
            cls = com.indogo.bri.wsdl.PaymentCash_CT.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:Brifastservice", "paymentCash_CT_Result");
            cachedSerQNames.add(qName);
            cls = com.indogo.bri.wsdl.PaymentCash_CT_Result.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:Brifastservice", "requestTokenCT");
            cachedSerQNames.add(qName);
            cls = com.indogo.bri.wsdl.RequestTokenCT.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:Brifastservice", "requestTokenCTResult");
            cachedSerQNames.add(qName);
            cls = com.indogo.bri.wsdl.RequestTokenCTResult.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

    }

    protected org.apache.axis.client.Call createCall() throws java.rmi.RemoteException {
        try {
            org.apache.axis.client.Call _call = super._createCall();
            if (super.maintainSessionSet) {
                _call.setMaintainSession(super.maintainSession);
            }
            if (super.cachedUsername != null) {
                _call.setUsername(super.cachedUsername);
            }
            if (super.cachedPassword != null) {
                _call.setPassword(super.cachedPassword);
            }
            if (super.cachedEndpoint != null) {
                _call.setTargetEndpointAddress(super.cachedEndpoint);
            }
            if (super.cachedTimeout != null) {
                _call.setTimeout(super.cachedTimeout);
            }
            if (super.cachedPortName != null) {
                _call.setPortName(super.cachedPortName);
            }
            java.util.Enumeration keys = super.cachedProperties.keys();
            while (keys.hasMoreElements()) {
                java.lang.String key = (java.lang.String) keys.nextElement();
                _call.setProperty(key, super.cachedProperties.get(key));
            }
            // All the type mapping information is registered
            // when the first call is made.
            // The type mapping information is actually registered in
            // the TypeMappingRegistry of the service, which
            // is the reason why registration is only needed for the first call.
            synchronized (this) {
                if (firstCall()) {
                    // must set encoding style before registering serializers
                    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
                    _call.setEncodingStyle(org.apache.axis.Constants.URI_SOAP11_ENC);
                    for (int i = 0; i < cachedSerFactories.size(); ++i) {
                        java.lang.Class cls = (java.lang.Class) cachedSerClasses.get(i);
                        javax.xml.namespace.QName qName =
                                (javax.xml.namespace.QName) cachedSerQNames.get(i);
                        java.lang.Object x = cachedSerFactories.get(i);
                        if (x instanceof Class) {
                            java.lang.Class sf = (java.lang.Class)
                                 cachedSerFactories.get(i);
                            java.lang.Class df = (java.lang.Class)
                                 cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                        }
                        else if (x instanceof javax.xml.rpc.encoding.SerializerFactory) {
                            org.apache.axis.encoding.SerializerFactory sf = (org.apache.axis.encoding.SerializerFactory)
                                 cachedSerFactories.get(i);
                            org.apache.axis.encoding.DeserializerFactory df = (org.apache.axis.encoding.DeserializerFactory)
                                 cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                        }
                    }
                }
            }
            return _call;
        }
        catch (java.lang.Throwable _t) {
            throw new org.apache.axis.AxisFault("Failure trying to get the Call object", _t);
        }
    }

    public com.indogo.bri.wsdl.InquiryAccount_CT_Result inquiryAccount(com.indogo.bri.wsdl.InquiryAccount_CT parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[0]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:https://trx.dev.brifast.co.id/Webservice/brifastService/inquiryAccount");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:SOAPServerWSDL", "inquiryAccount"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.indogo.bri.wsdl.InquiryAccount_CT_Result) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.indogo.bri.wsdl.InquiryAccount_CT_Result) org.apache.axis.utils.JavaUtils.convert(_resp, com.indogo.bri.wsdl.InquiryAccount_CT_Result.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public com.indogo.bri.wsdl.InquiryTransaction_CT_Result inquiryTransaction(com.indogo.bri.wsdl.InquiryTransaction_CT parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[1]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:https://trx.dev.brifast.co.id/Webservice/brifastService/inquiryTransaction");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:SOAPServerWSDL", "inquiryTransaction"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.indogo.bri.wsdl.InquiryTransaction_CT_Result) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.indogo.bri.wsdl.InquiryTransaction_CT_Result) org.apache.axis.utils.JavaUtils.convert(_resp, com.indogo.bri.wsdl.InquiryTransaction_CT_Result.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public com.indogo.bri.wsdl.RequestTokenCTResult requestToken(com.indogo.bri.wsdl.RequestTokenCT parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[2]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:https://trx.dev.brifast.co.id/Webservice/brifastService/requestToken");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:SOAPServerWSDL", "requestToken"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.indogo.bri.wsdl.RequestTokenCTResult) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.indogo.bri.wsdl.RequestTokenCTResult) org.apache.axis.utils.JavaUtils.convert(_resp, com.indogo.bri.wsdl.RequestTokenCTResult.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public com.indogo.bri.wsdl.InquiryVostro_CT_Result inquiryVostro(com.indogo.bri.wsdl.InquiryVostro_CT parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[3]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:https://trx.dev.brifast.co.id/Webservice/brifastService/inquiryVostro");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:SOAPServerWSDL", "inquiryVostro"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.indogo.bri.wsdl.InquiryVostro_CT_Result) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.indogo.bri.wsdl.InquiryVostro_CT_Result) org.apache.axis.utils.JavaUtils.convert(_resp, com.indogo.bri.wsdl.InquiryVostro_CT_Result.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public com.indogo.bri.wsdl.CancelTransaction_CT_Result cancelTransaction(com.indogo.bri.wsdl.CancelTransaction_CT parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[4]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:https://trx.dev.brifast.co.id/Webservice/brifastService/cancelTransaction");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:SOAPServerWSDL", "cancelTransaction"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.indogo.bri.wsdl.CancelTransaction_CT_Result) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.indogo.bri.wsdl.CancelTransaction_CT_Result) org.apache.axis.utils.JavaUtils.convert(_resp, com.indogo.bri.wsdl.CancelTransaction_CT_Result.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public com.indogo.bri.wsdl.PaymentAccount_CT_Result paymentAccount(com.indogo.bri.wsdl.PaymentAccount_CT parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[5]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:https://trx.dev.brifast.co.id/Webservice/brifastService/paymentAccount");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:SOAPServerWSDL", "paymentAccount"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.indogo.bri.wsdl.PaymentAccount_CT_Result) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.indogo.bri.wsdl.PaymentAccount_CT_Result) org.apache.axis.utils.JavaUtils.convert(_resp, com.indogo.bri.wsdl.PaymentAccount_CT_Result.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public com.indogo.bri.wsdl.PaymentCash_CT_Result paymentCash(com.indogo.bri.wsdl.PaymentCash_CT parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[6]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:https://trx.dev.brifast.co.id/Webservice/brifastService/paymentCash");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:SOAPServerWSDL", "paymentCash"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.indogo.bri.wsdl.PaymentCash_CT_Result) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.indogo.bri.wsdl.PaymentCash_CT_Result) org.apache.axis.utils.JavaUtils.convert(_resp, com.indogo.bri.wsdl.PaymentCash_CT_Result.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public com.indogo.bri.wsdl.PaymentAccount_CT_Result_BRC paymentAccount_BRC(com.indogo.bri.wsdl.PaymentAccount_CT_BRC parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[7]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:https://trx.dev.brifast.co.id/Webservice/brifastService/paymentAccount_BRC");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:SOAPServerWSDL", "paymentAccount_BRC"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.indogo.bri.wsdl.PaymentAccount_CT_Result_BRC) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.indogo.bri.wsdl.PaymentAccount_CT_Result_BRC) org.apache.axis.utils.JavaUtils.convert(_resp, com.indogo.bri.wsdl.PaymentAccount_CT_Result_BRC.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public com.indogo.bri.wsdl.PaymentCash_BRC_CT_Result_BRC paymentCash_BRC(com.indogo.bri.wsdl.PaymentCash_BRC_CT_BRC parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[8]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:https://trx.dev.brifast.co.id/Webservice/brifastService/paymentCash_BRC");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:SOAPServerWSDL", "paymentCash_BRC"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.indogo.bri.wsdl.PaymentCash_BRC_CT_Result_BRC) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.indogo.bri.wsdl.PaymentCash_BRC_CT_Result_BRC) org.apache.axis.utils.JavaUtils.convert(_resp, com.indogo.bri.wsdl.PaymentCash_BRC_CT_Result_BRC.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

}
