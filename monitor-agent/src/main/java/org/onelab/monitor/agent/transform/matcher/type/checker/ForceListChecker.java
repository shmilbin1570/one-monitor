package org.onelab.monitor.agent.transform.matcher.type.checker;

import org.onelab.monitor.agent.Agent;

/**
 * 强制转换类校验器
 * Created by chunliangh on 14-11-21.
 */
public class ForceListChecker implements TypeChecker {
    private static final String patten = Agent.config.getForceListPatten();
    @Override
    public boolean check(String className) {
        if (patten!=null && className.matches(patten)){
            return true;
        }
        return false;
    }
}
