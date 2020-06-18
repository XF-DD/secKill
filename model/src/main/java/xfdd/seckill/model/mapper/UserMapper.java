package xfdd.seckill.model.mapper;

import org.apache.ibatis.annotations.Param;
import xfdd.seckill.model.entity.User;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    User selectByUserName(@Param("userName") String userName);

 //   User selectByUserNamePsd(@Param("userName") String userName, @Param("password") String password);
}