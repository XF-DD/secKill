package xfdd.seckill.server.service.impl;

import org.apache.ibatis.annotations.ResultMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import xfdd.seckill.model.entity.ItemKill;
import xfdd.seckill.model.mapper.ItemKillMapper;
import xfdd.seckill.server.service.ItemService;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: XF-DD
 * @Date: 20/05/18 21:59
 */
@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ItemKillMapper itemKillMapper;


    /**
     * 获取待秒杀商品列表
     * @return
     * @throws Exception
     */
    @Override
    public List<ItemKill> getKillItems() throws Exception {
        return itemKillMapper.selectAll();
    }

    /**
     * 获取秒杀详情
     * @param id
     * @return
     * @throws Exception
     */
    @Override
    public ItemKill getKillDetail(Integer id) throws Exception {
        ItemKill entity = itemKillMapper.selectById(id);
        if(entity == null){
            throw new Exception("获取秒杀详情,记录不存在");
        }
        return entity;
    }
}
