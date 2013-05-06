/*______________________________________________________________________________
 *
 * Macker   http://innig.net/macker/
 *
 * Copyright 2002-2003 Paul Cantrell
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2, as published by the
 * Free Software Foundation. See the file LICENSE.html for more information.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the license for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
 * Place, Suite 330 / Boston, MA 02111-1307 / USA.
 *______________________________________________________________________________
 */
 
package com.github.benromberg.macker;


import net.innig.collect.*;

import java.io.*;
import java.util.*;

import org.jdom.input.SAXBuilder;

import com.github.benromberg.macker.event.*;
import com.github.benromberg.macker.rule.*;
import com.github.benromberg.macker.structure.*;

/**
    The main class for the command line interface.
*/
public class Macker
    {
    //------------------------------------------------------------------------
    // Static
    //------------------------------------------------------------------------
    
    public static void main(String[] args)
        throws Exception
        {
        try
            {
            // Parse args
            
           	Macker macker = new Macker();
            
            boolean nextIsRule = false;
            for(int arg = 0; arg < args.length; arg++)
                {
                if(args[arg].equals("-h")
                || args[arg].equals("-help")
                || args[arg].equals("--help"))
                    {
                    commandLineUsage();
                    return;
                    }
                else if(args[arg].equals("-V") || args[arg].equals("--version"))
                    {
                    Properties p = new Properties();
                    p.load(Macker.class.getClassLoader().getResourceAsStream("net/innig/macker/version.properties"));
                    System.out.println("Macker " + p.get("macker.version.long"));
                    System.out.println("http://innig.net/macker/");
                    System.out.println("Licensed under GPL v2.1; see LICENSE.html");
                    return;
                    }
                else if(args[arg].equals("-v") || args[arg].equals("--verbose"))
                    macker.setVerbose(true);
                else if(args[arg].startsWith("-D") || args[arg].equals("--define"))
                    {
                    int initialPos = 0, equalPos;
                    if(args[arg].length() == 2 || args[arg].equals("--define"))
                        arg++;
                    else
                        initialPos = 2;
                    
                    equalPos = args[arg].indexOf('=');
                    if(equalPos == -1)
                        {
                        System.out.println("-D argument doesn't have name=value form: " + args[arg]);
                        commandLineUsage();
                        return;
                        }
                    String varName = args[arg].substring(initialPos, equalPos);
                    String value   = args[arg].substring(equalPos + 1);
                    macker.setVariable(varName, value);
                    }
                else if(args[arg].equals("-o") || args[arg].equals("--output")) 
                    macker.setXmlReportFile(new File(args[++arg]));
                else if(args[arg].equals("--print-max"))
                    macker.setPrintMaxMessages(Integer.parseInt(args[++arg]));
                else if(args[arg].equals("--print"))
                    macker.setPrintThreshold(RuleSeverity.fromName(args[++arg]));
                else if(args[arg].equals("--anger"))
                    macker.setAngerThreshold(RuleSeverity.fromName(args[++arg]));
                else if(args[arg].equals("-r") || args[arg].equals("--rulesfile"))
                    nextIsRule = true;
                else if(args[arg].endsWith(".xml") || nextIsRule)
                    {
                    macker.addRulesFile(new File(args[arg]));
                    nextIsRule = false;
                    }
                else if(args[arg].endsWith(".class"))
                    macker.addClass(new File(args[arg]));
                else
                    {
                    System.out.println();
                    System.out.println("macker: Unknown file type: " + args[arg]);
                    System.out.println("(expected .class or .xml)");
                    commandLineUsage();
                    return;
                    }
                }
            
            macker.check();
            
            if(!macker.hasRules() || !macker.hasClasses())
                commandLineUsage();
            }
        catch(MackerIsMadException mime)
            {
            System.out.println(mime.getMessage());
            System.exit(2);
            }
        catch(IncompleteClassInfoException icie)
            {
            System.out.println(icie.getMessage());
            throw icie;
            }
        catch(Exception e)
            {
            e.printStackTrace(System.out);
            commandLineUsage();
            throw e;
            }
        }
    
    public static void commandLineUsage()
        {
        System.out.println("usage: macker [opts]* <rules files> <classes>");
        System.out.println("          -r, --rulesfile <rules.xml>");
        System.out.println("          -o, --output <report.xml>");
        System.out.println("          -D, --define <var>=<value>");
        System.out.println("              --print <threshold>");
        System.out.println("              --anger <threshold>");
        System.out.println("              --print-max <max-messages>");
        System.out.println("          -v, --verbose");
        System.out.println("          -V, --version");
        }
    
    //------------------------------------------------------------------------
    // Instance
    //------------------------------------------------------------------------
    
    public Macker()
        {
    	cm = new ClassManager();
    	ruleSets = new ArrayList();
    	vars = new HashMap();
    	verbose = false;
        }
    
    public void addClass(File classFile)
        throws IOException, ClassParseException
        {
        cm.makePrimary(
            cm.readClass(classFile));
        }
    
    public void addClass(InputStream classFile)
        throws IOException, ClassParseException
        {
        cm.makePrimary(
            cm.readClass(classFile));
        }

    public void addClass(String className)
        throws ClassNotFoundException
        {
        cm.makePrimary(
            cm.getClassInfo(className));
        }

    public void addReachableClasses(Class initialClass, final String primaryPrefix)
        throws IncompleteClassInfoException
        { addReachableClasses(initialClass.getName(), primaryPrefix); }
    
    /**
     * For determining the primary classes when you don't have a hard-coded class
     * list, or knowledge of the file system where classes are stored. Determines
     * the set of primary classes by walking the class reference graph out from
     * the initial class name, and marking all classes which start with primaryPrefix.
     */
    public void addReachableClasses(String initialClassName, final String primaryPrefix)
        throws IncompleteClassInfoException
        {
        Graphs.reachableNodes(
            cm.getClassInfo(initialClassName),
            new GraphWalker()
                {
                public Collection getEdgesFrom(Object node)
                    {
                    ClassInfo classInfo = (ClassInfo) node;
                    cm.makePrimary(classInfo);
                    return InnigCollections.select(
                        classInfo.getReferences().keySet(),
                        new Selector()
                            {
                            public boolean select(Object classInfo)
                                {
                                return ((ClassInfo) classInfo)
                                    .getFullName()
                                    .startsWith(primaryPrefix);
                                }
                            });
                    }
                });
        }
    
    public boolean hasClasses()
        { return !cm.getPrimaryClasses().isEmpty(); }
        
    
    public void addRulesFile(File rulesFile)
        throws IOException, RulesException
        { ruleSets.addAll(new RuleSetBuilder().build(rulesFile)); }
    
    public void addRulesFile(InputStream rulesFile)
        throws IOException, RulesException
        { ruleSets.addAll(new RuleSetBuilder().build(rulesFile)); }

    public void addRuleSet(RuleSet ruleSet)
        throws IOException, RulesException
        { ruleSets.add(ruleSet); }
    
    public void addListener(MackerEventListener listener)
        { listeners.add(listener); }
    
    public boolean hasRules()
        { return !ruleSets.isEmpty(); }
    
    public void setVariable(String name, String value)
        { vars.put(name, value); }
    
    public void setVerbose(boolean verbose)
        { this.verbose = verbose; }
    
    public void setClassLoader(ClassLoader classLoader)
        { cm.setClassLoader(classLoader); }
    
    public void setPrintMaxMessages(int printMaxMessages)
        { this.printMaxMessages = printMaxMessages; }
    
    public void setPrintThreshold(RuleSeverity printThreshold)
        { this.printThreshold = printThreshold; }
    
    public void setAngerThreshold(RuleSeverity angerThreshold)
        { this.angerThreshold = angerThreshold; }
    
    public void setXmlReportFile(File xmlReportFile) 
        { this.xmlReportFile = xmlReportFile; }

    /**
     * Performs rule checking with the default printing, throwing, and XML reporting listeners.
     **/
    public void check()
        throws MackerIsMadException, RulesException, ListenerException
        {
        if(!hasRules())
            System.out.println("WARNING: No rules files specified");
        if(!hasClasses())
            System.out.println("WARNING: No class files specified");

        if(verbose)
            {
            System.out.println(cm.getPrimaryClasses().size() + " primary classes");
            System.out.println(cm.getAllClasses().size() + " total classes");
            System.out.println(cm.getReferences().size() + " references");
            
            for(Iterator i = cm.getPrimaryClasses().iterator(); i.hasNext(); )
                {
                ClassInfo classInfo = (ClassInfo) i.next();
                System.out.println("Classes used by " + classInfo + ":");
                for(Iterator usedIter = classInfo.getReferences().keySet().iterator(); usedIter.hasNext(); )
                    System.out.println("    " + usedIter.next());
                System.out.println();
                }
            }

        PrintingListener printing;
        if(printThreshold == null)
            printing = null;
        else
            {
            printing = new PrintingListener(System.out);
            printing.setThreshold(printThreshold);
            if(printMaxMessages > 0)
                printing.setMaxMessages(printMaxMessages);
            addListener(printing);
            }

        ThrowingListener throwing;
        if(angerThreshold == null)
            throwing = null;
        else
            {
            throwing = new ThrowingListener(null, angerThreshold);
            addListener(throwing);
            }

        XmlReportingListener xmlReporting = null;
        if(xmlReportFile != null)
            {
            xmlReporting = new XmlReportingListener(xmlReportFile);
            addListener(xmlReporting);
            }
        
        checkRaw();
        
        if(printing != null)
            printing.printSummary();
        if(xmlReporting != null)
            {
            xmlReporting.flush();
            xmlReporting.close();
            }
        if(throwing != null)
            throwing.timeToGetMad();
        }
    
    /**
     * Performs rule checking without any default listeners.
     **/
    public void checkRaw()
        throws MackerIsMadException, RulesException, ListenerException
        {
        for(Iterator rsIter = ruleSets.iterator(); rsIter.hasNext(); )
            {
            RuleSet rs = (RuleSet) rsIter.next();
            
            if(verbose)
                for(Iterator patIter = rs.getAllPatterns().iterator(); patIter.hasNext(); )
                    {
                    final Pattern pat = (Pattern) patIter.next();
                    final EvaluationContext ctx = new EvaluationContext(cm, rs);
                    System.out.println("matching " + pat);
                    for(Iterator i = cm.getPrimaryClasses().iterator(); i.hasNext(); )
                        {
                        ClassInfo classInfo = (ClassInfo) i.next();
                        if(pat.matches(ctx, classInfo))
                            System.out.println("    " + classInfo);
                        }
                    System.out.println();
                    }
                    
            EvaluationContext context = new EvaluationContext(cm, rs);
            context.setVariables(vars);
            for(Iterator listenIter = listeners.iterator(); listenIter.hasNext(); )
                context.addListener((MackerEventListener) listenIter.next());
            
            rs.check(context, cm);
            }
        }

    private ClassManager cm;
    private Collection/*<RuleSet>*/ ruleSets;
    private Map/*<String,String>*/ vars;
    private boolean verbose;
    private File xmlReportFile;
    private List/*<MackerEventListener>*/ listeners = new ArrayList();
    private int printMaxMessages;
    private RuleSeverity printThreshold = RuleSeverity.INFO, angerThreshold = RuleSeverity.ERROR;
    }
