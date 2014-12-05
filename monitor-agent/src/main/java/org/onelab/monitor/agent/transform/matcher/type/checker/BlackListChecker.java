package org.onelab.monitor.agent.transform.matcher.type.checker;

import org.onelab.monitor.agent.Agent;

/**
 * 类名黑名单校验器
 * Created by chunliangh on 14-11-13.
 */
public class BlackListChecker implements TypeChecker {
    private static final String patten = Agent.config.getBlackListPatten();
    @Override
    public boolean check(String className) {
        if (patten!=null && className.matches(patten)){
            return false;
        }
        return true;
    }
}
