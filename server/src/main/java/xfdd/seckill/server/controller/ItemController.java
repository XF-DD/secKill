package xfdd.seckill.server.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import xfdd.seckill.model.entity.ItemKill;
import xfdd.seckill.server.service.ItemService;

import java.util.List;

/**
 * 等待秒杀商品Controller
 * @Author: XF-DD
 * @Date: 20/05/18 21:52
 */
@Controller
public class ItemController extends SMSController {
    private static final Logger logger = LoggerFactory.getLogger(ItemController.class);
    private static final String prefix = "item";

    @Autowired
    private ItemService itemService;

    @RequestMapping(value = {"/","/index",prefix+"/list",prefix+"/index.html"},method = RequestMethod.GET)
    public String list(Model model){

        try{
            //获取待秒杀的商品列表
            List<ItemKill> list = itemService.getKillItems();
            model.addAttribute("list",list);
            logger.info("获取待秒杀商品列表数据:{}",list);
        }catch(Exception e){
            logger.error("获取带秒杀商品列表发生异常",e.fillInStackTrace());
            return super.error();
        }
        return "list";
    }

    @RequestMapping(value = prefix + "/detail/{id}",method = RequestMethod.GET)
    public String detail(@PathVariable Integer id,Model model){
        if (id==null || id<0){
            return super.error();
        }

        try{
            ItemKill detail = itemService.getKillDetail(id);
            model.addAttribute("detail",detail);
        }catch(Exception e){
            e.printStackTrace();
            return super.error();
        }

        return "info";
    }
}
