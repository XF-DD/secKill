package xfdd.seckill.model.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import xfdd.seckill.model.entity.ItemKill;

import java.util.List;


@Repository
@Mapper
public interface ItemKillMapper {
    List<ItemKill> selectAll();

    ItemKill selectById(@Param("id") Integer id);

    int updateKillItem(@Param("killId") Integer killId);



    ItemKill selectByIdV2(@Param("id") Integer id);

    int updateKillItemV2(@Param("killId") Integer killId);
}