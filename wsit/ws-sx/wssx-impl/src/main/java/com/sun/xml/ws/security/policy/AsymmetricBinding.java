/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.xml.ws.security.policy;

/**
 * Represents Asymmetric Token information to be used for Signature and Encryption
 * by the client and the service. If the message pattern requires multiple messages,
 * this binding defines that the Initiator Token is used for the message signature
 * from initiator to the recipient, and for encryption from recipient to initiator.
 * The Recipient Token is used for encryption from initiator to recipient, and for
 * the message signature from recipient to initiator. This interface represents normalized
 * AsymmetricBinding security policy assertion as shown below.
 *
 * <pre>
 *  &lt;xmp&gt;
 *      &lt;sp:AsymmetricBinding ... &gt;
 *              &lt;wsp:Policy&gt;
 *                  &lt;sp:InitiatorToken&gt;
 *                      &lt;wsp:Policy&gt; ... &lt;/wsp:Policy&gt;
 *                  &lt;/sp:InitiatorToken&gt;
 *                  &lt;sp:RecipientToken&gt;
 *                      &lt;wsp:Policy&gt; ... &lt;/wsp:Policy&gt;
 *                  &lt;/sp:RecipientToken&gt;
 *                  &lt;sp:AlgorithmSuite ... &gt;
 *                      ...
 *                  &lt;/sp:AlgorithmSuite&gt;
 *                  &lt;sp:Layout ... &gt; ... &lt;/sp:Layout&gt; ?
 *                  &lt;sp:IncludeTimestamp ... /&gt; ?
 *                  &lt;sp:EncryptBeforeSigning ... /&gt; ?
 *                  &lt;sp:EncryptSignature ... /&gt; ?
 *                  &lt;sp:ProtectTokens ... /&gt; ?
 *                  &lt;sp:OnlySignEntireHeadersAndBody ... /&gt; ?
 *                      ...
 *             &lt;/wsp:Policy&gt;
 *          ...
 *      &lt;/sp:AsymmetricBinding&gt;
 *
 *  &lt;/xmp&gt;
 * </pre>
 *
 * @author K.Venugopal@sun.com
 */
public interface AsymmetricBinding extends Binding{
   
    /**
     * returns Recipient token
     * @return {@link X509Token}
     */
    public Token getRecipientToken();
   
    /**
     * returns Recipient token
     * @return {@link X509Token}
     */
    public Token getRecipientSignatureToken();

     /**
     * returns Recipient token
     * @return {@link X509Token}
     */
    public Token getRecipientEncryptionToken();

    /**
     * returns Initiator token
     * @return {@link X509Token}
     */
    public Token getInitiatorToken();

    /**
     * returns Initiator token
     * @return {@link X509Token}
     */
    public Token getInitiatorSignatureToken();

    /**
     * returns Initiator token
     * @return {@link X509Token}
     */
    public Token getInitiatorEncryptionToken();
}
