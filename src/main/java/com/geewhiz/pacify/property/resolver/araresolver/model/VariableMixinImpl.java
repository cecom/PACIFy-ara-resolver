package com.geewhiz.pacify.property.resolver.araresolver.model;

import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;

import com.geewhiz.pacify.property.resolver.araresolver.model.AraData.Variable;
import com.uc4.ara.feature.utils.Maxim;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

public class VariableMixinImpl extends AbstractMixin implements VariableMixin {

    private Variable me;
    private Boolean  decodePasswordWithBase64;
    private String   encoding;
    private String   keyValueStore;

    public VariableMixinImpl(String propertyKeyValueSeparator, String encoding, Boolean decodePasswordWithBase64) {
        super(propertyKeyValueSeparator);
        this.decodePasswordWithBase64 = decodePasswordWithBase64;
        this.encoding = encoding;
    }

    public String getName() {
        int separatorIdx = getKeyValueStoreAndDecryptIfNecessary().indexOf(getPropertyKeyValueSeparator());

        return getKeyValueStoreAndDecryptIfNecessary().substring(0, separatorIdx);
    }

    public String getValue() {
        int separatorIdx = getKeyValueStoreAndDecryptIfNecessary().indexOf(getPropertyKeyValueSeparator());

        return getKeyValueStoreAndDecryptIfNecessary().substring(separatorIdx + getPropertyKeyValueSeparator().length());
    };

    public String getKeyValueStoreAndDecryptIfNecessary() {
        if (keyValueStore == null) {
            keyValueStore = me.getInternalValue();
            if (me.isEncrypted()) {
                keyValueStore = Maxim.deMaxim(me.getInternalValue());
                if (decodePasswordWithBase64) {
                    try {
                        keyValueStore = new String(Base64.decodeBase64(keyValueStore), encoding);
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException("Error while decoding passwort", e);
                    }
                }
            }
        }

        return keyValueStore;
    }
}
