package xfdd.seckill.server.service;

import xfdd.seckill.model.entity.ItemKill;

import java.util.List;

/**
 * @Author: XF-DD
 * @Date: 20/05/18 21:59
 */
public interface ItemService {

    List<ItemKill> getKillItems() throws Exception;

    ItemKill getKillDetail(Integer id) throws Exception;
}
