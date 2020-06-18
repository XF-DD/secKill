package xfdd.seckill.server.utils;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import xfdd.seckill.model.entity.User;
import xfdd.seckill.model.mapper.UserMapper;

import java.util.Objects;

/**
 * 用户自定义的的realm - 用于shiro的认证，授权
 * @Author: XF-DD
 * @Date: 20/05/24 0:19
 */
public class CustomRealm extends AuthorizingRealm {
    private static final Logger logger = LoggerFactory.getLogger(CustomRealm.class);

    //5分钟
    private static final Long sessionKeyTimeOut = 30_000L;

    @Autowired
    private UserMapper userMapper;

    /**
     * 授权
     * @param principalCollection
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        return null;
    }

    /**
     * 认证 - 登录
     * @param authenticationToken
     * @return
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;
        String username = token.getUsername();
        String password = String.valueOf(token.getPassword());
        logger.info("当前登录的用户名={} 密码={}",username,password);

        User user = userMapper.selectByUserName(username);
        if(user == null){
            //AuthenticationException及其子类
            throw new UnknownAccountException("用户名不存在");
        }
        if(!Objects.equals(1,user.getIsActive().intValue())){
            throw new DisabledAccountException("当前用户已被禁用");
        }
        if(!user.getPassword().equals(password)){
            throw new IncorrectCredentialsException("用户名密码不匹配！");
        }

        SimpleAuthenticationInfo simpleAuthenticationInfo = new SimpleAuthenticationInfo(user.getUserName(),password,getName());
        setSession("userId",user.getId());
        return simpleAuthenticationInfo;
    }

    /**
     * 将key与对应value塞入shiro的session中-最终交给HttpSession进行管理(如果是分布式session配置，那么就是交给Redis管理)
     * @param key
     * @param value
     */
    private void setSession(String key,Object value){
        Session session = SecurityUtils.getSubject().getSession();
        if(session != null){
            session.setAttribute(key,value);
            session.setTimeout(sessionKeyTimeOut);
        }
    }
}
