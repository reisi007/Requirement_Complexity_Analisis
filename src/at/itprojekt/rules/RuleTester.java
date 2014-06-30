package at.itprojekt.rules;


import at.itprojekt.xml.generated.Report;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.*;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RuleTester {
    private Report report;
    private Rules rules;
    private static final List<String> rule_type = new ArrayList<>(), rule_allowedValues = new ArrayList<>();
    private static final String subjunctives = "subjunctives", zip = "zip", chars = "chars", words = "words", allSentenceSeperators = "allSentenceSeperators", innerSentenceSeperators = "innerSentenceSeperators", sentences = "sentences", abbreviations = "abbreviations";

    //Static constructor
    {
        rule_type.add(0, "project");
        rule_type.add(1, "line_key");
        rule_type.add(2, "line_value");
        rule_allowedValues.add(subjunctives);
        rule_allowedValues.add(zip);
        rule_allowedValues.add(chars);
        rule_allowedValues.add(words);
        rule_allowedValues.add(allSentenceSeperators);
        rule_allowedValues.add(innerSentenceSeperators);
        rule_allowedValues.add(sentences);
        rule_allowedValues.add(abbreviations);
    }

    // region getValue from Result
    private double getValue(String value, Report.Project.Result result) {
        assert rule_allowedValues.contains(value) : "Unknown type \"" + value + '"';
        switch (value) {
            case subjunctives:
                return result.getSubjunctives();
            case zip:
                return result.getZip();
            case chars:
                return result.getChars();
            case words:
                return result.getWords();
            case allSentenceSeperators:
                return result.getAllSentenceSeperators();
            case innerSentenceSeperators:
                return result.getInnerSentenceSeperators();
            case sentences:
                return result.getSentences();
            case abbreviations:
                return result.getAbbreviations();
            default:
                return Double.POSITIVE_INFINITY;
        }
    }

    private double getValue(String value, Report.Line.Result.Key result) {
        assert rule_allowedValues.contains(value) : "Unknown type \"" + value + '"';
        switch (value) {
            case subjunctives:
                return result.getSubjunctives();
            case chars:
                return result.getChars();
            case words:
                return result.getWords();
            case allSentenceSeperators:
                return result.getAllSentenceSeperators();
            case innerSentenceSeperators:
                return result.getInnerSentenceSeperators();
            case sentences:
                return result.getSentences();
            case abbreviations:
                return result.getAbbreviations();
            default:
                return Double.POSITIVE_INFINITY;
        }
    }

    private double getValue(String value, Report.Line.Result.Value result) {
        assert rule_allowedValues.contains(value) : "Unknown type \"" + value + '"';
        switch (value) {
            case subjunctives:
                return result.getSubjunctives();
            case chars:
                return result.getChars();
            case words:
                return result.getWords();
            case allSentenceSeperators:
                return result.getAllSentenceSeperators();
            case innerSentenceSeperators:
                return result.getInnerSentenceSeperators();
            case sentences:
                return result.getSentences();
            case abbreviations:
                return result.getAbbreviations();
            default:
                return Double.POSITIVE_INFINITY;
        }
    }
    //endregion

    /**
     * @param report      A report only missing testing against rules
     * @param rulesXMLurl A URL as string to the XML file containing the rules
     * @param rulesXSDurl A URL as string to the XSD file to verify the xsd file against
     */
    public RuleTester(Report report, String rulesXMLurl, String rulesXSDurl) {
        assert report != null && rulesXMLurl != null && rulesXSDurl != null;
        this.report = report;
        rules = generate(rulesXMLurl, rulesXSDurl);
        assert rules != null;
    }

    /**
     * Does the test and writes the changes to the Report
     */
    public void performTest() {
        double totalRuleWeight = 0, totalRuleValues = 0;
        for (Rule rule : rules.getListOfRules()) {
            assert rule_type.contains(rule.getType());
            totalRuleWeight += rule.getWeight();
            //region if rule type is project
            assert rule_type.indexOf(rule.getType()) != -1;
            if (rule.getType().equals(rule_type.get(0))) {
                double totalTestWeight = 0, ruleValue = 0;
                for (Test test : rule.getListOfTests()) {
                    totalTestWeight += test.getWeight();
                }
                for (Test test : rule.getListOfTests()) {
                    ruleValue += (test(test, report.getProject().getResult()) * (test.getWeight() / totalTestWeight));
                }
                //Add a result
                Report.Project.Result.RuleResults.RuleResult result = new Report.Project.Result.RuleResults.RuleResult();
                result.setId(rule.getId());
                result.setValue(ruleValue);
                totalRuleValues += (ruleValue * rule.getWeight());
                report.getProject().getResult().getRuleResults().getRuleResult().add(result);
            }
            //endregion
            //region if rule type is line_*
            else {
                boolean bKey = rule_type.indexOf(rule.getType()) == 1;
                double ruleValueTotal = 0;
                for (Report.Line line : report.getLine()) {
                    double ruleValueLine = 0, ruleWeightLine = 0;
                    for (Test test : rule.getListOfTests()) {
                        ruleWeightLine += test.getWeight();
                        if (bKey) {
                            ruleValueLine += test(test, line.getResult().getKey());
                        } else {
                            ruleValueLine += test(test, line.getResult().getValue());
                        }
                    }//end for Test test
                    double ruleValue4line = ruleValueLine / ruleWeightLine;
                    ruleValueTotal += ruleValue4line;
                    if (bKey) {
                        Report.Line.Result.Key.RuleResults.RuleResult result = new Report.Line.Result.Key.RuleResults.RuleResult();
                        result.setId(rule.getId());
                        result.setValue(ruleValue4line);
                        line.getResult().getKey().getRuleResults().getRuleResult().add(result);
                    } else {
                        Report.Line.Result.Value.RuleResults.RuleResult result = new Report.Line.Result.Value.RuleResults.RuleResult();
                        result.setId(rule.getId());
                        result.setValue(ruleValue4line);
                        line.getResult().getValue().getRuleResults().getRuleResult().add(result);
                    }
                }
                //Add total result to XML
                Report.Project.Result.RuleResults.RuleResult result = new Report.Project.Result.RuleResults.RuleResult();
                result.setId(rule.getId());
                result.setValue(ruleValueTotal / report.getLine().size());
                report.getProject().getResult().getRuleResults().getRuleResult().add(result);
                totalRuleValues += (result.getValue() * rule.getWeight());
            }
            //endregion

        }//end Rule rule for
        Report.Project.Result.RuleResults.RuleResult result = new Report.Project.Result.RuleResults.RuleResult();
        result.setId(-1);//@MagicContant --> //TODO better solution
        result.setValue(totalRuleValues / totalRuleWeight);
        report.getProject().getResult().getRuleResults().getRuleResult().add(result);
    }

    //region Do actual testing
    private double test(Test test, Report.Project.Result result) {
        return testHelper(test, getValue(test.getValue(), result));
    }

    private double test(Test test, Report.Line.Result.Key result) {
        return testHelper(test, getValue(test.getValue(), result));
    }

    private double test(Test test, Report.Line.Result.Value result) {
        return testHelper(test, getValue(test.getValue(), result));
    }

    private double testHelper(Test test, double value) {
        assert test != null : "Test is NULL";
        if (test.getYellow() > test.getRed()) { //  1 > 0.9 --> ]+INF, 1[ => green       [1,0.9[ => yellow       [0.9,-INF[ => red
            if (value > test.getYellow())
                return rules.getGreen();
            if (value <= test.getRed())
                return rules.getRed();
            return rules.getYellow();
        } else { //                                2 < 4 -->    ]+INF,4] => red         ]4,2] => yellow         ]2,-INF[ => green
            if (value >= test.getRed())
                return rules.getRed();
            if (value < test.getYellow())
                return rules.getGreen();
            return rules.getYellow();
        }
    }
//endregion

    private Rules generate(String xmlFileURL, String xsdFileUrl) {
        assert xmlFileURL != null && xsdFileUrl != null && xmlFileURL.endsWith(".xml") && xsdFileUrl.endsWith(".xsd");
        Rules rules = new Rules();
        try {
            // create JAXB context and initializing Marshaller
            JAXBContext jaxbContext = JAXBContext.newInstance(Rules.class);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            // specify the location and name of xml file to be read

            File XMLfile = new File(xmlFileURL);
            File XSDfile = new File(xsdFileUrl);

            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = sf.newSchema(XSDfile);
            jaxbUnmarshaller.setSchema(schema);
            jaxbUnmarshaller.setEventHandler(new MyValidationEventHandler());

            // this will create Java object "rules" from the XML file
            jaxbUnmarshaller.unmarshal(new InputSource(xmlFileURL));
            rules = (Rules) jaxbUnmarshaller.unmarshal(XMLfile);


        } catch (JAXBException e) {

            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
        return rules;
    }

    private static class MyValidationEventHandler implements ValidationEventHandler {

        public boolean handleEvent(ValidationEvent arg0) {
            System.out.println(arg0.getSeverity());
            return true;
        }

    }

}


