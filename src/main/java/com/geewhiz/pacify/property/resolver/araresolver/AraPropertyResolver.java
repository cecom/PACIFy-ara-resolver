package com.geewhiz.pacify.property.resolver.araresolver;

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

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlbeam.XBProjector;

import com.geewhiz.pacify.defect.Defect;
import com.geewhiz.pacify.property.resolver.araresolver.model.AraData;
import com.geewhiz.pacify.property.resolver.araresolver.model.AraData.GenerateTask;
import com.geewhiz.pacify.property.resolver.araresolver.model.AraData.Task;
import com.geewhiz.pacify.property.resolver.araresolver.model.AraData.Variable;
import com.geewhiz.pacify.property.resolver.araresolver.model.GenerateTaskMixinImpl;
import com.geewhiz.pacify.property.resolver.araresolver.model.VariableMixinImpl;
import com.geewhiz.pacify.resolver.BasePropertyResolver;
import com.uc4.ara.feature.utils.Maxim;
import com.uc4.schemas.bond._2011_01.deploymentservice.DeploymentDescriptorResult;
import com.uc4.schemas.bond._2011_01.deploymentservice.DeploymentService;
import com.uc4.schemas.bond._2011_01.deploymentservice.DeploymentService_Service;

public class AraPropertyResolver extends BasePropertyResolver {

    private Logger              logger       = LogManager.getLogger(AraPropertyResolver.class.getName());

    private static final String SERVICE_NAME = "service/DeploymentService.svc";

    private String              araUrl;
    private String              userName;
    private String              password;
    private Integer             runId;
    private String              target;
    private String              component;
    private String              namespace;

    private boolean             initilized   = false;
    private AraData             araData;

    private Boolean             decodePasswordWithBase64;

    private String              beginToken;
    private String              endToken;

    private String              propertyKeyValueSeparator;

    public AraPropertyResolver() {
    }

    private synchronized void initialize() {
        if (isInitilized()) {
            return;
        }

        if (araUrl == null) {
            throw new IllegalArgumentException("Ara URL is null!");
        }

        if (userName == null) {
            throw new IllegalArgumentException("Ara Username is null!");
        }

        if (password == null) {
            throw new IllegalArgumentException("Ara User Password is null!");
        }

        if (runId == null) {
            throw new IllegalArgumentException("Ara Run Id is null!");
        }

        if (target == null) {
            throw new IllegalArgumentException("Ara Target is null!");
        }

        if (component == null) {
            throw new IllegalArgumentException("Ara Component is null!");
        }

        if (namespace == null) {
            throw new IllegalArgumentException("Ara Namespace is null!");
        }

        if (decodePasswordWithBase64 == null) {
            throw new IllegalArgumentException("Ara decodePasswortWithBase64 is null!");
        }

        QName qname = new QName("http://schemas.uc4.com/bond/2011-01/DeploymentService", "DeploymentService");
        URL wsdl = AraPropertyResolver.class.getResource("/DeploymentService.wsdl");

        DeploymentService_Service serviceFactory = new DeploymentService_Service(wsdl, qname);

        logger.debug("Webservice endpoint: {}", araUrl + "/" + SERVICE_NAME);

        DeploymentService webservice = serviceFactory.getBasicHttpBindingDeploymentService();
        BindingProvider bp = (BindingProvider) webservice;
        bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, araUrl + "/" + SERVICE_NAME);

        DeploymentDescriptorResult result = webservice.getDeploymentDescriptor(userName, password, runId);
        if (!result.isIsSuccess()) {
            throw new RuntimeException("Webservice call wasn't successful!\n" + result.getMessage().getValue());
        }

        logger.debug("Webservice response:");
        logger.debug(result.getDeploymentXML().getValue());

        XBProjector xbProjector = new XBProjector();
        xbProjector.mixins().addProjectionMixin(Variable.class, new VariableMixinImpl(getKeyValueSeparatorToken()));
        xbProjector.mixins().addProjectionMixin(GenerateTask.class, new GenerateTaskMixinImpl(getKeyValueSeparatorToken()));

