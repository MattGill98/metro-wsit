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

package com.sun.xml.ws.security.impl.policyconv;

import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.NestedPolicy;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import com.sun.xml.ws.policy.sourcemodel.ModelNode;
import com.sun.xml.ws.security.impl.policy.PolicyUtil;
import com.sun.xml.ws.security.policy.AsymmetricBinding;
import com.sun.xml.ws.security.policy.Issuer;
import com.sun.xml.ws.security.policy.SecureConversationToken;
import com.sun.xml.ws.security.policy.SupportingTokens;
import com.sun.xml.ws.security.policy.SymmetricBinding;
import com.sun.xml.ws.security.policy.Token;
import com.sun.xml.wss.impl.policy.mls.MessagePolicy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class SCTokenWrapper extends PolicyAssertion implements SecureConversationToken{
    
    private SecureConversationToken scToken = null;
    private MessagePolicy messagePolicy = null;
    private List<PolicyAssertion> tokenList = null;
    private boolean cached = false;
    /** Creates a new instance of SCTokenWrapper */
    public SCTokenWrapper(PolicyAssertion scToken,MessagePolicy mp) {
        super(new AssertionData(scToken.getName(),scToken.getValue(),scToken.getAttributes(), ModelNode.Type.ASSERTION),getNestedAssertions(scToken),
                (scToken.getNestedPolicy()== null ? null : scToken.getNestedPolicy().getAssertionSet()));
        this.scToken = (SecureConversationToken)scToken;
        this.messagePolicy = mp;
    }
    
    private static Collection<PolicyAssertion> getNestedAssertions(PolicyAssertion scToken){
        if(scToken.hasNestedAssertions()){
            Iterator<PolicyAssertion> itr = scToken.getNestedAssertionsIterator();
            if(itr.hasNext()){// will have only one assertion set. TODO:Cross check with marek.
                return Collections.singletonList(itr.next());
            }
        }
        return null;
        
    }
    
    public SecureConversationToken getSecureConversationToken() {
        return scToken;
    }
    
    public void setSecureConversationToken(SecureConversationToken scToken) {
        this.scToken = scToken;
    }
    
    public MessagePolicy getMessagePolicy() {
        return messagePolicy;
    }
    
    public void setMessagePolicyp(MessagePolicy mp) {
        this.messagePolicy = mp;
    }
    
    
    public boolean isRequireDerivedKeys() {
        return this.scToken.isRequireDerivedKeys();
    }
    
    
    public String getTokenType() {
        return this.scToken.getTokenType();
    }
    
    public Issuer getIssuer() {
        return this.scToken.getIssuer();
    }
    
    
    public NestedPolicy getBootstrapPolicy() {
        return this.scToken.getBootstrapPolicy();
    }
    
    
    public String getIncludeToken() {
        return this.scToken.getIncludeToken();
    }
    
    public String getTokenId() {
        return this.scToken.getTokenId();
    }
    
    
    public List<PolicyAssertion> getIssuedTokens(){
        if(!cached){
            if(this.hasNestedPolicy()){
                tokenList = getTokens(this.getNestedPolicy());
                cached = true;
            }
        }
        return tokenList;
    }
    
    private ArrayList<PolicyAssertion> getTokens(NestedPolicy policy){
        ArrayList<PolicyAssertion> tokenList = new ArrayList<PolicyAssertion>();
        AssertionSet assertionSet = policy.getAssertionSet();
        for(PolicyAssertion pa:assertionSet){
            if(PolicyUtil.isBootstrapPolicy(pa)){
                NestedPolicy np = pa.getNestedPolicy();
                AssertionSet bpSet = np.getAssertionSet();
                for(PolicyAssertion assertion:bpSet){
                    if(PolicyUtil.isAsymmetricBinding(assertion)){
                        AsymmetricBinding sb =  (AsymmetricBinding)assertion;
                        addToken(sb.getInitiatorToken(),tokenList);
                        addToken(sb.getRecipientToken(),tokenList);
                    }else if(PolicyUtil.isSymmetricBinding(assertion)){
                        SymmetricBinding sb = (SymmetricBinding)assertion;
                        Token token = sb.getProtectionToken();
                        if(token != null){
                            addToken(token,tokenList);
                        }else{
                            addToken(sb.getEncryptionToken(),tokenList);
                            addToken(sb.getSignatureToken(),tokenList);
                        }
                    }else if(PolicyUtil.isSupportingTokens(assertion)){
                        SupportingTokens st = (SupportingTokens)assertion;
                        Iterator itr = st.getTokens();
                        while(itr.hasNext()){
                            addToken((Token)itr.next(),tokenList);
                        }
                    }
                }
            }
            
        }
        return tokenList;
    }
    
    private void addToken(Token token,ArrayList list){
        if(PolicyUtil.isIssuedToken((PolicyAssertion)token)){
            list.add(token);
        }
    }
    
    public Set getTokenRefernceTypes() {
        return this.scToken.getTokenRefernceTypes();
    }
    
    public void addBootstrapPolicy(NestedPolicy policy) {
    }
}
