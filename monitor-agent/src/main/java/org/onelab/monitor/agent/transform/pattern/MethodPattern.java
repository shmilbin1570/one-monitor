package org.onelab.monitor.agent.transform.pattern;

import org.onelab.monitor.agent.config.Config;
import org.onelab.monitor.agent.log.AgentLogger;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 方法匹配模板
 * Created by chunliangh on 14-12-8.
 */
public class MethodPattern {

    private Set<MethodDescPattern> includepattern = new HashSet<MethodDescPattern>();
    private Set<MethodDescPattern> excludepattern = new HashSet<MethodDescPattern>();

    private class MethodDescPattern{
        Pattern owner;
        Pattern name;
        Pattern desc;
        MethodDescPattern(Pattern owner,Pattern name,Pattern desc){
            if (owner==null || name==null || desc==null){
                AgentLogger.sys
                    .severe("init MethodPattern error!owner==null || name==null || desc==null");
                throw new RuntimeException();
            }
            this.owner = owner;
            this.name = name;
            this.desc = desc;
        }
        boolean match(String outOwner,String outName,String outDesc){
            Matcher ownerMatcher = owner.matcher(outOwner);
            Matcher nameMatcher = name.matcher(outName);
            Matcher descMatcher = desc.matcher(outDesc);
            return ownerMatcher.matches() && nameMatcher.matches() && descMatcher.matches();
        }
    }

    private void setInclude(Set<Config.MethodDesc> include){
        if (include!=null){
            for (Config.MethodDesc desc:include){
                if (desc.owner!=null && desc.name!=null && desc.desc!=null){
                    MethodDescPattern methodDescPattern = new MethodDescPattern(
                            Pattern.compile(desc.owner),Pattern.compile(desc.name),Pattern.compile(desc.desc)
                    );
                    includepattern.add(methodDescPattern);
                }
            }
        }
    }
    private void setExclude(Set<Config.MethodDesc> exclude){
        if (exclude!=null){
            for (Config.MethodDesc desc:exclude){
                if (desc.owner!=null && desc.name!=null && desc!=null){
                    MethodDescPattern methodDescPattern = new MethodDescPattern(
                            Pattern.compile(desc.owner),Pattern.compile(desc.name),Pattern.compile(desc.desc)
                    );
                    excludepattern.add(methodDescPattern);
                }
            }
        }
    }
    public MethodPattern(Set<Config.MethodDesc> include,Set<Config.MethodDesc> exclude){
        setInclude(include);
        setExclude(exclude);
    }

    //默认包含所有
    public boolean matchInclude(String owner,String name,String desc){
        for (MethodDescPattern methodDescPattern : includepattern){
            if (methodDescPattern.match(owner,name,desc))
                return true;
        }
        return false;
    }
    //默认包含所有
    public boolean matchExclude(String owner,String name,String desc){
        for (MethodDescPattern methodDescPattern : excludepattern){
            if (methodDescPattern.match(owner,name,desc))
                return true;
        }
        return false;
    }
}
