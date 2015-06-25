package com.geewhiz.pacify.property.resolver.araresolver;

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

    interface Variable {
        @XBRead("@name")
        String getInternalName();

        @XBRead("substring-before(@value, '=>')")
        String getName();

        @XBRead("substring-after(@value, '=>')")
        String getValue();

        @XBRead("boolean(@isEncrypted)")
        Boolean isEncrypted();
    }

    interface GenerateTask {
        @XBRead("@alias")
        String getAlias();

        @XBRead("./variables/variable")
        List<Variable> getVariables();

        @XBRead("./variables/variable[starts-with(@name,'{0}') and contains(@value, '=>')]")
        List<Variable> getVariables(String forNamespace);

        @XBRead("./variables/variable[starts-with(@value,'{0}=>')]")
        Variable getVariable(String variable);

        @XBRead("./variables/variable[starts-with(@name,'{0}') and starts-with(@value,'{1}=>')]")
        Variable getVariable(String forNamespace, String variable);
    }

    interface Task {
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
