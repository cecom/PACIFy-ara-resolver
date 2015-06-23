package com.geewhiz.pacify.property.resolver.araresolver;

import java.util.Map;

import com.geewhiz.pacify.resolver.PropertyResolver;
import com.geewhiz.pacify.resolver.PropertyResolverModule;
import com.google.inject.Provides;
import com.google.inject.multibindings.Multibinder;

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

public class AraPropertyResolverModule extends PropertyResolverModule {

    Map<String, String> commandLineParameters;

    @Override
    public String getResolverId() {
        return "AraResolver";
    }

    @Override
    protected void configure() {
        Multibinder<PropertyResolver> resolveBinder = Multibinder.newSetBinder(binder(), PropertyResolver.class);
        resolveBinder.addBinding().to(AraPropertyResolver.class);
    }

    @Override
    public void setParameters(Map<String, String> commandLineParameters) {
        this.commandLineParameters = commandLineParameters;
    }

    @Provides
    public AraPropertyResolver createFilePropertyResolver() {
        String araUrl = commandLineParameters.get("araUrl");
        String username = commandLineParameters.get("username");
        String password = commandLineParameters.get("password");
        String runId = commandLineParameters.get("runId");
        String target = commandLineParameters.get("target");
        String component = commandLineParameters.get("component");
        String namespace = commandLineParameters.get("namespace");
        String beginToken = commandLineParameters.get("beginToken");
        String endToken = commandLineParameters.get("endToken");
        Boolean decodePasswordWithBase64 = Boolean.parseBoolean(commandLineParameters.get("decodePasswordWithBase64"));

        checkNotNull(araUrl, "araUrl is null! Please specify it via -DAraResolver.araUrl=<url>");
        checkNotNull(username, "username is null! Please specify it via -DAraResolver.username=<username>. The user is used to authenticate with ara.");
        checkNotNull(password, "password is null! Please specify it via -DAraResolver.password=<password>. The password is used to authenticate with ara.");
        checkNotNull(runId, "runId is null! Please specify it via -DAraResolver.runId=<runId>.");
        checkNotNull(target, "target is null! Please specify it via -DAraResolver.target=<target>.");
        checkNotNull(component, "component is null! Please specify it via -DAraResolver.component=<component>.");
        checkNotNull(namespace, "namespace is null! Please specify it via -DAraResolver.namespace=<namespace>.");

        AraPropertyResolver araPropertyResolver = new AraPropertyResolver();
        araPropertyResolver.setAraUrl(araUrl);
        araPropertyResolver.setUserName(username);
        araPropertyResolver.setPassword(password);
        araPropertyResolver.setRunId(Integer.parseInt(runId));
        araPropertyResolver.setTarget(target);
        araPropertyResolver.setComponent(component);
        araPropertyResolver.setNamespace(namespace);
        araPropertyResolver.setDecodePasswordWithBase64(decodePasswordWithBase64);
        araPropertyResolver.setBeginToken(beginToken == null ? "@" : beginToken);
        araPropertyResolver.setEndToken(endToken == null ? "@" : endToken);

        return araPropertyResolver;
    }

    private void checkNotNull(String param, String message) {
        if (param == null) {
            throw new IllegalArgumentException(message);
        }
    }
}
