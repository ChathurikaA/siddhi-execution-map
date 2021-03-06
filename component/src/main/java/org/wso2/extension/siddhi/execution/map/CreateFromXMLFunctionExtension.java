/*
 * Copyright (c)  2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.extension.siddhi.execution.map;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.lang3.math.NumberUtils;
import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.ReturnAttribute;
import org.wso2.siddhi.annotation.util.DataType;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.exception.SiddhiAppRuntimeException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.SiddhiAppValidationException;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.stream.XMLStreamException;


/**
 * createFromXML(String)
 * Returns the created hashmap
 * Accept Type(s): (String)
 * Return Type(s): Map
 */
@Extension(
        name = "createFromXML",
        namespace = "map",
        description = "Returns the map created with the key values pairs given in the XMLstring.",
        parameters = {
                @Parameter(name = "xml.string",
                        description = "XML as a string, which is used to create the map.",
                        type = DataType.STRING,
                optional = false
                )
        },
        examples = @Example(
                description = " returns a map with the keys \"symbol\", \"price\", \"volume\"," +
                        " and with the values \"IBM\", 200 and 100 respectively.",
                syntax = "createFromJSON(“{‘symbol' : 'IBM' , 'price' : 200, 'volume' : 100}”)"),
        returnAttributes = @ReturnAttribute(description = "will return a map", type = DataType.OBJECT)
)
public class CreateFromXMLFunctionExtension extends FunctionExecutor {
    private Attribute.Type returnType = Attribute.Type.OBJECT;
    private NumberFormat numberFormat = NumberFormat.getInstance();

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors,
                        ConfigReader configReader,
                        SiddhiAppContext siddhiAppContext) {
        if ((attributeExpressionExecutors.length) != 1) {
            throw new SiddhiAppValidationException("Invalid no of arguments passed to map:createFromXML() function, " +
                    "required only 1, but found " + attributeExpressionExecutors.length);
        }
    }

    @Override
    protected Object execute(Object[] data) {
        return null;
    }

    @Override
    protected Object execute(Object data) {
        if (data instanceof String) {
            try {
                OMElement parentElement = AXIOMUtil.stringToOM(data.toString());
                return getMapFromXML(parentElement);
            } catch (XMLStreamException e) {
                throw new SiddhiAppRuntimeException("Input data cannot be parsed to xml: " + e.getMessage(), e);
            }
        } else {
            throw new SiddhiAppRuntimeException("Data should be a string");
        }
    }

    private Object getMapFromXML(OMElement parentElement) throws XMLStreamException {
        Map<Object, Object> topLevelMap = new HashMap<Object, Object>();
        Iterator iterator = parentElement.getChildElements();
        while (iterator.hasNext()) {
            OMElement streamAttributeElement = (OMElement) iterator.next();
            String key = streamAttributeElement.getQName().toString();
            Object value;
            if (streamAttributeElement.getFirstElement() != null) {
                value = getMapFromXML(streamAttributeElement);
            } else {
                String elementText = streamAttributeElement.getText();
                if (elementText.equals("true") || elementText.equals("false")) {
                    value = Boolean.parseBoolean(elementText);
                } else {
                    if (NumberUtils.isNumber(elementText)) {
                        try {
                            value = numberFormat.parse(elementText);
                        } catch (ParseException e) {
                            value = elementText;
                        }
                    } else {
                        value = elementText;
                    }
                }
            }
            topLevelMap.put(key, value);
        }
        return topLevelMap;
    }

    @Override
    public void start() {
        //Nothing to start
    }

    @Override
    public void stop() {
        //Nothing to stop
    }

    @Override
    public Attribute.Type getReturnType() {
        return returnType;
    }

    @Override
    public Map<String, Object> currentState() {
        return null;    //No need to maintain a state.
    }

    @Override
    public void restoreState(Map<String, Object> state) {
        //Since there's no need to maintain a state, nothing needs to be done here.
    }
}


