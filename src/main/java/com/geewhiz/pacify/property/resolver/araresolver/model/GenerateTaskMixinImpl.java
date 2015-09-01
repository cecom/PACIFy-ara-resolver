package com.geewhiz.pacify.property.resolver.araresolver.model;

import java.util.ArrayList;
import java.util.List;

import com.geewhiz.pacify.property.resolver.araresolver.model.AraData.GenerateTask;
import com.geewhiz.pacify.property.resolver.araresolver.model.AraData.Variable;

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

public class GenerateTaskMixinImpl extends AbstractMixin implements GenerateTaskMixin {

    private GenerateTask me;

    public GenerateTaskMixinImpl(String propertyKeyValueSeparator) {
        super(propertyKeyValueSeparator);
    }

    /**
     * Fetch all variables which contains the separator
     */
    public List<Variable> getVariables(String forNamespace) {
        List<Variable> result = new ArrayList<Variable>();

        for (Variable variable : me.getVariablesForNamespace(forNamespace)) {
            if (variable.getKeyValueStoreAndDecryptIfNecessary().contains(getPropertyKeyValueSeparator())) {
                result.add(variable);
            }
        }

        return result;
    }

    public Variable getVariable(String variable) {
        for (Variable current : me.getVariables()) {
            if (current.getName().equals(variable)) {
                return current;
            }
        }
        return null;
    }

    public Variable getVariable(String forNamespace, String property) {
        for (Variable current : me.getVariablesForNamespace(forNamespace)) {
            if (property.equals(current.getName())) {
                return current;
            }
        }

        return null;
    }

}
