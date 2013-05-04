package net.innig.macker.event;

import net.innig.macker.rule.RuleSet;
import net.innig.macker.structure.ClassInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.Writer;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Iterator;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

public class XmlReportingListener
    implements MackerEventListener
    {
 	private Writer out;
    private String encoding;
    
    private Document document;
    private Element curElem;
    private LinkedList elemStack;
    
    public XmlReportingListener(File outFile)
        throws ListenerException
        {
        try {
            if(outFile.exists())
                outFile.delete();
            OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(outFile));
            BufferedWriter bufferedOut = new BufferedWriter(out);
            init(bufferedOut, out.getEncoding());
            }
        catch(IOException ioe)
            { throw new ListenerException(this, "Unable to remove and re-create report file \"" + outFile + "\"", ioe); }
        }
    
    public XmlReportingListener(Writer out, String encoding)
        throws ListenerException
        { init(out, encoding); }
    
    private void init(Writer out, String encoding)
        throws ListenerException
        {
        this.out = out;
        this.encoding = encoding;
        
        elemStack = new LinkedList();
        pushElem(new Element("macker-report"));
        document = new Document(curElem);
        }
    
    public void flush()
        throws ListenerException
        {
        try
            {
            XMLOutputter xmlOut = new XMLOutputter("    ", true, encoding);
            xmlOut.output(document, out);
            out.flush();
            }
        catch(IOException ioe)
            { throw new ListenerException(this, "Unable to write XML report", ioe); }
        }
    
    public void close()
        throws ListenerException
        {
        try { out.close(); }
        catch(IOException ioe)
            { throw new ListenerException(this, "Unable to close XML report", ioe); }
        }
    
	public void mackerStarted(RuleSet ruleSet)
        {
        if(ruleSet.hasName())
            {
            Element ruleSetElem = new Element("ruleset");
            ruleSetElem.setAttribute("name", ruleSet.getName());
            curElem.addContent(ruleSetElem);
            pushElem(ruleSetElem);
            }
        else
            pushElem(curElem); // push again so finish can pop
        }

	public void mackerFinished(RuleSet ruleSet)
        throws MackerIsMadException, ListenerException
        { popElem(); }

	public void mackerAborted(RuleSet ruleSet)
        { curElem = null; }

	public void handleMackerEvent(RuleSet ruleSet, MackerEvent event)
		throws MackerIsMadException
        {
        if(event instanceof MessageEvent)
            {
            Element messageRuleElem = new Element("message-rule");
            handleEventBasics(messageRuleElem, event);
            curElem.addContent(messageRuleElem);
            }
        
        if(event instanceof AccessRuleViolation)
            {
            AccessRuleViolation violation = (AccessRuleViolation) event;
            Element violationElem = new Element("access-rule-violation");
            
            handleEventBasics(violationElem, violation);
            
            Element fromElem = new Element("from");
            Element   toElem = new Element("to");
            describeClass(fromElem, violation.getFrom());
            describeClass(  toElem, violation.getTo());
            violationElem.addContent(fromElem);
            violationElem.addContent(toElem);
            
            curElem.addContent(violationElem);
            }
        
        if(event instanceof ForEachStarted)
            {
            ForEachStarted forEachStarted = (ForEachStarted) event;
            Element forEachElem = new Element("foreach");
            forEachElem.setAttribute("var", forEachStarted.getForEach().getVariableName());
            curElem.addContent(forEachElem);
            pushElem(forEachElem);
            }
        
        if(event instanceof ForEachIterationStarted)
            {
            ForEachIterationStarted forEachIter = (ForEachIterationStarted) event;
            Element iterElem = new Element("iteration");
            iterElem.setAttribute("value", forEachIter.getVariableValue());
            curElem.addContent(iterElem);
            pushElem(iterElem);
            }

        if(event instanceof ForEachIterationFinished || event instanceof ForEachFinished)
            popElem();
        }

    private void handleEventBasics(Element elem, MackerEvent event)
        {
        elem.setAttribute("severity", event.getRule().getSeverity().getName());
        for(Iterator msgIter = event.getMessages().iterator(); msgIter.hasNext(); )
            {
            String message = (String) msgIter.next();
            Element messageElem = new Element("message");
            messageElem.setText(message);
            elem.addContent(messageElem);
            }
        }
    
    private void describeClass(Element classInfoElem, ClassInfo classInfo)
        {
        Element fullElem = new Element("full-name");
        Element classElem = new Element("class");
        Element packElem = new Element("package");
        fullElem.setText(classInfo.getFullName());
        classElem.setText(classInfo.getClassName());
        packElem.setText(classInfo.getPackageName());
        classInfoElem.addContent(fullElem);
        classInfoElem.addContent(classElem);
        if(classInfo.getPackageName() != null)
            classInfoElem.addContent(packElem);
        }
    
    private void pushElem(Element elem)
        {
        elemStack.addLast(curElem);
        curElem = elem;
        }
    
    private void popElem()
        { curElem = (Element) elemStack.removeLast(); }
    
    public String toString()
        { return "XmlReportingListener"; }
    }
