/**
 * SrmGetSpaceTokensRequest.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.3 Oct 05, 2005 (05:23:37 EDT) WSDL2Java emitter.
 */

package org.dcache.srm.v2_2;

public class SrmGetSpaceTokensRequest  implements java.io.Serializable {
    private static final long serialVersionUID = 7673338975905019983L;
    private java.lang.String userSpaceTokenDescription;

    private java.lang.String authorizationID;

    public SrmGetSpaceTokensRequest() {
    }

    public SrmGetSpaceTokensRequest(
           java.lang.String userSpaceTokenDescription,
           java.lang.String authorizationID) {
           this.userSpaceTokenDescription = userSpaceTokenDescription;
           this.authorizationID = authorizationID;
    }


    /**
     * Gets the userSpaceTokenDescription value for this SrmGetSpaceTokensRequest.
     * 
     * @return userSpaceTokenDescription
     */
    public java.lang.String getUserSpaceTokenDescription() {
        return userSpaceTokenDescription;
    }


    /**
     * Sets the userSpaceTokenDescription value for this SrmGetSpaceTokensRequest.
     * 
     * @param userSpaceTokenDescription
     */
    public void setUserSpaceTokenDescription(java.lang.String userSpaceTokenDescription) {
        this.userSpaceTokenDescription = userSpaceTokenDescription;
    }


    /**
     * Gets the authorizationID value for this SrmGetSpaceTokensRequest.
     * 
     * @return authorizationID
     */
    public java.lang.String getAuthorizationID() {
        return authorizationID;
    }


    /**
     * Sets the authorizationID value for this SrmGetSpaceTokensRequest.
     * 
     * @param authorizationID
     */
    public void setAuthorizationID(java.lang.String authorizationID) {
        this.authorizationID = authorizationID;
    }

    private java.lang.Object __equalsCalc;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SrmGetSpaceTokensRequest)) {
            return false;
        }
        SrmGetSpaceTokensRequest other = (SrmGetSpaceTokensRequest) obj;
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.userSpaceTokenDescription==null && other.getUserSpaceTokenDescription()==null) || 
             (this.userSpaceTokenDescription!=null &&
              this.userSpaceTokenDescription.equals(other.getUserSpaceTokenDescription()))) &&
            ((this.authorizationID==null && other.getAuthorizationID()==null) || 
             (this.authorizationID!=null &&
              this.authorizationID.equals(other.getAuthorizationID())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getUserSpaceTokenDescription() != null) {
            _hashCode += getUserSpaceTokenDescription().hashCode();
        }
        if (getAuthorizationID() != null) {
            _hashCode += getAuthorizationID().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SrmGetSpaceTokensRequest.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://srm.lbl.gov/StorageResourceManager", "srmGetSpaceTokensRequest"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("userSpaceTokenDescription");
        elemField.setXmlName(new javax.xml.namespace.QName("", "userSpaceTokenDescription"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("authorizationID");
        elemField.setXmlName(new javax.xml.namespace.QName("", "authorizationID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
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