        setAraData(xbProjector.onXMLString(result.getDeploymentXML().getValue()).createProjection(AraData.class));

        setInitilized(true);
    }

    public boolean containsProperty(String property) {
        initialize();

        Variable variable = getVariable(property);

        return variable != null;
    }

    public String getPropertyValue(String property) {
        initialize();

        Variable variable = getVariable(property);
        if (variable == null) {
            return null;
        }

        String value = variable.getValue();
        if (variable.isEncrypted()) {
            logger.debug("Property is encrypted. Decrypting.");

            String araDecoded = Maxim.deMaxim(value);
            if (isDecodePasswordWithBase64()) {
                logger.debug("Property is base64 encryped. Decrypting.");
                try {
                    return new String(Base64.decodeBase64(araDecoded), getEncoding());
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException("Error while decoding passwort", e);
                }
            }
        }
        return value;
    }

    private Variable getVariable(String property) {
        Task task = getAraData().getTask(component);
        if (task == null) {
            throw new IllegalArgumentException("Couldn't find component [" + component + "]");
        }

        GenerateTask generateTask = task.getGenerateTask(target);
        if (generateTask == null) {
            throw new IllegalArgumentException("Couldn't find target [" + target + "]");
        }

        Variable variable = generateTask.getVariable(namespace, property);
        return variable;
    }

    public Set<String> getPropertyKeys() {
        initialize();

        Set<String> result = new TreeSet<String>();

        Task task = getAraData().getTask(component);
        if (task == null) {
            throw new IllegalArgumentException("Couldn't find component [" + component + "]");
        }

        GenerateTask generateTask = task.getGenerateTask(target);
        if (generateTask == null) {
            throw new IllegalArgumentException("Couldn't find target [" + target + "]");
        }

        List<Variable> variables = generateTask.getVariables(namespace);
        for (Variable variable : variables) {
            result.add(variable.getName());
        }

        return result;
    }

    public String getEncoding() {
        return "utf-8";
    }

    public String getPropertyResolverDescription() {
        return "ARA";
    }

    public List<Defect> checkForDuplicateEntry() {
        return Collections.emptyList();
    }

    public String getBeginToken() {
        return beginToken;
    }

    public String getEndToken() {
        return endToken;
    }

    public String getAraUrl() {
        return araUrl;
    }

    public void setAraUrl(String araUrl) {
        this.araUrl = araUrl;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (password != null && password.startsWith("--10")) {
            this.password = Maxim.deMaxim(password);
        } else {
            this.password = password;
        }
    }

    public Integer getRunId() {
        return runId;
    }

    public void setRunId(Integer runId) {
        this.runId = runId;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    protected boolean isInitilized() {
        return initilized;
    }

    protected void setInitilized(boolean initilized) {
        this.initilized = initilized;
    }

    public AraData getAraData() {
        return araData;
    }

    protected void setAraData(AraData araData) {
        this.araData = araData;
    }

    @Override
    public boolean propertyUsesToken(String property) {
        if (getVariable(property).isEncrypted()) {
            return false;
        }
        return super.propertyUsesToken(property);
    }

    public void setDecodePasswordWithBase64(Boolean decodePasswordWithBase64) {
        this.decodePasswordWithBase64 = decodePasswordWithBase64;
    }

    public Boolean isDecodePasswordWithBase64() {
        return decodePasswordWithBase64;
    }

    public void setBeginToken(String beginToken) {
        this.beginToken = beginToken;
    }

    public void setEndToken(String endToken) {
        this.endToken = endToken;
    }

    public void setKeyValueSeparatorToken(String propertyKeyValueSeparator) {
        this.propertyKeyValueSeparator = propertyKeyValueSeparator;
    }

    public String getKeyValueSeparatorToken() {
        return propertyKeyValueSeparator;
    }

}
