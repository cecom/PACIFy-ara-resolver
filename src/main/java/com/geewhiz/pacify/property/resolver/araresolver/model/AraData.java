package com.geewhiz.pacify.property.resolver.araresolver.model;

import java.util.List;

import org.xmlbeam.annotation.XBRead;

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

public interface AraData {

    public interface Variable extends VariableMixin {

        @XBRead("@name")
        String getInternalName();

        @XBRead("@value")
        String getInternalValue();

        @XBRead("boolean(@isEncrypted)")
        Boolean isEncrypted();

    }

    public interface GenerateTask extends GenerateTaskMixin {
        @XBRead("@alias")
        String getAlias();

        @XBRead("./variables/variable[starts-with(@name,'{0}/')]")
        List<Variable> getVariablesForNamespace(String forNamespace);
        
        // for performance
        @XBRead("./variables/variable[starts-with(@name,'{0}/')][starts-with(@value,'{1}')]")
        List<Variable> getVariablesForNamespaceDirect(String forNamespace, String property);
        
        @XBRead("./variables/variable[starts-with(@name,'{0}/')][@isEncrypted=true()]")
        List<Variable> getVariablesWhichAreEncrypted(String forNamespace);
    }

    public interface Task {
        @XBRead("@parentComponent")
        String getParentComponent();

        @XBRead("@component")
        String getComponent();

        // ends-with not supported so we need to write our own
        @XBRead("./modification/generateTask['{0}' = substring(@alias, string-length(@alias) - string-length('{0}') + 1)]")
        GenerateTask getGenerateTask(String target);
    }

    @XBRead("/processFlowModification/task[@component='{0}']")
    Task getTask(String componentName);

}
