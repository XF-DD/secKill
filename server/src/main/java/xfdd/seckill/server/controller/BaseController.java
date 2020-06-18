package xfdd.seckill.server.controller;

import jodd.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import xfdd.seckill.api.enums.StatusCode;
import xfdd.seckill.api.response.BaseResponse;

/**
 * @Author: XF-DD
 * @Date: 20/05/18 20:50
 */
@Controller
@RequestMapping("base")
public class BaseController {
    private static final Logger logger = LoggerFactory.getLogger(BaseController.class);

    @GetMapping("/welcome")
    public String welcome(String name, Model model){
        if(StringUtil.isBlank(name)){
            name = "这是欢迎尼玛";
        }
        model.addAttribute("name",name);
        return "welcome";
    }

    @RequestMapping(value = "/data",method = RequestMethod.GET)
    @ResponseBody
    public String data(String name){
        if(StringUtil.isBlank(name)){
            name = "这是欢迎尼玛";
        }
        return name;
    }
    @RequestMapping(value = "/response",method = RequestMethod.GET)
    @ResponseBody
    public BaseResponse response(String name){
        BaseResponse response = new BaseResponse(StatusCode.Success);
        if(StringUtil.isBlank(name)){
            name = "hello";
        }
        response.setData(name);
        return response;
    }



    @RequestMapping(value = "/error",method = RequestMethod.GET)
    public String toError(){
        return "error";
    }
}
