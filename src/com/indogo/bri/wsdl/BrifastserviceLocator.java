/**
 * BrifastserviceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.indogo.bri.wsdl;

public class BrifastserviceLocator extends org.apache.axis.client.Service implements com.indogo.bri.wsdl.Brifastservice {

    public BrifastserviceLocator() {
    }


    public BrifastserviceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public BrifastserviceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for BrifastservicePort
    private java.lang.String BrifastservicePort_address = "http://trx.dev.brifast.co.id/Webservice/brifastService/";

    public java.lang.String getBrifastservicePortAddress() {
        return BrifastservicePort_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String BrifastservicePortWSDDServiceName = "BrifastservicePort";

    public java.lang.String getBrifastservicePortWSDDServiceName() {
        return BrifastservicePortWSDDServiceName;
    }

    public void setBrifastservicePortWSDDServiceName(java.lang.String name) {
        BrifastservicePortWSDDServiceName = name;
    }

    public com.indogo.bri.wsdl.BrifastservicePortType getBrifastservicePort() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(BrifastservicePort_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getBrifastservicePort(endpoint);
    }

    public com.indogo.bri.wsdl.BrifastservicePortType getBrifastservicePort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.indogo.bri.wsdl.BrifastserviceBindingStub _stub = new com.indogo.bri.wsdl.BrifastserviceBindingStub(portAddress, this);
            _stub.setPortName(getBrifastservicePortWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setBrifastservicePortEndpointAddress(java.lang.String address) {
        BrifastservicePort_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (com.indogo.bri.wsdl.BrifastservicePortType.class.isAssignableFrom(serviceEndpointInterface)) {
                com.indogo.bri.wsdl.BrifastserviceBindingStub _stub = new com.indogo.bri.wsdl.BrifastserviceBindingStub(new java.net.URL(BrifastservicePort_address), this);
                _stub.setPortName(getBrifastservicePortWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("BrifastservicePort".equals(inputPortName)) {
            return getBrifastservicePort();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("urn:Brifastservice", "Brifastservice");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("urn:Brifastservice", "BrifastservicePort"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("BrifastservicePort".equals(portName)) {
            setBrifastservicePortEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
