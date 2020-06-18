package xfdd.seckill.server.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import xfdd.seckill.api.enums.StatusCode;
import xfdd.seckill.api.response.BaseResponse;
import xfdd.seckill.model.dto.KillSuccessUserInfo;
import xfdd.seckill.model.mapper.ItemKillMapper;
import xfdd.seckill.model.mapper.ItemKillSuccessMapper;
import xfdd.seckill.server.dto.KillDto;
import xfdd.seckill.server.service.IKillService;

import javax.servlet.http.HttpSession;
import java.awt.*;

/**
 * 秒杀Controller
 * @Author: XF-DD
 * @Date: 20/05/19 16:18
 */
@Controller
@RequestMapping("kill")
public class KillController extends SMSController {

    private static final Logger logger = LoggerFactory.getLogger(KillController.class);

    @Autowired
    private IKillService iKillService;

    @Autowired
    private ItemKillSuccessMapper itemKillSuccessMapper;

    /**
     * 商品秒杀核心业务逻辑
     *
     * consumes:只在接收APPLICATION_JSON_UTF8_VALUE的请求
     * @Valid和BindingResult配套使用，@Valid用在参数前，BindingResult作为校验结果绑定返回
     *
     * @param killDto
     * @param result
     * @return
     */
    @RequestMapping(value = "/execute",method = RequestMethod.POST,consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public BaseResponse execute(@RequestBody @Validated KillDto killDto, BindingResult result,HttpSession session){
        if(result.hasErrors() || killDto.getKillId()<=0){
            return new BaseResponse(StatusCode.InvalidParams);
        }

        Object object = session.getAttribute("userId");
        if(object == null){
            return new BaseResponse((StatusCode.UserNotLogin));
        }
        Integer userId = (Integer) object;

        BaseResponse response = new BaseResponse(StatusCode.Success);
        try{
            boolean res = iKillService.killItem(killDto.getKillId(), userId);
            if(!res){
                return new BaseResponse(StatusCode.Fail.getCode(),"哈哈~商品已抢购完毕或者不在抢购时间段哦!");
            }
        }catch(Exception e){
            response = new BaseResponse(StatusCode.Fail.getCode(),e.getMessage());
        }
        return response;
    }

    /**
     * 查看订单详情
     * @return
     */
    @GetMapping("/record/detail/{orderCode}")
    public String killRecordDetail(@PathVariable String orderCode, Model model){
        if(StringUtils.isBlank(orderCode)){
            return super.error();
        }
        KillSuccessUserInfo info = itemKillSuccessMapper.selectByCode(orderCode);
        if(info == null){
            return super.error();
        }
        model.addAttribute("info",info);
        return "killRecord";
    }


    //抢购成功 ！
    @RequestMapping(value = "/execute/success",method = RequestMethod.GET)
    public String executeSuccess(){
        return "executeSuccess";
    }

    //抢购失败 ！
    @RequestMapping(value = "/execute/fail",method = RequestMethod.GET)
    public String executeFail(){
        return "executeFail";
    }

    /**
     * 商品秒杀核心业务逻辑 - 用于压力测试
     * @Valid和BindingResult配套使用，@Valid用在参数前，BindingResult作为校验结果绑定返回
     *
     * @param killDto
     * @param result
     * @return
     */
    @RequestMapping(value = "/execute/lock",method = RequestMethod.POST,consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public BaseResponse executeWithoutLock(@RequestBody @Validated KillDto killDto, BindingResult result, HttpSession session){
        if(result.hasErrors() || killDto.getKillId()<=0){
            return new BaseResponse(StatusCode.InvalidParams);
        }
        Object object = session.getAttribute("userId");
        if(object == null){
            return new BaseResponse((StatusCode.UserNotLogin));
        }
        Integer userId = (Integer) object;
        BaseResponse response = new BaseResponse(StatusCode.Success);
        try{
/*            基于数据库优化：优化不足
                boolean res = iKillService.killItemV2(killDto.getKillId(), killDto.getUserId());
            if(!res){
                return new BaseResponse(StatusCode.Fail.getCode(),"不加分布式锁，哈哈~商品已抢购完毕或者不在抢购时间段哦!");
            }*/
            //TODO 基于Redis的分布式锁进行控制，redis可能宕机，造成死锁
            boolean res = iKillService.killItemV3(killDto.getKillId(), userId);
            if(!res){
                return new BaseResponse(StatusCode.Fail.getCode(),"加redis分布式锁，哈哈~商品已抢购完毕或者不在抢购时间段哦!");
            }

/*            //TODO 基于Redisson的分布式锁进行控制，redisson会自动释放锁
            boolean res = iKillService.killItemV4(killDto.getKillId(), killDto.getUserId());
            if(!res) {
                return new BaseResponse(StatusCode.Fail.getCode(), "加redisson分布式锁，哈哈~商品已抢购完毕或者不在抢购时间段哦!");
            }*/

/*            //TODO 基于zookeeper的分布式锁进行控制
            boolean res = iKillService.killItemV5(killDto.getKillId(), killDto.getUserId());
            if(!res) {
                return new BaseResponse(StatusCode.Fail.getCode(), "加zookeeper分布式锁，哈哈~商品已抢购完毕或者不在抢购时间段哦!");
            }*/
        }catch(Exception e){
            response = new BaseResponse(StatusCode.Fail.getCode(),e.getMessage());
        }
        return response;
    }

//    @Autowired
//    private ItemKillMapper itemKillMapper;
//
//    @GetMapping("/test")
//    @ResponseBody
//    public String test(){
//        itemKillMapper.updateKillItemV2(3);
//        return "Success";
//    }
}
