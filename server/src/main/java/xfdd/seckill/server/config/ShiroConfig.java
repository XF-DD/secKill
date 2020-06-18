package xfdd.seckill.server.config;

import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xfdd.seckill.server.utils.CustomRealm;

import java.util.HashMap;
import java.util.Map;

/**
 * shiro的通用化配置
 * @Author: XF-DD
 * @Date: 20/05/24 0:18
 */
@Configuration
public class ShiroConfig {

    @Bean
    public CustomRealm customRealm(){
        return new CustomRealm();
    }

    @Bean
    public SecurityManager securityManager(){
        DefaultSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(customRealm());
        return securityManager;
    }

    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(){
        ShiroFilterFactoryBean bean = new ShiroFilterFactoryBean();
        bean.setSecurityManager(securityManager());
        //登录链接
        bean.setLoginUrl("/to/login");
        //没有认证的跳转到
        bean.setUnauthorizedUrl("/unauth");

    /*
        anon：无需认证就可以访问
        authc：必须认证了才能访问
        user: 必须拥有 记住我 功能才能用
        perms：拥有对某个资源的权限才能访问
        role：拥有某个角色权限才能访问
     */

        Map<String, String> map = new HashMap<>();
        //其他可以匿名访问
        map.put("/**","anon");
        //登录页面不用权限
        map.put("/to/login","anon");

        map.put("/kill/execute/*","authc");
        map.put("/item/detail/*","authc");

        //过滤器
        bean.setFilterChainDefinitionMap(map);
        return bean;
    }
}
