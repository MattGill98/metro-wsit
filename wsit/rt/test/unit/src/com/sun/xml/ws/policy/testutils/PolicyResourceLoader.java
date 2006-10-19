/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.xml.ws.policy.testutils;

import com.sun.xml.stream.buffer.XMLStreamBuffer;
import com.sun.xml.stream.buffer.XMLStreamBufferException;
import com.sun.xml.ws.api.model.wsdl.WSDLModel;
import com.sun.xml.ws.api.wsdl.parser.WSDLParserExtension;
import com.sun.xml.ws.policy.DummyPolicySelector;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.jaxws.PolicyWSDLParserExtension;
import com.sun.xml.ws.policy.jaxws.WSDLPolicyMapWrapper;
import com.sun.xml.ws.policy.sourcemodel.PolicyModelTranslator;
import com.sun.xml.ws.policy.sourcemodel.PolicyModelUnmarshaller;
import com.sun.xml.ws.policy.sourcemodel.PolicySourceModel;
import com.sun.xml.ws.util.xml.XmlUtil;
import com.sun.xml.ws.wsdl.parser.RuntimeWSDLParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

/**
 * This class provides utility methods to load resources and unmarshall policy source model.
 *
 * @author Marek Potociar
 */
public final class PolicyResourceLoader {
    public static final String POLICY_UNIT_TEST_RESOURCE_ROOT = "policy/";
    private static final XMLInputFactory inputFactory = XMLInputFactory.newInstance();

    public static final String[] SINGLE_ALTERNATIVE_POLICY = new String[] {
        "single_alternative_policy/policy1.xml",
        "single_alternative_policy/policy2.xml",
        "single_alternative_policy/policy3.xml",
        "single_alternative_policy/policy4.xml",
        "single_alternative_policy/policy5.xml"
    };
    
    private PolicyResourceLoader() {
    }
    
    public static PolicySourceModel unmarshallModel(String resource) throws PolicyException, IOException {
        Reader resourceReader = getResourceReader(resource);
        PolicySourceModel model = PolicyModelUnmarshaller.getXmlUnmarshaller().unmarshalModel(resourceReader);
        resourceReader.close();
        return model;
    }
    
    public static PolicySourceModel unmarshallModel(Reader resourceReader) throws PolicyException, IOException {
        PolicySourceModel model = PolicyModelUnmarshaller.getXmlUnmarshaller().unmarshalModel(resourceReader);
        resourceReader.close();
        return model;
    }
    
    public static InputStream getResourceStream(String resourceName) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(POLICY_UNIT_TEST_RESOURCE_ROOT + resourceName);
    }
    
    public static Reader getResourceReader(String resourceName) {
        return new InputStreamReader(getResourceStream(resourceName));
    }
    
    public static XMLStreamBuffer getResourceXmlBuffer(String resourceName)
        throws XMLStreamException, XMLStreamBufferException {
        return XMLStreamBuffer.createNewBufferFromXMLStreamReader(inputFactory.createXMLStreamReader(getResourceStream(resourceName)));
    }
    
    public static URL getResourceUrl(String resourceName) {
        return Thread.currentThread().getContextClassLoader().getResource(POLICY_UNIT_TEST_RESOURCE_ROOT + resourceName);
    }
    
    public static Policy translateModel(PolicySourceModel model) throws PolicyException {
        return PolicyModelTranslator.getTranslator().translate(model);
    }
    
    public static Policy loadPolicy(String resourceName) throws PolicyException, IOException {
        return translateModel(unmarshallModel(resourceName));
    }

    // reads policy map from the given wsdl document
    public static PolicyMap getPolicyMap(String resourceName) throws Exception {
        return getPolicyMap(resourceName, true);
    }

    
    // reads policy map from given wsdl document
    public static PolicyMap getPolicyMap(String resourceName, boolean policySelectorResponse) throws Exception {
        WSDLModel model = getWSDLModel(resourceName, policySelectorResponse);
        WSDLPolicyMapWrapper wrapper = model.getExtension(WSDLPolicyMapWrapper.class);
        return wrapper.getPolicyMap();
    }
    
    // reads wsdl model from given wsdl document
    public static WSDLModel getWSDLModel(String resourceName) throws Exception {
        return getWSDLModel(resourceName, true);
    }

    
    // reads wsdl model from given wsdl document
    public static WSDLModel getWSDLModel(String resourceName, boolean policySelectorResponse) throws Exception {
        DummyPolicySelector.supported = policySelectorResponse;
        URL wsdlUrl = PolicyResourceLoader.getResourceUrl(resourceName);
        WSDLModel model = RuntimeWSDLParser.parse(wsdlUrl, XmlUtil.createDefaultCatalogResolver(), true, new WSDLParserExtension[] { new PolicyWSDLParserExtension() });
        return model;
    }
    
    
}
