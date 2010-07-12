/*
 * Copyright (C) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.caliper;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class Xml {
  private static final String DATE_FORMAT_STRING = "yyyy-MM-dd'T'HH:mm:ssz";
  private static final String ENVIRONMENT_ELEMENT_NAME = "environment";
  private static final String RESULT_ELEMENT_NAME = "result";
  private static final String RUN_ELEMENT_NAME = "run";
  private static final String BENCHMARK_ATTRIBUTE = "benchmark";
  private static final String API_KEY_ATTRIBUTE = "apiKey";
  private static final String EXECUTED_TIMESTAMP_ATTRIBUTE = "executedTimestamp";
  private static final String SCENARIO_ELEMENT_NAME = "scenario";

  private static Element createResultElement(Document doc, Result result) {
    Element topElement = doc.createElement(RESULT_ELEMENT_NAME);
    topElement.appendChild(createEnvironmentElement(doc, result.getEnvironment()));
    topElement.appendChild(createRunElement(doc, result.getRun()));
    return topElement;
  }

  private static Result readResultElement(Element element) throws Exception {
    Environment environment = null;
    Run run = null;
    for (Node topLevelNode : childrenOf(element)) {
      if (topLevelNode.getNodeName().equals(ENVIRONMENT_ELEMENT_NAME)) {
        Element environmentElement = (Element) topLevelNode;
        environment = readEnvironmentElement(environmentElement);
      } else if (topLevelNode.getNodeName().equals(RUN_ELEMENT_NAME)) {
        run = readRunElement((Element) topLevelNode);
      } else {
        throw new RuntimeException("illegal node name: " + topLevelNode.getNodeName());
      }
    }

    if (environment == null || run == null) {
      throw new RuntimeException("missing environment or run elements");
    }

    return new Result(run, environment);
  }

  private static Element createEnvironmentElement(Document doc, Environment environment) {
    Element environmentElement = doc.createElement(ENVIRONMENT_ELEMENT_NAME);
    for (Map.Entry<String, String> entry : environment.getProperties().entrySet()) {
      environmentElement.setAttribute(entry.getKey(), entry.getValue());
    }
    return environmentElement;
  }

  private static Environment readEnvironmentElement(Element element) {
    return new Environment(attributesOf(element));
  }

  private static Element createRunElement(Document doc, Run run) {
    Element runElement = doc.createElement(RUN_ELEMENT_NAME);
    runElement.setAttribute(BENCHMARK_ATTRIBUTE, run.getBenchmarkName());
    runElement.setAttribute(API_KEY_ATTRIBUTE, run.getApiKey());
    String executedTimestampString = new SimpleDateFormat(DATE_FORMAT_STRING)
        .format(run.getExecutedTimestamp());
    runElement.setAttribute(EXECUTED_TIMESTAMP_ATTRIBUTE, executedTimestampString);

    for (Map.Entry<Scenario, MeasurementSet> entry : run.getMeasurements().entrySet()) {
      Element scenarioElement = doc.createElement(SCENARIO_ELEMENT_NAME);
      runElement.appendChild(scenarioElement);

      Scenario scenario = entry.getKey();
      for (Map.Entry<String, String> parameter : scenario.getVariables().entrySet()) {
        scenarioElement.setAttribute(parameter.getKey(), parameter.getValue());
      }
      scenarioElement.setTextContent(String.valueOf(entry.getValue()));
    }
    return runElement;
  }

  private static Run readRunElement(Element element) throws Exception {
    String benchmarkName = element.getAttribute(BENCHMARK_ATTRIBUTE);
    String apiKey = element.getAttribute(API_KEY_ATTRIBUTE);
    String executedDateString = element.getAttribute(EXECUTED_TIMESTAMP_ATTRIBUTE);
    Date executedDate = new SimpleDateFormat(DATE_FORMAT_STRING).parse(executedDateString);

    ImmutableMap.Builder<Scenario, MeasurementSet> measurementsBuilder = ImmutableMap.builder();
    for (Node node : childrenOf(element)) {
      Element scenarioElement = (Element) node;
      Scenario scenario = new Scenario(attributesOf(scenarioElement));
      MeasurementSet measurement = MeasurementSet.valueOf(scenarioElement.getTextContent());
      measurementsBuilder.put(scenario, measurement);
    }

    return new Run(measurementsBuilder.build(), benchmarkName, apiKey, executedDate);
  }

  /**
   * Encodes this run as XML to the specified stream. This XML can be parsed
   * with {@link #runFromXml(InputStream)}. Sample output:
   * <pre>{@code
   * <?xml version="1.0" encoding="UTF-8" standalone="no"?>
   * <run apiKey="56b35ad1-2985-4541-8f40-170471a46693"
   *      benchmark="examples.FooBenchmark"
   *      executedTimestamp="2010-07-12T11:38:47PDT">
   *   <scenario bar="15" foo="A" vm="dalvikvm">1200.1</scenario>
   *   <scenario bar="15" foo="B" vm="dalvikvm">1100.2</scenario>
   * </run>
   * }</pre>
   */
  public static void runToXml(Run run, OutputStream out) {
    try {
      Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
      doc.appendChild(createRunElement(doc, run));
      TransformerFactory.newInstance().newTransformer()
          .transform(new DOMSource(doc), new StreamResult(out));
    } catch (Exception e) {
      throw new IllegalStateException("Malformed XML document", e);
    }
  }

  /**
   * Encodes this environment as XML to the specified stream. This XML can be parsed
   * with {@link #environmentFromXml(InputStream)}. Sample output:
   * <pre>{@code
   * <?xml version="1.0" encoding="UTF-8" standalone="no"?>
   * <environment host.cpu.cachesize="[8192 KB x 4]" host.cpu.cores="[4 x 4]"
   *              ...
   *              jre.vmname="Java HotSpot(TM) 64-Bit Server VM" jre.vmversion="16.3-b01"
   *              os.arch="amd64" os.name="Linux" os.version="2.6.24-gg804011-generic"/>
   * }</pre>
   */
  public static void environmentToXml(Environment environment, OutputStream out) {
    try {
      Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
      doc.appendChild(createEnvironmentElement(doc, environment));
      TransformerFactory.newInstance().newTransformer()
          .transform(new DOMSource(doc), new StreamResult(out));
    } catch (Exception e) {
      throw new IllegalStateException("Malformed XML document", e);
    }
  }

  /**
   * Encodes this result as XML to the specified stream. This XML can be parsed
   * with {@link #resultFromXml(InputStream)}. Sample output:
   * <pre>{@code
   * <?xml version="1.0" encoding="UTF-8" standalone="no"?>
   * <result>
   *     <environment host.cpu.cachesize="[8192 KB x 4]" host.cpu.cores="[4 x 4]"
   *                  ...
   *                  jre.vmname="Java HotSpot(TM) 64-Bit Server VM" jre.vmversion="16.3-b01"
   *                  os.arch="amd64" os.name="Linux" os.version="2.6.24-gg804011-generic"/>
   *     <run apiKey="56b35ad1-2985-4541-8f40-170471a46693"
   *          benchmark="examples.FooBenchmark"
   *          executedTimestamp="2010-07-12T11:38:47PDT">
   *       <scenario bar="15" foo="A" vm="dalvikvm">1200.1</scenario>
   *       <scenario bar="15" foo="B" vm="dalvikvm">1100.2</scenario>
   *     </run>
   * </result>
   * }</pre>
   */
  public static void resultToXml(Result result, OutputStream out) {
    try {
      Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
      doc.appendChild(createResultElement(doc, result));
      TransformerFactory.newInstance().newTransformer()
          .transform(new DOMSource(doc), new StreamResult(out));
    } catch (Exception e) {
      throw new IllegalStateException("Malformed XML document", e);
    }
  }

  /**
   * Creates a result by decoding XML from the specified stream. The XML should
   * be consistent with the format emitted by {@link #runToXml(Run, OutputStream)}.
   */
  public static Run runFromXml(InputStream in) {
    try {
      Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
      return readRunElement(document.getDocumentElement());
    } catch (Exception e) {
      throw new IllegalStateException("Malformed XML document", e);
    }
  }

  /**
   * Creates an environment by decoding XML from the specified stream. The XML should
   * be consistent with the format emitted by {@link #environmentToXml(Environment, OutputStream)}.
   */
  public static Environment environmentFromXml(InputStream in) {
    try {
      Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
      Element environmentElement = document.getDocumentElement();
      return readEnvironmentElement(environmentElement);
    } catch (Exception e) {
      throw new IllegalStateException("Malformed XML document", e);
    }
  }

  public static Result resultFromXml(InputStream in) {
    try {
      Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
      return readResultElement(document.getDocumentElement());
    } catch (Exception e) {
      throw new IllegalStateException("Malformed XML document", e);
    }
  }

  private static ImmutableList<Node> childrenOf(Node node) {
    NodeList children = node.getChildNodes();
    ImmutableList.Builder<Node> result = ImmutableList.builder();
    for (int i = 0, size = children.getLength(); i < size; i++) {
      result.add(children.item(i));
    }
    return result.build();
  }

  private static ImmutableMap<String, String> attributesOf(Element element) {
    NamedNodeMap map = element.getAttributes();
    ImmutableMap.Builder<String, String> result = ImmutableMap.builder();
    for (int i = 0, size = map.getLength(); i < size; i++) {
      Attr attr = (Attr) map.item(i);
      result.put(attr.getName(), attr.getValue());
    }
    return result.build();
  }

  private Xml() {}
}
