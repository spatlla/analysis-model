package edu.hm.hafner.analysis.parser.fxcop;

import java.util.Optional;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.ParsingCanceledException;
import edu.hm.hafner.analysis.ParsingException;
import edu.hm.hafner.analysis.ReaderFactory;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;
import edu.hm.hafner.util.XmlElementUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;


/**
 * Parses a fxcop xml report file.
 *
 * <p> Note that instances of this parser are not thread safe. </p>
 */
@SuppressWarnings("unused")
public class FxCopParser extends IssueParser {
    private static final long serialVersionUID = -7208558002331355408L;

    @SuppressFBWarnings({"UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR", "SE_TRANSIENT_FIELD_NOT_RESTORED"})
    private transient Report warnings;
    @SuppressFBWarnings({"UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR", "SE_TRANSIENT_FIELD_NOT_RESTORED"})
    private transient FxCopRuleSet ruleSet;

    @Override
    public Report parse(final ReaderFactory readerFactory) throws ParsingException, ParsingCanceledException {
        ruleSet = new FxCopRuleSet();
        warnings = new Report();

        Document doc = readerFactory.readDocument();

        NodeList mainNode = doc.getElementsByTagName("FxCopReport");

        Element rootElement = (Element) mainNode.item(0);
        parseRules(rootElement);
        parseNamespaces(rootElement, null);
        parseTargets(rootElement);

        return warnings;
    }

    private void parseRules(final Element rootElement) {
        Optional<Element> rulesElement = XmlElementUtil.getFirstChildElementByName(rootElement, "Rules");
        if (rulesElement.isPresent()) {
            for (Element rule : XmlElementUtil.getChildElementsByName(rulesElement.get(), "Rule")) {
                ruleSet.addRule(rule);
            }
        }
    }

    private void parseTargets(final Element rootElement) {
        Optional<Element> targetsElement = XmlElementUtil.getFirstChildElementByName(rootElement, "Targets");
        if (targetsElement.isPresent()) {
            for (Element target : XmlElementUtil.getChildElementsByName(targetsElement.get(), "Target")) {
                String name = getString(target, "Name");
                parseMessages(target, name);
                parseModules(target, name);
                parseResources(target, name);
            }
        }
    }

    private void parseResources(final Element target, final String parentName) {
        Optional<Element> resources = XmlElementUtil.getFirstChildElementByName(target, "Resources");
        if (resources.isPresent()) {
            for (Element resource : XmlElementUtil.getChildElementsByName(resources.get(), "Resource")) {
                String name = getString(resource, "Name");
                parseMessages(resource, name);
            }
        }
    }

    private void parseModules(final Element target, final String parentName) {
        Optional<Element> modulesElement = XmlElementUtil.getFirstChildElementByName(target, "Modules");
        if (modulesElement.isPresent()) {
            for (Element module : XmlElementUtil.getChildElementsByName(modulesElement.get(), "Module")) {
                String name = getString(module, "Name");
                parseMessages(module, name);
                parseNamespaces(module, name);
            }
        }
    }

    private void parseNamespaces(final Element rootElement, final String parentName) {
        Optional<Element> namespacesElement = XmlElementUtil.getFirstChildElementByName(rootElement, "Namespaces");
        if (namespacesElement.isPresent()) {
            for (Element namespace : XmlElementUtil.getChildElementsByName(namespacesElement.get(), "Namespace")) {
                String name = getString(namespace, "Name");

                parseMessages(namespace, name);
                parseTypes(namespace, name);
            }
        }
    }

    private void parseTypes(final Element typesElement, final String parentName) {
        Optional<Element> types = XmlElementUtil.getFirstChildElementByName(typesElement, "Types");
        if (types.isPresent()) {
            for (Element type : XmlElementUtil.getChildElementsByName(types.get(), "Type")) {
                String name = parentName + "." + getString(type, "Name");

                parseMessages(type, name);
                parseMembers(type, name);
            }
        }
    }

    private void parseMembers(final Element members, final String parentName) {
        Optional<Element> membersElement = XmlElementUtil.getFirstChildElementByName(members, "Members");
        if (membersElement.isPresent()) {
            for (Element member : XmlElementUtil.getChildElementsByName(membersElement.get(), "Member")) {
                parseMember(member, parentName);
            }
        }
    }

    private void parseAccessors(final Element accessorsElement, final String parentName) {
        Optional<Element> accessors = XmlElementUtil.getFirstChildElementByName(accessorsElement, "Accessors");
        if (accessors.isPresent()) {
            for (Element member : XmlElementUtil.getChildElementsByName(accessors.get(), "Accessor")) {
                parseMember(member, parentName);
            }
        }
    }

    private void parseMember(final Element member, final String parentName) {
        parseMessages(member, parentName);
        parseAccessors(member, parentName);
    }

    private void parseMessages(final Element messages, final String parentName) {
        Optional<Element> messagesElement = XmlElementUtil.getFirstChildElementByName(messages, "Messages");
        if (messagesElement.isPresent()) {
            for (Element message : XmlElementUtil.getChildElementsByName(messagesElement.get(), "Message")) {
                for (Element issue : XmlElementUtil.getChildElementsByName(message, "Issue")) {
                    parseIssue(issue, message, parentName);
                }
            }
        }
    }

    private void parseIssue(final Element issue, final Element parent, final String parentName) {
        String typeName = getString(parent, "TypeName");
        String category = getString(parent, "Category");
        String checkId = getString(parent, "CheckId");
        String issueLevel = getString(issue, "Level");

        StringBuilder msgBuilder = new StringBuilder();
        FxCopRule rule = ruleSet.getRule(category, checkId);
        if (rule == null) {
            msgBuilder.append(typeName);
        }
        else {
            msgBuilder.append("<a href=\"");
            msgBuilder.append(rule.getUrl());
            msgBuilder.append("\">");
            msgBuilder.append(typeName);
            msgBuilder.append("</a>");
        }
        msgBuilder.append(" - ");
        msgBuilder.append(issue.getTextContent());

        String filePath = getString(issue, "Path");
        String fileName = getString(issue, "File");
        String fileLine = getString(issue, "Line");

        IssueBuilder builder = new IssueBuilder().setFileName(filePath + "/" + fileName)
                .setLineStart(fileLine)
                .setCategory(category)
                .setMessage(msgBuilder.toString())
                .setSeverity(getPriority(issueLevel));
        if (rule != null) {
            builder.setDescription(rule.getDescription());
        }
        warnings.add(builder.build());
    }

    private String getString(final Element element, final String name) {
        if (element.hasAttribute(name)) {
            return element.getAttribute(name);
        }
        else {
            return "";
        }
    }

    private Severity getPriority(final String issueLevel) {
        if (issueLevel.contains("Error") || issueLevel.contains("Critical")) {
            return Severity.WARNING_HIGH;
        }
        if (issueLevel.contains("Warning")) {
            return Severity.WARNING_NORMAL;
        }
        return Severity.WARNING_LOW;
    }
}
