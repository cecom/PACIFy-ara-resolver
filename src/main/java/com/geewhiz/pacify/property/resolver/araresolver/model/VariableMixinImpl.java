package com.geewhiz.pacify.property.resolver.araresolver.model;

import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    private Logger   logger = LogManager.getLogger(VariableMixinImpl.class.getName());

    private Variable me;
    private Boolean  decodePasswordWithBase64;
    private String   encoding;

    public VariableMixinImpl(String propertyKeyValueSeparator, String encoding, Boolean decodePasswordWithBase64) {
        super(propertyKeyValueSeparator);
        this.decodePasswordWithBase64 = decodePasswordWithBase64;
        this.encoding = encoding;
    }

    public String getName() {
        int separatorIdx = getKeyValueStoreAndDecryptIfNecessary().indexOf(getPropertyKeyValueSeparator());

        if (separatorIdx == -1) {
            logger.debug("The property format of [{}] is wrong. The key/value separator [{}] is missing.", me.getInternalName(), getPropertyKeyValueSeparator());
            return null;
        }

        return getKeyValueStoreAndDecryptIfNecessary().substring(0, separatorIdx);
    }

    public String getValue() {
        int separatorIdx = getKeyValueStoreAndDecryptIfNecessary().indexOf(getPropertyKeyValueSeparator());

        if (separatorIdx == -1) {
            logger.debug("The property format of [{}] is wrong. The key/value separator [{}] is missing.", me.getInternalName(), getPropertyKeyValueSeparator());
            return null;
        }

        String value = getKeyValueStoreAndDecryptIfNecessary().substring(separatorIdx + getPropertyKeyValueSeparator().length());

        if (me.isEncrypted() && decodePasswordWithBase64) {
            try {
                if (!Base64.isBase64(value)) {
                    logger.debug(
                            "        You defined to decode the value with base64, but this value isn't base64 encoded. [AraPropertyName={}],[Property={}] [Value={}]",
                            me.getInternalName(), me.getName(),
                            value);
                    return null;
                }
                byte[] decoded = Base64.decodeBase64(value);
                value = new String(decoded, encoding);
            } catch (UnsupportedEncodingException e) {
                logger.debug("Error while trying to decode base64 [AraPropertyName={}], [Property={}] with [Value={}]. Exception was:\n{}",
                        me.getInternalName(), me.getName(), value,
                        e.getMessage());
                return null;
            }
        }

        return value;
    };

    public String getKeyValueStoreAndDecryptIfNecessary() {
        String keyValueStore = me.getInternalValue();
        if (me.isEncrypted()) {
            keyValueStore = Maxim.deMaxim(me.getInternalValue());
        }

        return keyValueStore;
    }
}
