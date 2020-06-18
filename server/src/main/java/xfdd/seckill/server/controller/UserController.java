package xfdd.seckill.server.controller;

import com.sun.org.apache.regexp.internal.RE;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import xfdd.seckill.model.entity.User;

/**
 * @Author: XF-DD
 * @Date: 20/05/23 23:41
 */
@Controller
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private Environment environment;

    /**
     * 要登录的，没有权限的
     * 跳到登录页面
     */
    @RequestMapping(value = {"/to/login","/unauth"})
    public String toLogin(){
        return "login";
    }

    @RequestMapping(value = "/login",method = RequestMethod.POST)
    public String login(User user , Model model){
        String errorMsg="";

        try{
            //判断当前用户是否被认证
            if(!SecurityUtils.getSubject().isAuthenticated()){
                //MD5 + 盐
                String password = new Md5Hash(user.getPassword(), environment.getProperty("system.shiro-password-salt")).toString();
                UsernamePasswordToken token = new UsernamePasswordToken(user.getUserName(), password);
                //登录认证
                SecurityUtils.getSubject().login(token);
            }
        }catch (UnknownAccountException e){
            errorMsg = e.getMessage();
            //回写
            model.addAttribute("userName",user.getUserName());
        }catch (DisabledAccountException e){
            errorMsg = e.getMessage();
            model.addAttribute("userName",user.getUserName());
        }catch (IncorrectCredentialsException e){
            errorMsg = e.getMessage();
            model.addAttribute("userName",user.getUserName());
        } catch(Exception e){
            errorMsg  = "用户登录异常";
            e.printStackTrace();
        }
        //回写，如果没有错误信息说明认证成功
        if(StringUtils.isBlank(errorMsg)){
            return "redirect:/index";
        }else {
            model.addAttribute("errorMsg",errorMsg);
            return "login";
        }
    }

    @RequestMapping(value = "/logout")
    public String logout(){
        SecurityUtils.getSubject().logout();
        return "login";
    }

}
